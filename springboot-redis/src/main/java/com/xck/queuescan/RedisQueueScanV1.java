package com.xck.queuescan;

import com.xck.redis.RedisPool;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 版本1
 *
 * @author xuchengkun
 * @date 2022/05/06 08:50
 **/
public class RedisQueueScanV1 extends Thread {

    private final int cpu = Runtime.getRuntime().availableProcessors();
    private volatile boolean isRunning = true;
    public BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(24);
    public ExecutorService scanPool = new ThreadPoolExecutor(cpu, cpu, 60, TimeUnit.SECONDS, workQueue);
    private static RedisPool redisPool = new RedisPool();
    private final static String queueName = "test:queue:";
    private static Map<String, Integer> userSpeed = new HashMap<>();
    private static Map<String, List<Long>> userScanPoint = new ConcurrentHashMap<>();
    private static AtomicInteger total = new AtomicInteger(0);
    private static AtomicInteger emptyRuntotal = new AtomicInteger(0);
    private static int hopeSize = 0;

    @Override
    public void run() {
        try {
            final long startThread = System.currentTimeMillis();
            while (isRunning) {
                while (workQueue.remainingCapacity() <= 20) {
                    Thread.sleep(1000);
                    System.out.println("remainingCapacity 20");
                }

                final Set<String> keys = redisPool.scan(queueName + "*", 1000);
                if (keys.size() > 0) {
                    if (!scanPool.isShutdown()) {
                        scanPool.submit(new Runnable() {
                            @Override
                            public void run() {
                                for (String key : keys) {
                                    long start = System.currentTimeMillis();
                                    try {
                                        Thread.sleep(8);
                                    } catch (InterruptedException e) {
                                    }
                                    List<Object> list = redisPool.rpopMulit(key, userSpeed.get(key));
                                    if (list.size() == 0) {
                                        emptyRuntotal.incrementAndGet();
                                        if (total.get() == hopeSize) {
                                            System.out.println("================" + (System.currentTimeMillis() - startThread));
                                        }
                                    } else {
                                        total.addAndGet(list.size());
                                    }
                                    userScanPoint.get(key).add(start);
                                }
                            }
                        });
                    }
                    sleep(5);
                } else {
                    sleep(100);
                }
            }
            scanPool.shutdownNow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void putQueue(String queue, int size) {
        List<String> list = new ArrayList<>(500);
        for (int i = 0; i < size; i++) {
            list.add(i + "");
            if (list.size() >= 500) {
                redisPool.lpushMulit(queue, list);
                list.clear();
            }
        }

        if (list.size() > 0) {
            redisPool.lpushMulit(queue, list);
        }
    }

    public static void main(String[] args) throws Exception{
        int size = test3();
        hopeSize = size;

        RedisQueueScanV1 scanV1 = new RedisQueueScanV1();
        scanV1.start();

        while (total.get() < size) {
            Thread.sleep(100);
        }

        scanV1.isRunning = false;
        Thread.sleep(3000);
        for (String user : userScanPoint.keySet()) {
            List<Long> list = userScanPoint.get(user);
            List<Long> intervalList = new ArrayList<>();
            Collections.sort(list);
            long diff = 0;
            int times = 0;
            for (int i = 0; i < list.size() - 1; i++) {
                long curDiff = list.get(i + 1) - list.get(i);
                diff += curDiff;
                ++times;
                intervalList.add(curDiff);
            }
            System.out.println("user: " + user + ", avgInterval: " + (diff/times));
            System.out.println("user: " + user + ", intervalList: " + intervalList);
        }
        System.out.println("emptyRuntotal: " + emptyRuntotal.get());
    }

    public static int test1() {
        String userPrefix = "xck:";
        putQueue(queueName + userPrefix + "1", 10);
        putQueue(queueName + userPrefix + "2", 10);
        userSpeed.put(queueName + userPrefix + "1", 20);
        userSpeed.put(queueName + userPrefix + "2", 20);
        userScanPoint.put(queueName + userPrefix + "1", new ArrayList<Long>());
        userScanPoint.put(queueName + userPrefix + "2", new ArrayList<Long>());

        return 20;
    }

    public static int test2() {
        String userPrefix = "xck:";
        putQueue(queueName + userPrefix + "1", 50000);
        putQueue(queueName + userPrefix + "10", 50000);
        putQueue(queueName + userPrefix + "20", 50000);
        userSpeed.put(queueName + userPrefix + "1", 1000);
        userSpeed.put(queueName + userPrefix + "10", 1000);
        userSpeed.put(queueName + userPrefix + "20", 1000);
        userScanPoint.put(queueName + userPrefix + "1", new ArrayList<Long>());
        userScanPoint.put(queueName + userPrefix + "10", new ArrayList<Long>());
        userScanPoint.put(queueName + userPrefix + "20", new ArrayList<Long>());

        int size = 50000*3;
        for (int i = 1; i <= 100; i++) {
            if (i == 1 || i == 10 || i == 20) {
                continue;
            }
            putQueue(queueName + userPrefix + i, 1000);
            userSpeed.put(queueName + userPrefix + i, 100);
            userScanPoint.put(queueName + userPrefix + i, new ArrayList<Long>());
            size+=1000;
        }

        return size;
    }

    public static int test3() {
        String userPrefix = "xck:";
        int size = 0;
        for (int i = 1; i <= 140; i++) {
            putQueue(queueName + userPrefix + i, 2000);
            userSpeed.put(queueName + userPrefix + i, 100);
            userScanPoint.put(queueName + userPrefix + i, new ArrayList<Long>());
            size+=2000;
        }

        return size;
    }
}
