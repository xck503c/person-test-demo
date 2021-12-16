package com.xck.persistentQueue.block;

import com.alibaba.fastjson.JSONObject;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * 对比两种阻塞队列的性能
 */
public class BlockBlockingQueuePerformanceTest {

    public static void main(String[] args) throws Exception{
        MultiVABMessagePCNoEQQueueTest();
    }

    public static void MultiVABMessagePCNoEQQueueTest() throws Exception{
        //每个线程put 50w int类型的数据，每次放入队列都是500大小的List，每个测试200次，取平均值
//        testBase(8000, 2, 1); //avg 260ms
        testBase(3, 3); //avg 528ms
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
    public static BlockFileQueue queue1 = null;

    public static void testBase(int producerSize, int consumerSize) throws Exception{
        String home = System.getProperty("user.dir") + "/mq/home/";
        queue1 = new BlockFileQueue(home, "test", 256 * 1024);
        long t = 0L;
        for(int i=0; i<1; i++){
            t+=test(producerSize, consumerSize);
        }
        System.out.println("end: " + t/200);
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

        for(int i=0, j=0; i<producerSize; i++, j = j + 1500000){
            Thread t1 = new Thread(new PutTask(j, j + 1500000));
            t1.start();
        }

        countDownLatch.await();

        long start = System.currentTimeMillis();
        isFinish.await();
        isTakeFinish.await();
        long time = System.currentTimeMillis()-start;
        System.out.println(time);
        isStop = true;

        return time;
    }

    private final static Set<Integer> set = new HashSet<>(1600000);

    public static class TakeTask implements Runnable{

        public TakeTask() {
        }

        @Override
        public void run() {
            countDownLatch.countDown();
            int count = 0;
            try {
                countDownLatch.await();
                while (count < 1500000){
                    byte[] o = queue1.poll();
                    if (o != null) {
                        String s = new String(o);
                        TestObj testObj = JSONObject.parseObject(s, TestObj.class);
                        ++count;
                    }
                }
            } catch (InterruptedException e) {
            }
            System.out.println("task " + Thread.currentThread().getName() + " end " + count);
            isTakeFinish.countDown();
        }
    }

    public static class PutTask implements Runnable{

        private int start;
        private int end;

        public PutTask(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            countDownLatch.countDown();
            try {
                countDownLatch.await();
                for (int i = start; i < end; i++) {
                    queue1.offer(JSONObject.toJSONString(new TestObj(i)).getBytes());
                }
            } catch (InterruptedException e) {
            }
            System.out.println("put " + Thread.currentThread().getName() + " end");
            isFinish.countDown();
        }
    }

    public static class TestObj {
        private String s;
        private int id;

        public TestObj(int id) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1200; i++) {
                sb.append(1);
            }
            this.s = sb.toString();
            this.id = id;
        }

        public String getS() {
            return s;
        }

        public void setS(String s) {
            this.s = s;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}
