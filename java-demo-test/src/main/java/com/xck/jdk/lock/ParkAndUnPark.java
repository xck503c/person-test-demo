package com.xck.jdk.lock;

import java.util.concurrent.locks.LockSupport;

public class ParkAndUnPark {

    public static void main(String[] args) {

        Thread t = new ParkThread();

        t.start();

        for (int i = 1; i <= 3; i++) {
            LockSupport.unpark(t);
        }

        System.out.println("unpark finish " + System.currentTimeMillis());
    }

    private static class ParkThread extends Thread {

        public void run() {
            try {
                Thread.sleep(3000);
                System.out.println("before park1 " + System.currentTimeMillis());
                LockSupport.park();
                System.out.println("after park1, before park2");
                LockSupport.park();
                System.out.println("after park2, before park3");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
