package com.xck.persistentQueue.v2;

import cn.hutool.log.Log;
import cn.hutool.log.dialect.console.ConsoleLog;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class PersistentQueue<T> implements java.io.Serializable {

    private static final long serialVersionUID = 5776749136465242264L;

    private transient static Log log = new ConsoleLog(PersistentQueue.class);

    public static final String FILE_PREFIX_TMP = ".tmp";
    public static final String FILE_PREFIX = ".queue";

    /**
     * 队列名字
     */
    private String name;

    /**
     * 基础文件路径
     */
    private String basePath;

    private int dealBatchSize;

    private Class dataType;

    /**
     * 内存队列
     * 内存准备写入文件队列
     */
    private BlockingQueue<T> memQueue;
    private BlockingQueue<T> mem2fileQueue;

    private transient QueueFile2MemThread queueFile2MemThread;
    private transient QueueMem2FileThread queueMem2FileThread;

    public PersistentQueue(String name, int memQueueSize, int mem2fileQueueSize
            , String basePath, int dealBatchSize, Class dataType) {
        this.name = name;
        this.memQueue = new ArrayBlockingQueue<>(memQueueSize);
        if (StringUtils.isNoneBlank(basePath)) {
            this.mem2fileQueue = new ArrayBlockingQueue<>(mem2fileQueueSize);
            this.basePath = basePath + File.separator + name;
            this.dealBatchSize = dealBatchSize;
            this.dataType = dataType;
        }
    }

    public void doStart() {
        log.info("start " + this.toString());
        if (StringUtils.isNoneBlank(basePath)) {
            queueFile2MemThread = new QueueFile2MemThread(dealBatchSize, this);
            queueFile2MemThread.setName("queueFile2MemThread-" + name);
            queueMem2FileThread = new QueueMem2FileThread(dealBatchSize, this);
            queueMem2FileThread.setName("queueMem2FileThread-" + name);

            queueFile2MemThread.start();
            queueMem2FileThread.start();
        }
    }

    public void doStop() throws InterruptedException{
        if (StringUtils.isNoneBlank(basePath)) {
            log.info("start stop queue name=" + name);
            queueFile2MemThread.doStop();
            queueMem2FileThread.doStop();

            long start = System.currentTimeMillis();
            while (!queueFile2MemThread.isStopped() || !queueMem2FileThread.isStopped()) {
                if (System.currentTimeMillis() - start >= 5000) {
                    break;
                }

                Thread.sleep(200);
            }
            log.info("stop success queue name=" + name + ", useTime=" + (System.currentTimeMillis() - start));
        }
    }

    /**
     * 待处理队列数据获取接口
     *
     * @param limit
     * @return
     */
    public List<T> outMemQueue(int limit) {
        List<T> list = new ArrayList<>(limit);

        if (memQueue.size() > 0) {
            memQueue.drainTo(list, limit);
        }

        if (mem2fileQueue != null && mem2fileQueue.size() > 0 && limit > list.size()) {
            mem2fileQueue.drainTo(list, limit - list.size());
        }

        return list;
    }

    /**
     * 入内存队列，如果队列空间不够则放到带写入文件队列
     * @param list
     * @throws InterruptedException
     */
    public boolean enQueueBatch(List<T> list) {
        if (list == null) return true;

        boolean isOneFalse = false;
        for (T t : list) {
            if (isOneFalse) {
                log.warn("queueName=" + name + " lost data=" + t);
                continue;
            }

            if (!enQueue(t)) {
                isOneFalse = true;
            }
        }

        return true;
    }

    /**
     * 入内存队列，如果队列空间不够则放到带写入文件队列
     * @param t
     * @throws InterruptedException
     */
    public boolean enQueue(T t) {
        if (t == null) return true;

        try {
            if (!memQueue.offer(t)) {
                if (!memQueue.offer(t)) {
                    if (mem2fileQueue != null) {
                        mem2fileQueue.put(t);
                    } else {
                        memQueue.put(t);
                    }
                }
            }

            return true;
        } catch (InterruptedException e) {
            log.warn("queueName=" + name + " lost data=" + t);
        }
        return false;
    }

    /**
     * 获取待写入文件队列数据
     *
     * @param limit
     * @return
     */
    public List<T> outMem2fileQueue(int limit) {
        List<T> list = new ArrayList<>(limit);
        if (mem2fileQueue != null) {
            mem2fileQueue.drainTo(list, limit);
        }
        return list;
    }

    public int memQueueSize() {
        return memQueue.size();
    }

    public int mem2fileQueueSize() {
        return mem2fileQueue == null ? 0 : mem2fileQueue.size();
    }

    public int memQueueRemainingSize() {
        return memQueue.remainingCapacity();
    }

    public int mem2fileQueueRemainingSize() {
        return mem2fileQueue == null ? 0 : mem2fileQueue.remainingCapacity();
    }

    public String getName() {
        return name;
    }

    public String getBasePath() {
        return basePath;
    }

    public Class getDataType() {
        return dataType;
    }

    public String queueMonitorLog() {
        StringBuilder sb = new StringBuilder();
        sb.append(", memQueueSize=").append(memQueue.size())
                .append(", mem2fileQueueSize=").append(mem2fileQueue.size());
        return sb.toString();
    }

    @Override
    public String toString() {
        return "{" +
                "name=" + name +
                ", basePath=" + basePath +
                ", dealBatchSize=" + dealBatchSize +
                ", memQueueSize=" + memQueue.size() +
                ", mem2fileQueueSize=" + mem2fileQueue.size() +
                ", dataType=" + dataType +
                '}';
    }
}
