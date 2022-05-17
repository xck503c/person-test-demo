package com.xck.queuescan;

import com.xck.redis.RedisPool;
import redis.clients.jedis.Response;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 版本1
 *
 * @author xuchengkun
 * @date 2022/05/06 08:50
 **/
public class RedisQueueScanV2 extends Thread {

    private final int cpu = Runtime.getRuntime().availableProcessors();
    private volatile boolean isRunning = true;
    public BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(1000);
    public ExecutorService scanPool = new ThreadPoolExecutor(cpu, cpu, 60, TimeUnit.SECONDS, workQueue);
    private static RedisPool redisPool = new RedisPool();
    private final static String queueName = "test:queue:";
    private static Map<String, Integer> userSpeed = new HashMap<>();
    private static Map<String, List<Long>> userScanPoint = new ConcurrentHashMap<>();
    private static AtomicInteger total = new AtomicInteger(0);
    private static AtomicInteger emptyRuntotal = new AtomicInteger(0);

    private static Map<String, Integer> userTaskMap = new ConcurrentHashMap<>();
    private static int userBatchSize = 16;
    private static int batchScanSize = 400;
    private static int hopeSize = 0;

    private static long startThread = 0;

    @Override
    public void run() {
        try {
            startThread = System.currentTimeMillis();
            while (isRunning) {
                while (workQueue.remainingCapacity() < 20) {
                    Thread.sleep(1000);
                    System.out.println("remainingCapacity 20");
                }

                final Set<String> keys = redisPool.scan(queueName + "*", 1000);
                if (keys.size() > 0) {
                    Set<String> taskSet = new HashSet<>(userBatchSize);
                    for (String key : keys) {
                        if (userTaskMap.containsKey(key)) {
                            continue;
                        }

                        taskSet.add(key);
                        if (taskSet.size() >= userBatchSize) {
                            System.out.println("submit " + taskSet);
                            if (!scanPool.isShutdown()) {
                                for (String tmp : taskSet) {
                                    userTaskMap.put(tmp, 1);
                                }
                                scanPool.submit(new ScanTask(taskSet));
                                taskSet = new HashSet<>();
                            }
                        }
                    }

                    if (taskSet.size() > 0) {
                        if (!scanPool.isShutdown()) {
                            System.out.println("submit " + taskSet);
                            for (String tmp : taskSet) {
                                userTaskMap.put(tmp, 1);
                            }
                            scanPool.submit(new ScanTask(taskSet));
                        }
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

        RedisQueueScanV2 scanV1 = new RedisQueueScanV2();
        scanV1.start();

        while (total.get() < size) {
            Thread.sleep(10);
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
        for (int i = 1; i <= 1200; i++) {
            putQueue(queueName + userPrefix + i, 2000);
            userSpeed.put(queueName + userPrefix + i, 100);
            userScanPoint.put(queueName + userPrefix + i, new ArrayList<Long>());
            size+=2000;
        }

        return size;
    }

    public static class ScanTask implements Runnable {

        private Set<String> taskSet;

        public ScanTask(Set<String> taskSet) {
            this.taskSet = taskSet;
        }

        @Override
        public void run() {
            Map<String, Integer> scanMap = new HashMap<>();
            int totalscan = 0;
            for (String key : taskSet) {
                Integer speed = userSpeed.get(key);
                totalscan += speed;
            }
            for (String key : taskSet) {
                Integer speed = userSpeed.get(key);
                int size = (batchScanSize * speed) /totalscan;
                size = size == 0 ? 1 : size;
                scanMap.put(key, size);
            }

            long start = System.currentTimeMillis();
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
            }
            Map<String, List<Response<String>>> responseMap = redisPool.getMessageFromDataCenterAsync(scanMap);
            List<Object> list = new ArrayList<>();
            for (String queue : responseMap.keySet()) {
                List<Response<String>> responses = responseMap.get(queue);

                for (Response<String> response : responses) {
                    String tmp = null;
                    try {
                        tmp = response.get();
                        if (tmp == null) continue;

                        list.add(tmp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            for (String key : taskSet) {
                userScanPoint.get(key).add(start);
            }

            for (String tmp : taskSet) {
                userTaskMap.remove(tmp);
            }

            if (list.size() == 0) {
                emptyRuntotal.incrementAndGet();
            } else {
                total.addAndGet(list.size());
                if (total.get() == hopeSize) {
                    System.out.println("================" + (System.currentTimeMillis() - startThread));
                }
            }
            System.out.println(System.currentTimeMillis() - start);
        }
    }
}
