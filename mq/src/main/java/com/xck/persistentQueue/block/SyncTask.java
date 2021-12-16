package com.xck.persistentQueue.block;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * 删除块文件任务
 *
 * @author xuchengkun
 * @date 2021/12/08 09:34
 **/
public class SyncTask extends Thread implements AutoCloseable {

    private volatile boolean isRunning;
    private BlockFileQueue blockFileQueue;

    public SyncTask(BlockFileQueue blockFileQueue) {
        this.blockFileQueue = blockFileQueue;
        this.isRunning = true;
    }

    @Override
    public void run() {
        long lastWriteTime = 0;
        while (isRunning) {

            try {
                if (lastWriteTime == 0) {
                    lastWriteTime = blockFileQueue.getlastwriteTime();
                } else if (System.currentTimeMillis() - lastWriteTime > 100){
                    blockFileQueue.flush();
                }
                Thread.sleep(10);
            } catch (InterruptedException e) {
            } catch (Throwable e) {
                e.printStackTrace();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e1) {
                }
            }
        }
    }

    @Override
    public void close() {
        isRunning = false;
        interrupt();
    }
}
