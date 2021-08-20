package com.xck.jdk.queue;

import java.util.concurrent.SynchronousQueue;

/**
 * 同步队列测试
 *
 * @author xuchengkun
 * @date 2021/08/12 10:02
 **/
public class SynchronousQueueTest {

    public static void main(String[] args) throws Exception{

        final SynchronousQueue<Integer> queue = new SynchronousQueue<>();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    queue.put(1);
                    System.out.println("end");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(queue.poll());
                System.out.println("end");
            }
        });
        t1.start();

        t.join();
    }
}
