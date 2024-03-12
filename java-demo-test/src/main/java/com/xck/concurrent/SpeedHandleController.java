package com.xck.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 速度控制
 */
public class SpeedHandleController {

    public static void main(String[] args) throws Exception{
        final AtomicLong start = new AtomicLong();

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        executorService.scheduleAtFixedRate(new Runnable() {
            int inc = 0;

            @Override
            public void run() {
                if (start.get() == 0) {
                    start.set(System.currentTimeMillis());
                }
                ++inc;
                System.out.println(inc);
            }
        }, 0, 10, TimeUnit.MICROSECONDS);

//        for (int i = 0; i < 300033; i++) {
//            if (start.get() == 0) {
//                start.set(System.currentTimeMillis());
//            }
//            TimeUnit.MICROSECONDS.sleep(10);
//        }


        // 10
//        Thread.sleep(3000);
        System.out.println("useTime=" + (System.currentTimeMillis() - start.get()));
        System.exit(-1);
    }
}
