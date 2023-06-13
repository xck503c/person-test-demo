package com.xck.persistentQueue.v2;

import cn.hutool.log.Log;
import cn.hutool.log.dialect.console.ConsoleLog;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PerQueueV2Test {

    private transient static Log log = new ConsoleLog(PersistentQueue.class);

    public static void main(String[] args) throws Exception {
        PersistentQueue<TestClass> persistentQueue = new PersistentQueue<>(
                "v2test", 8000, 15000
                , System.getProperty("user.dir") + File.separator + "v2/", 5000, TestClass.class);
        persistentQueue.doStart();

        int threadSize = 8;

        TestPThread[] testPThreads = new TestPThread[threadSize];
        for (int i = 0; i < threadSize; i++) {
            testPThreads[i] = new TestPThread(i, persistentQueue);
            testPThreads[i].start();
        }

        Thread.sleep(3000);

        while (true) {
            boolean isAllStop = true;
            for (int i = 0; i < threadSize; i++) {
                TestPThread testPThread = testPThreads[i];
                if (!testPThread.isResult()) {
                    log.info(testPThread.get_mid() + ", 不停止");
                    isAllStop = false;
                    break;
                }
            }
            if (isAllStop) {
                break;
            }
            Thread.sleep(1000);
        }

        persistentQueue.doStop();

        Thread.sleep(10000);

        String path = System.getProperty("user.dir") + File.separator + "cache" + File.separator + "persistentQueue";
        FileUtil.writeObj(path + ".cache", persistentQueue);
        persistentQueue = FileUtil.readObj(path + ".cache");
        log.info("read queue " + persistentQueue);
        persistentQueue.doStart();

        Thread.sleep(3000);

        TestCThread[] testCThreads = new TestCThread[threadSize];
        for (int i = 0; i < threadSize; i++) {
            testCThreads[i] = new TestCThread(i, persistentQueue);
            testCThreads[i].start();
        }
    }

    public static class TestPThread extends Thread {

        private int _mid;
        private PersistentQueue<TestClass> persistentQueue;
        private volatile boolean result = false;

        public TestPThread(int id, PersistentQueue<TestClass> persistentQueue) {
            this._mid = id;
            this.persistentQueue = persistentQueue;
        }

        @Override
        public void run() {
            log.info("start id=" + _mid);
            for (int j = 0; j < 200000; j++) {
                persistentQueue.enQueue(new TestClass(_mid + "-" + j));
            }
            log.info(_mid + ", 生产完成");
            result = true;
        }

        public boolean isResult() {
            return result;
        }

        public int get_mid() {
            return _mid;
        }
    }

    public static class TestCThread extends Thread {

        private int id;
        private PersistentQueue<TestClass> persistentQueue;
        private Set<String> set = new HashSet<>();

        public TestCThread(int id, PersistentQueue<TestClass> persistentQueue) {
            this.id = id;
            this.persistentQueue = persistentQueue;
        }

        @Override
        public void run() {
            try {
                for (int j = 0; j < 200000; j++) {
                    set.add(id + "-" + j);
                }
                while (!set.isEmpty()) {
                    List<TestClass> list = persistentQueue.outMemQueue(10);
                    if (list == null) {
                        Thread.sleep(100);
                        System.out.println(id + ", no,no,no,no,");
                        continue;
                    }

                    for (TestClass testClass : list) {
                        if (!testClass.getKey().startsWith(id + "")) {
                            persistentQueue.enQueue(testClass);
                            continue;
                        }

                        if (!set.contains(testClass.getKey())) {
                            log.error(id + ", 出现异常数据, " + testClass.getKey());

                        } else {
                            set.remove(testClass.getKey());
                        }
                    }
                }

                System.out.println(id + ", 消费完成");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public Set<String> getSet() {
            return set;
        }
    }

    public static class TestClass implements java.io.Serializable {

        private static final long serialVersionUID = -2329373130836326046L;

        String content = "sdfdsf发送端是公司大股东股份";

        String key = "";

        int age = 11;

        public TestClass(String key) {
            this.key = key;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

}
