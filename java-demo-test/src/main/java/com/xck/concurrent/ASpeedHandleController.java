package com.xck.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;

/**
 * 速度控制
 */
public class ASpeedHandleController {

    public static class SpeedController {
        long sleepInterval;

        long lastResetTime = System.currentTimeMillis();

        boolean canSend() {
            if (System.currentTimeMillis() - lastResetTime >= sleepInterval) {
                lastResetTime = System.currentTimeMillis();
                return true;
            }
            return false;
        }

        long sleepTime() {
            return sleepInterval - (System.currentTimeMillis() - lastResetTime);
        }
    }

    public static SpeedController create(int threadSize, int totalSize) {
        int perThreadSize = totalSize / threadSize;

        SpeedController speedController = new SpeedController();
        speedController.sleepInterval = 1000 / perThreadSize;

        return speedController;
    }

    public static void main(String[] args) throws Exception {
        final AtomicLong count = new AtomicLong();

        BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);

        ExecutorService executorService = Executors.newFixedThreadPool(1);

        List<SpeedController> lists = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            lists.add(create(2, 4000));
        }

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        long sleepTime = 0L;
                        boolean isCanSend = false;
                        for (SpeedController speedController : lists) {
                            if (speedController.canSend()) {
                                isCanSend = true;
                                String obj = queue.take();
                                count.incrementAndGet();
                            } else {
                                sleepTime = Math.min(sleepTime, speedController.sleepTime());
                            }
                        }

                        if (!isCanSend && sleepTime > 0) {
                            Thread.sleep(sleepTime);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        System.out.println(count.getAndSet(0) + ", " + queue.size());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();


        for (int i = 0; i < 1000000; i++) {
            queue.put(String.valueOf(i));
        }

        Thread.sleep(1000000L);
    }
}
