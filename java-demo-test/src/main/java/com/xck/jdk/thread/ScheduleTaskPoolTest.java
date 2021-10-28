package com.xck.jdk.thread;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 定时线程池测试
 *
 * @author xuchengkun
 * @date 2021/10/18 13:51
 **/
public class ScheduleTaskPoolTest {

    private static int cpu = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) throws Exception{
        ScheduledThreadPoolExecutor taskExecutor = new ScheduledThreadPoolExecutor(cpu + 1
                , new NamedThreadFactory("SyncThreadPool", true));

        for (int i = 0; i < 10; i++) {
            TTask tTask = new TTask();
            tTask.interval = 1000*(i+1);
            tTask.tag = i;
            taskExecutor.scheduleAtFixedRate(tTask, 0, tTask.interval, TimeUnit.MILLISECONDS);
        }

        Thread.sleep(60000);
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        System.out.println(time.format(new Date()) + " close");
        taskExecutor.shutdownNow();
        System.out.println(time.format(new Date()) + " close complete");
        Thread.sleep(15000);
    }

    public static class TTask implements Runnable {
        public long interval;
        public int tag;

        public TTask() {
        }

        @Override
        public void run() {
            SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
            System.out.println(time.format(new Date()) + " " + tag + " start");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            System.out.println(time.format(new Date()) + " " + tag + " end");
        }
    }

    public static class NamedThreadFactory implements ThreadFactory {
        private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        private boolean isDaemon = false;

        public NamedThreadFactory(String name) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            if (null == name || name.isEmpty()) {
                name = "pool";
            }
            namePrefix = name + "-" + POOL_NUMBER.getAndIncrement() + "-thread-";
        }

        public NamedThreadFactory(String name, boolean isDaemon) {
            this(name);
            this.isDaemon = isDaemon;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            t.setDaemon(isDaemon);
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
