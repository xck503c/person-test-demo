package com.xck.jdk.thread;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * @Classname ThreadContextSwitchingOverHead
 * @Description TODO
 * @Date 2020/12/6 22:47
 * @Created by xck503c
 *
 * 原代码来自：https://www.cnblogs.com/stevenczp/p/6422263.html，但是感觉实现有点问题，改了一下，到时候顺便测试一下io切换开销
 */
public class ThreadContextSwitchingOverHead {

    public static void main(String[] args) {
        final AtomicInteger count = new AtomicInteger(0);
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    LockSupport.park();
                    count.incrementAndGet();
                    Thread.interrupted();//clear interrupt flag
                }
            }
        });
        thread.start();

        for(int i =0;i<1;i++) {
            new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        if (!thread.isInterrupted()) {
                            thread.interrupt();
                        }
                    }
                }
            }).start();
        }
        new Thread(new Runnable() {
            public void run() {
                int i = 0;
                while (true) {
                    try {
                        Thread.sleep(1000);
                        ++i;
                        System.out.println(String.format("thread park %f times in 1s",(count.get()/(double)i))); //440152.551724
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


}
