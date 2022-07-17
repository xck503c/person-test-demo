package com.xck.jdk.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 对比两种阻塞队列的性能
 */
public class ArrayAndLinkBlockingQueuePerformanceTest {

    public static void main(String[] args) throws InterruptedException{
        MultiVABMessagePCNoEQQueueTest();
    }

    public static void MultiVABMessagePCNoEQQueueTest() throws InterruptedException{
        testBase(8000, 2, 1);
        testBase(8000, 4, 1);
        testBase(8000, 4, 2);
        testBase(8000, 8, 4);
        testBase(8000, 16, 8);

        testBase(8000, 1, 2);
        testBase(8000, 1, 4);
        testBase(8000, 1, 6);
        testBase(8000, 1, 8);
        testBase(8000, 2, 4);
        testBase(8000, 4, 8);
        testBase(8000, 4, 12);
        testBase(8000, 4, 16);
        testBase(8000, 4, 32);
    }

    public static void MultiVABMessagePCEQQueueTest() throws InterruptedException{
        testBase(1000, 4, 4);
        testBase(4000, 4, 4);
        testBase(8000, 4, 4);

        testBase(4000, 8, 8);
        testBase(4000, 16, 16);

        testBase(1000, 1, 1);
        testBase(4000, 1, 1);
    }

    public static CountDownLatch countDownLatch = null;
    public static CountDownLatch isFinish = null;
    public static CountDownLatch isTakeFinish =null;
    public static BlockingQueue queue1 = null;

    public static void testBase(int capacity, int producerSize, int consumerSize) throws InterruptedException{
        String testName = "Capacity" + capacity + "AndThread" + producerSize + "-" + consumerSize;
        System.out.println(testName + " test ArrayBlockingQueue...");
        queue1 = new ArrayBlockingQueue(capacity);
        long t = 0L;
        for(int i=0; i<200; i++){
            t+=test1(producerSize, consumerSize);
        }
        System.out.println(t/200);

        System.out.println(testName + " test LinkedBlockingQueue...");
        queue1 = new LinkedBlockingQueue(capacity);
        t = 0L;
        for(int i=0; i<200; i++){
            t+=test1(producerSize, consumerSize);
        }
        System.out.println(t/200);
    }

    public static long test(int producerSize, int consumerSize) throws InterruptedException{
        countDownLatch = new CountDownLatch(producerSize + consumerSize);
        isFinish = new CountDownLatch(producerSize);
        isTakeFinish = new CountDownLatch(consumerSize);
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

        long time = System.currentTimeMillis()-start;
        while (queue1.size() > 0) {
            Thread.sleep(1);
        }
        for(Thread thread : takTArr){
            thread.interrupt();
        }
        isTakeFinish.await();

//        System.out.println(times + " " + (System.currentTimeMillis()-start));
        return time;
    }

    public static long test1(int producerSize, int consumerSize) throws InterruptedException{
        countDownLatch = new CountDownLatch(producerSize + consumerSize);
        isFinish = new CountDownLatch(producerSize);
        isTakeFinish = new CountDownLatch(consumerSize);
        Thread[] takTArr = new Thread[consumerSize];
        for(int i=0; i<consumerSize; i++){
            takTArr[i] = new Thread(new TakeBatchTask());
            takTArr[i].start();
        }

        for(int i=0; i<producerSize; i++){
            Thread t1 = new Thread(new PutTask());
            t1.start();
        }

        countDownLatch.await();

        long start = System.currentTimeMillis();
        isFinish.await();

        long time = System.currentTimeMillis()-start;
        while (queue1.size() > 0) {
            Thread.sleep(1);
        }
        for(Thread thread : takTArr){
            thread.interrupt();
        }
        isTakeFinish.await();

//        System.out.println(times + " " + (System.currentTimeMillis()-start));
        return time;
    }

    public static class TakeTask implements Runnable{

        @Override
        public void run() {
            countDownLatch.countDown();
            try {
                countDownLatch.await();
                while (!Thread.currentThread().isInterrupted()){
                    Object o = queue1.take();
                }
            } catch (InterruptedException e) {
            }
            isTakeFinish.countDown();
        }
    }

    public static class TakeBatchTask implements Runnable{

        @Override
        public void run() {
            countDownLatch.countDown();
            try {
                countDownLatch.await();
                List<Object> list = new ArrayList<>();
                while (!Thread.currentThread().isInterrupted()){
                    queue1.drainTo(list, 500);
                    if (list.isEmpty()) {
                        queue1.take();
                    } else {
                        list.clear();
                    }
                }
            } catch (InterruptedException e) {
            }
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
                while (count < 800000){
                    queue1.put(count);
                    ++count;
                }
            } catch (InterruptedException e) {
            }
            isFinish.countDown();
        }
    }
}
