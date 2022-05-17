package com.xck.db.test;

import java.util.concurrent.CountDownLatch;

public class CpuSwitchTest {

    static CountDownLatch countDownLatch;
    static CountDownLatch finish;

    public static void main(String[] args) throws Exception {
        int times = 1;
        countDownLatch = new CountDownLatch(times);
        finish = new CountDownLatch(times);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                countDownLatch.countDown();
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                }

                long start = System.currentTimeMillis();
                int i = 0;
                try {
                    while (i < 10000) {
                        Thread.sleep(10);
                        ++i;
                    }
                    long end = System.currentTimeMillis();
                    System.out.println(Thread.currentThread().getName() + " " + (end - start));

                    finish.countDown();

                    Thread.sleep(300000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        Thread[] t = new Thread[times];
        for (int i = 0; i < t.length; i++) {
            t[i] = new Thread(r);
            t[i].start();
        }

        countDownLatch.await();
        long start = System.currentTimeMillis();

        finish.await();
        System.out.println("use: " + (System.currentTimeMillis() - start));
    }
}
