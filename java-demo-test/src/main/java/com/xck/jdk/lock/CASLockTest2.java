package com.xck.jdk.lock;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class CASLockTest2 {

    private static int shareInc = 0;
    private static TestLock testLock = new TestLock();

    public static void main(String[] args) {
//        for (int i = 0; i < 20; i++) {
//            CASTestThread casTestThread = new CASTestThread();
//            casTestThread.start();
//        }
//
//
//        while (true) {
//            System.out.println(shareInc);
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2023);
        calendar.set(Calendar.MONTH, 5);
        calendar.set(Calendar.DATE, 1);

        for (int i = 0; i < 30; i++) {
            String sql = "select count(1) from third_black_mobile_" + new SimpleDateFormat("yyyyMMdd").format(calendar.getTime())
                    + " where dfrom=4;";
            System.out.println(sql);
            calendar.add(Calendar.DATE, 1);
        }
    }

    private static class CASTestThread extends Thread {

        @Override
        public void run() {
            for (int i = 0; i < 100000; i++) {
                try {
                    testLock.lock();
                    ++shareInc;
                } finally {
                    testLock.unlock();
                }
            }
        }
    }

    private static class TestLock {
        // 自旋锁
        private AtomicInteger lock = new AtomicInteger();
        // 线程队列
        private List<Thread> list = new ArrayList<>();

        public void lock() {
            boolean isAddList = false;
            int times = 0;
            Thread t = Thread.currentThread();
            while (!lock.compareAndSet(0, 1)) {
                if (times++ < 100) {
                    times = 0;
                    if (!isAddList) {
                        synchronized (list) {
                            list.add(t);
                            isAddList = true;
                        }
                    }
                    LockSupport.park();
                }
            }

            // 获得资源，将自己移除
            if (isAddList) {
                synchronized (list) {
                    list.remove(t);
                }
            }
        }

        public void unlock() {
            lock.set(0);
            // 只唤醒第一个
            if (list.size() > 0) {
                synchronized (list) {
                    if (list.size() > 0) {
                        LockSupport.unpark(list.get(0));
                    }
                }
            }
        }
    }
}
