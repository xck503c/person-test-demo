package com.xck.jdk.thread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 递增数字多线程测试
 *
 * @author xuchengkun
 * @date 2021/10/19 15:59
 **/
public class MultiThreadIncNumberTest {

    public static AtomicLong seq1 = new AtomicLong(0);
    private static Lock globalLock = new ReentrantLock();

    public static AtomicBoolean lockAtomic = new AtomicBoolean(false);
    public static volatile long globalNumber = 0;
    public static AtomicInteger globalAtomicInc = new AtomicInteger();

    public static CountDownLatch startLatch;
    public static CountDownLatch endLatch;

    public static void main(String[] args) throws Exception{
        int threadNum = 8;
        int repeat = 1000000;

        startLatch = new CountDownLatch(threadNum);
        endLatch = new CountDownLatch(threadNum);
        Thread[] tArr = new Thread[threadNum];


        for (int i = 0; i < threadNum; i++) {
            tArr[i] = new Thread(new TestTask(repeat));
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < threadNum; i++) {
            tArr[i].start();
        }

        endLatch.await();
        System.out.println(System.currentTimeMillis()-start);
    }

    public static class TestTask implements Runnable{

        private int count;

        public TestTask(int count) {
            this.count = count;
        }

        @Override
        public void run() {
            try {
                startLatch.countDown();
                startLatch.await();
            } catch (InterruptedException e) {
            }

            long start = System.currentTimeMillis();
            while (count-- > 0) {
//                getIncNumber5();
//                getIncNumber4();
//                getIncNumber3();
                getIncNumber2();
//                getIncNumber1();
            }
            long end = System.currentTimeMillis();
            System.out.println(Thread.currentThread().getName() + " use=" + (end-start));

            endLatch.countDown();
        }
    }

    public static long getIncNumber6() {
        int number = globalAtomicInc.incrementAndGet();
//        if (number > 999) {
//            try {
//                globalLock.lock();
//                if (globalAtomicInc > 999)
//            } finally {
//                globalLock.unlock();
//            }
//        }

        return number;
    }

    public static long getIncNumber5() {
        long number = 0L;
        while (!lockAtomic.compareAndSet(false, true)) {}
        number = (globalNumber++) % 1000; //求余运算耗时
        if (globalNumber < 0) {
            globalNumber = 0;
        }
        lockAtomic.set(false);

        return number;
    }

    public static long getIncNumber4() {
        long number = 0L;
        synchronized (globalLock) {
            number = globalNumber;
            if (++globalNumber > 999) {
                globalNumber = 0;
            }
        }

        return number;
    }

    public static long getIncNumber3() {
        long number = 0L;
        try {
            globalLock.lock();
            number = globalNumber;
            if (++globalNumber > 999) {
                globalNumber = 0;
            }
        } finally {
            globalLock.unlock();
        }

        return number;
    }

    public static long getIncNumber2() {
        long number = 0L;
        while (!lockAtomic.compareAndSet(false, true)) {}
        number = globalNumber;
        if (++globalNumber > 999) {
            globalNumber = 0;
        }
        lockAtomic.set(false);

        return number;
    }

    public static long getIncNumber1() {
        long number = 0;
        number = seq1.incrementAndGet();
        if (number >= 999) {
            try {
                globalLock.lock();
                number = seq1.incrementAndGet();
                if (number >= 999) {
                    seq1.set(0);
                    number = 0;
                }
            } finally {
                if (globalLock != null) {
                    globalLock.unlock();
                }
            }
        }
        return number;
    }
}
