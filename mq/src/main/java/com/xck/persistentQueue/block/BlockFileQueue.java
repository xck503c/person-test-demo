package com.xck.persistentQueue.block;

import java.io.IOException;
import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 块文件队列
 *
 * @author xuchengkun
 * @date 2021/12/07 11:40
 **/
public class BlockFileQueue extends AbstractQueue<byte[]> {

    BlockFileHeader blockFileHeader;

    private Lock readLock = new ReentrantLock();
    private Lock writeLock = new ReentrantLock();
    private SyncTask syncTask;

    public BlockFileQueue(String home, String queueName) throws IOException {
        this.blockFileHeader = new BlockFileHeader(home, queueName);
        this.blockFileHeader.load();

        this.syncTask = new SyncTask(this);
        syncTask.start();
    }

    public BlockFileQueue(String home, String queueName, int bufferSize) throws IOException {
        this.blockFileHeader = new BlockFileHeader(home, queueName, bufferSize);
        this.blockFileHeader.load();

        this.syncTask = new SyncTask(this);
        syncTask.start();
    }

    public long getlastwriteTime() {
        return blockFileHeader.getLastWriteTime();
    }

    public void flush() {
        try {
            writeLock.lock();
            blockFileHeader.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean offer(byte[] bytes) {
        try {
            writeLock.lock();
            return blockFileHeader.putData(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
        return false;
    }

    @Override
    public byte[] poll() {
        try {
            readLock.lock();
            return blockFileHeader.readData(true);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            readLock.unlock();
        }
        return null;
    }

    @Override
    public Iterator<byte[]> iterator() {
        return null;
    }

    @Override
    public byte[] peek() {
        try {
            readLock.lock();
            return blockFileHeader.readData(false);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            readLock.unlock();
        }
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    public void close() throws IOException {
        this.blockFileHeader.close();
    }
}
