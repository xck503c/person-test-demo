package com.xck.persistentQueue.bdb;

import com.alibaba.fastjson.JSONObject;
import com.xck.persistentQueue.block.BlockBlockingQueuePerformanceTest;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * 对比两种阻塞队列的性能
 */
public class BlockingQueuePerformanceTest {

    public static void main(String[] args) throws InterruptedException{
        MultiVABMessagePCNoEQQueueTest();
    }

    public static void MultiVABMessagePCNoEQQueueTest() throws InterruptedException{
        //每个线程put 50w int类型的数据，每次放入队列都是500大小的List，每个测试200次，取平均值
//        testBase(8000, 2, 2); //avg 260ms
        testBase(8000, 3, 3); //avg 528ms
//        testBase(8000, 4, 2); //avg 545ms
//        testBase(8000, 8, 4); //avg 1166ms
//
//        testBase(8000, 1, 2); //avg 191ms
//        testBase(8000, 1, 4); //avg 175ms
//        testBase(8000, 1, 6); //avg 159ms
//        testBase(8000, 1, 8); //avg 173ms
//        testBase(8000, 2, 4); //avg 303ms
    }

    public static CountDownLatch countDownLatch = null;
    public static CountDownLatch isFinish = null;
    public static CountDownLatch isTakeFinish =null;
    public static volatile boolean isStop = false;
    public static BDBPersistentQueue<String> queue1 = null;

    public static void testBase(int capacity, int producerSize, int consumerSize) throws InterruptedException{
        String path = System.getProperty("user.dir");
        queue1 = new BDBPersistentQueue<>(path + "/mq/bdb", "test", String.class);
        long t = 0L;
        for(int i=0; i<1; i++){
            t+=test(producerSize, consumerSize);
        }
//        System.out.println("end: " + t/200);
        queue1.close();
    }

    public static long test(int producerSize, int consumerSize) throws InterruptedException{
        countDownLatch = new CountDownLatch(producerSize + consumerSize);
        isFinish = new CountDownLatch(producerSize);
        isTakeFinish = new CountDownLatch(consumerSize);
        isStop =  false;
        Thread[] takTArr = new Thread[consumerSize];
        for(int i=0; i<consumerSize; i++){
            takTArr[i] = new Thread(new TakeTask());
            takTArr[i].start();
        }

        for(int i=0; i<producerSize; i++){
            Thread t1 = new Thread(new PutTask());
            t1.start();
        }

        countDownLatch.await();

        long start = System.currentTimeMillis();
        isFinish.await();

        while (queue1.size() > 0) {
            Thread.sleep(1);
        }
        long time = System.currentTimeMillis()-start;
        System.out.println(time);
        isStop = true;
        isTakeFinish.await();

        return time;
    }

    public static class TakeTask implements Runnable{

        @Override
        public void run() {
            countDownLatch.countDown();
            try {
                countDownLatch.await();
                int count = 0;
                while (!isStop){
                    Object o = queue1.poll();
                    if (o != null) {
                        System.out.println(++count);
                    }
                }
            } catch (InterruptedException e) {
            }
            System.out.println("task " + Thread.currentThread().getName() + " end");
            isTakeFinish.countDown();
        }
    }

    public static class PutTask implements Runnable{

        @Override
        public void run() {
            int count = 0;
            countDownLatch.countDown();
            try {
                countDownLatch.await();
                while (count < 500000){
                    queue1.offer(JSONObject.toJSONString(new BlockBlockingQueuePerformanceTest.TestObj(count)));
                    ++count;
                }
            } catch (InterruptedException e) {
            }
            System.out.println("put " + Thread.currentThread().getName() + " end");
            isFinish.countDown();
        }
    }
}
