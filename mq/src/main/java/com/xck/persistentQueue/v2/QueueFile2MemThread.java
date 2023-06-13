package com.xck.persistentQueue.v2;

import cn.hutool.log.Log;
import cn.hutool.log.dialect.console.ConsoleLog;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class QueueFile2MemThread<T> extends Thread implements IStop {

    private static Log log = new ConsoleLog(QueueFile2MemThread.class);

    private int dealBatchSize;
    private PersistentQueue<T> persistentQueue;
    private volatile boolean isRunning = true;
    private volatile boolean isFinished;

    private LinkedList<String> filePathList = new LinkedList<>();

    public QueueFile2MemThread(int dealBatchSize, PersistentQueue<T> persistentQueue) {
        this.dealBatchSize = dealBatchSize;
        this.persistentQueue = persistentQueue;
    }

    @Override
    public void run() {
        log.info("start, name=" + persistentQueue.getName());

        while (isRunning) {
            isFinished = false;
            try {
                if (persistentQueue.mem2fileQueueRemainingSize() < dealBatchSize) {
                    Thread.sleep(1000);
                    continue;
                }

                scanPath();

                List<T> list = deal();
                if (list == null || list.isEmpty()) {
                    Thread.sleep(2000);
                    continue;
                }

                persistentQueue.enQueueBatch(list);
            } catch (InterruptedException e) {
                log.error("name=" + persistentQueue.getName(), e);
            } catch (Exception e) {
                log.error("name=" + persistentQueue.getName(), e);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                }
            } finally {
                isFinished = true;
            }
        }

        log.info("end, name=" + persistentQueue.getName());
    }

    private void scanPath() {
        if (filePathList.isEmpty()) {
            List<String> paths = FileUtil.getFilePathList(persistentQueue.getBasePath());
            if (paths == null || paths.isEmpty()) {
                return;
            }

            for (String path : paths) {
                if (path.endsWith(PersistentQueue.FILE_PREFIX)) {
                    filePathList.addFirst(path);
                }
            }

            log.info("scan " + persistentQueue.getBasePath() + ", fileSize=" + paths.size());
        }
    }

    private List<T> deal() {
        List<T> resultList = new ArrayList<>(dealBatchSize);
        String path = null;
        while ((path = filePathList.pollLast()) != null) {
            List<T> list = FileUtil.readJson(path, true, persistentQueue.getDataType());
            log.info("read file, name=" + persistentQueue.getName()
                    + ", path=" + path
                    + ", readSize=" + list.size());

            if (list != null && list.size() > 0) {
                resultList.addAll(list);
            }

            if (list.size() >= dealBatchSize) {
                break;
            }
        }

        return resultList;
    }

    @Override
    public void doStop() {
        isRunning = false;
        interrupt();
        log.info("ready stop, name=" + persistentQueue.getName());
    }

    @Override
    public boolean isStopped() {
        return !isRunning && isFinished;
    }
}
