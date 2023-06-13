package com.xck.persistentQueue.v2;

import cn.hutool.log.Log;
import cn.hutool.log.dialect.console.ConsoleLog;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 内存写到文件队列
 *
 * @param <T>
 */
public class QueueMem2FileThread<T> extends Thread implements IStop {

    private static Log log = new ConsoleLog(QueueMem2FileThread.class);

    private final long flushInterval = 60000L;

    private int dealBatchSize;
    private PersistentQueue<T> persistentQueue;
    private volatile boolean isRunning = true;
    private volatile boolean isFinished;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private int seq = 0;

    public QueueMem2FileThread(int dealBatchSize, PersistentQueue<T> persistentQueue) {
        this.dealBatchSize = dealBatchSize;
        this.persistentQueue = persistentQueue;
    }

    @Override
    public void run() {
        log.info("start, name=" + persistentQueue.getName());

        long lastFlushedTime = System.currentTimeMillis();
        while (isRunning) {
            isFinished = false;
            try {
                if (persistentQueue.mem2fileQueueSize() < dealBatchSize && System.currentTimeMillis() - lastFlushedTime < flushInterval) {
                    Thread.sleep(2000);
                    continue;
                }

                lastFlushedTime = System.currentTimeMillis();

                List<T> list = persistentQueue.outMem2fileQueue(dealBatchSize);
                if (list.isEmpty()) {
                    Thread.sleep(2000);
                    continue;
                }

                deal(list);

                Thread.sleep(50);
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

    private void deal(List<T> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        String filePath = getFileName(persistentQueue.getBasePath(), list.size());
        log.info("write file, name=" + persistentQueue.getName()
                + ", filePath=" + filePath
                + ", size=" + list.size());

        FileUtil.writeJson(filePath + PersistentQueue.FILE_PREFIX_TMP
                , filePath + PersistentQueue.FILE_PREFIX, list);
    }


    private String getFileName(String basePath, int fileSize) {
        String filePrefix = sdf.format(System.currentTimeMillis()) + (seq++) % 10000;
        return basePath + File.separator + filePrefix + "_" + fileSize;
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
