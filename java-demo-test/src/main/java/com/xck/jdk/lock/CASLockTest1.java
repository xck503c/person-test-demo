package com.xck.jdk.lock;

import java.util.concurrent.atomic.AtomicInteger;

public class CASLockTest1 {

    private static int shareInc = 0;
    private static AtomicInteger lock = new AtomicInteger();

    public static void main(String[] args) {

    }

    private static class CASTestThread extends Thread {

        @Override
        public void run() {
            for (int i = 0; i < 100; i++) {
                try {
                    while (!lock.compareAndSet(0, 1)) {}
                    ++shareInc;
                } finally {
                    lock.set(0);
                }
            }
        }
    }
}
