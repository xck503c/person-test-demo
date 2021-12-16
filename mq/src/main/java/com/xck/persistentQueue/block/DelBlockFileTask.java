package com.xck.persistentQueue.block;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * 删除块文件任务
 *
 * @author xuchengkun
 * @date 2021/12/08 09:34
 **/
public class DelBlockFileTask extends Thread implements AutoCloseable {

    private volatile boolean isRunning;
    private String delQueueHome;

    public DelBlockFileTask(String delQueueHome) {
        this.delQueueHome = delQueueHome;
        this.isRunning = true;
    }

    @Override
    public void run() {
        while (isRunning) {

            try {
                Thread.sleep(10000);
                File file = new File(delQueueHome);
                if (!file.isDirectory()) {
                    //输出warn信息
                } else {
                    File[] files = file.listFiles();
                    for (File f : files) {
                        if (f.isDirectory()) continue;
                        RandomAccessFile accessFile = new RandomAccessFile(f, "r");
                        boolean isDel = accessFile.readBoolean();
                        if (isDel) {
                            accessFile.close();
                            System.out.println(", " + f.getName());
                        }
                    }
                }
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
