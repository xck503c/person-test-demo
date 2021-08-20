package com.xck.jdk.thread;

import java.util.concurrent.CountDownLatch;

/**
 * Created by cord on 2018/5/7.
 * [100] thread concurrent test <nanoTime> cost total time [6716284]ns, average time [67162]ns.
 * [100] count serial test <nanoTime> cost total time [5548]ns, average time [55]ns.
 * -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-
 * [100] thread concurrent test <currentTimeMillis> cost total time [4409745]ns, average time [44097]ns.
 * [100] count serial test <currentTimeMillis> cost total time [4993]ns, average time [49]ns.
 */
public class SystemApiPerfTest {

    public static void main(String[] args) throws InterruptedException {
        int count = 2;
        /**并发*/
        long interval = concurrentTest(count, new Runnable() {
            @Override
            public void run() {
                System.nanoTime();
            }
        });
        System.out.format("[%s] thread concurrent test <nanoTime> cost total time [%s]ns, average time [%s]ns.\n", count, interval, interval/count);

        /**串行循环*/
        interval = serialNanoTime(count);
        System.out.format("[%s] count serial test <nanoTime> cost total time [%s]ns, average time [%s]ns.\n", count, interval, interval/count);

        System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-");

        /**并发*/
        interval = concurrentTest(count, new Runnable() {
            @Override
            public void run() {
                System.currentTimeMillis();
            }
        });
        System.out.format("[%s] thread concurrent test <currentTimeMillis> cost total time [%s]ns, average time [%s]ns.\n", count, interval, interval/count);

        /**串行循环*/
        interval = serialCurrentTime(count);
        System.out.format("[%s] count serial test <currentTimeMillis> cost total time [%s]ns, average time [%s]ns.\n", count, interval, interval/count);

    }

    private static long concurrentTest(int threads, final Runnable r) throws InterruptedException {
        final CountDownLatch start = new CountDownLatch(threads+1);
        final CountDownLatch end = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        start.countDown();
                        start.await();
                        try {
                            r.run();
                        }finally {
                            end.countDown();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        Thread.sleep(1000);
        long stime = System.nanoTime();
        start.countDown();
        end.await();
        return System.nanoTime() - stime;
    }

    private static long serialNanoTime(int count){
        long stime = System.nanoTime();
        for (int i = 0; i < count; i++) {
            System.nanoTime();
        }
        return System.nanoTime() - stime;
    }

    private static long serialCurrentTime(int count){
        long stime = System.nanoTime();
        for (int i = 0; i < count; i++) {
            System.currentTimeMillis();
        }
        return System.nanoTime() - stime;
    }
}
