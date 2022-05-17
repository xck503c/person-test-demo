package com.xck.persistentQueue.block;

import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xuchengkun
 * @date 2021/12/07 13:23
 **/
public class BlockFileHeader implements AutoCloseable {

    public final static long MAX_BLOCK_SIZE = 16 * 1024 * 1024;
    public String home;
    public String headerFileName;
    public String blockFileName;

    private volatile BlockFile readBlockFile;
    private volatile BlockFile writeBlockFile;

    private String queueName;

    /*读写文件序号*/
    private volatile int readBlockFileIndex = 0;
    private volatile int writeBlockFileIndex = 0;

    private ByteArrayInputStream readBaos;
    private ByteArrayOutputStream writeBaos;
    private int bufferSize;

    private RandomAccessFile headerFile;

    private volatile long readPos;
    private volatile long writePos;
    private volatile long lastWriteTime;

    private volatile boolean isStop;

    private Lock lock = new ReentrantLock();


    public BlockFileHeader(String home, String queueName) {
        this.home = home;
        this.queueName = queueName;
        this.headerFileName = home + "headerFile-" + queueName;
        this.blockFileName = home + "blockFile-" + queueName;

        this.lastWriteTime = System.currentTimeMillis();
        this.bufferSize = 8192;
        this.writeBaos = new ByteArrayOutputStream(bufferSize);
        this.isStop = false;
    }

    public BlockFileHeader(String home, String queueName, int bufferSize) {
        this.home = home;
        this.queueName = queueName;
        this.headerFileName = home + "headerFile-" + queueName;
        this.blockFileName = home + "blockFile-" + queueName;

        this.lastWriteTime = System.currentTimeMillis();
        this.bufferSize = bufferSize;
        this.writeBaos = new ByteArrayOutputStream(bufferSize);
    }

    public void load() throws IOException {
        try {
            lock.lock();

            File file = new File(headerFileName);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                RandomAccessFile headerFile = new RandomAccessFile(file, "rw");
                byte[] bytes = queueName.getBytes("UTF-8");
                headerFile.writeShort(bytes.length);
                headerFile.write(bytes);
                headerFile.writeInt(1);
                headerFile.writeInt(1);
                headerFile.writeLong(0);
                headerFile.writeLong(0);
                headerFile.close();
            }
            this.headerFile = new RandomAccessFile(file, "rw");
            short nameLen = headerFile.readShort();
            headerFile.skipBytes(nameLen);
            this.readBlockFileIndex = headerFile.readInt();
            this.writeBlockFileIndex = headerFile.readInt();
            readPos = headerFile.readLong();
            readPos = readPos < 5 ? 5 : readPos;
            writePos = headerFile.readLong();
            writePos = writePos < 5 ? 5 : writePos;

            this.readBlockFile = new BlockFile(this, readBlockFileIndex, readPos, true);
            this.readBlockFile.load();
            this.writeBlockFile = new BlockFile(this, writeBlockFileIndex, writePos, false);
            this.writeBlockFile.load();

            selfCheck();

        } finally {
            lock.unlock();
        }
    }

    public void selfCheck() {

    }

    @Override
    public void close() throws IOException {
        try {
            this.isStop = true;

            lock.lock();

            flush();
            flushPos();
            headerFile.close();

            this.readBlockFile.close();
            this.writeBlockFile.close();
        } finally {
            lock.unlock();
        }
    }

    public boolean putData(byte[] data) throws IOException {
        if (isStop) {
            return false;
        }

        byte[] b = new byte[2 + data.length];
        System.arraycopy(NumberUtil.short2Bytes((short) data.length), 0, b, 0, 2);
        System.arraycopy(data, 0, b, 2, data.length);

        if (writeBaos.size() + b.length > bufferSize) {
            flush();
        }
        writeBaos.write(b);

        lastWriteTime = System.currentTimeMillis();
        return true;
    }

    private void write(byte[] data) throws IOException {

        if (writePos + data.length > MAX_BLOCK_SIZE) {
            ++writeBlockFileIndex;
            if (writeBlockFileIndex > 100000) {
                writeBlockFileIndex = 1;
            }
            writeBlockFile.setNextIndex(writeBlockFileIndex); //设置下个节点文件

            try {
                lock.lock();
                writeBlockFile.close(); //关闭当前文件
                System.out.println(Thread.currentThread().getName() + "关闭写文件: " + writeBlockFile.getIndex());
                writePos = 5;
                this.writeBlockFile = new BlockFile(this, writeBlockFileIndex, writePos, false);
                this.writeBlockFile.load();
                flushPos();
                System.out.println(Thread.currentThread().getName() + "打开写文件: " + writeBlockFile.getIndex());
            } finally {
                lock.unlock();
            }
        }

        writeBlockFile.putData(data);
        writePos += data.length; //增加写指针
        flushPos();
    }

    public void flush() throws IOException {
        if (writeBaos != null && writeBaos.size() > 0) {
            write(writeBaos.toByteArray());
            writeBaos.reset();
        }
    }

    public byte[] readData(boolean isCommit) throws IOException {
        if (isStop) {
            return null;
        }

        if (writeBlockFile.getIndex() == readBlockFile.getIndex() &&
                readPos >= writePos) {
            return null;
        }

        if (readBaos != null && readBaos.available() > 2) {
            byte[] lenBytes = new byte[2];
            readBaos.mark(-1);
            readBaos.read(lenBytes);
            int len = (short) ((lenBytes[0] << 8) + (lenBytes[1] & 0xFF));
            if (readBaos.available() >= len) {
                byte[] data = new byte[len];
                readBaos.read(data);
                if (isCommit) {
                    readPos += (2 + len);
                } else {
                    readBaos.reset();
                }
                return data;
            }
        }

        flushPos();

        try {
            byte[] b = readBlockFile.readData(readPos);
            readBaos = new ByteArrayInputStream(b);
        } catch (EOFException e) {
            try {
                lock.lock();
                //读到头了，需要设置删除标记并且滚动
                int nextReadIndex = readBlockFile.getDiskNextIndex();
                if (nextReadIndex <= 0) {
                    return null;
                }
                readPos = 5;
                readBlockFile.setDel(true);
                readBlockFile.close();
                BlockFile oldReadFile = readBlockFile;
                System.out.println(Thread.currentThread().getName() + "关闭读文件: " + readBlockFile.getIndex());
                this.readBlockFile = new BlockFile(this, nextReadIndex, readPos, true);
                this.readBlockFile.load();
                flushPos();
                oldReadFile.del();
                System.out.println(Thread.currentThread().getName() + "打开读文件: " + readBlockFile.getIndex());
            } finally {
                lock.unlock();
            }
        }
        return readData(isCommit);
    }

    public void flushPos() throws IOException {
        try {
            byte[] bytes = queueName.getBytes("UTF-8");
            byte[] writeB = new byte[2 + bytes.length + 8 + 16];

            lock.lock(); //这里有可能发生竞争

            int i = 0;
            System.arraycopy(NumberUtil.short2Bytes((short) bytes.length)
                    , 0, writeB, i, 2);
            i = i + 2;
            System.arraycopy(bytes, 0, writeB, i, bytes.length);
            i = i + bytes.length;
            System.arraycopy(NumberUtil.int2Bytes(readBlockFile.getIndex())
                    , 0, writeB, i, 4);
            i = i + 4;
            System.arraycopy(NumberUtil.int2Bytes(writeBlockFile.getIndex())
                    , 0, writeB, i, 4);
            i = i + 4;
            System.arraycopy(NumberUtil.long2Bytes(readPos)
                    , 0, writeB, i, 8);
            i = i + 8;
            System.arraycopy(NumberUtil.long2Bytes(writePos)
                    , 0, writeB, i, 8);
            headerFile.seek(0);
            headerFile.write(writeB);
        } finally {
            lock.unlock();
        }
    }

    public String getBlockFileName() {
        return blockFileName;
    }

    public long getLastWriteTime() {
        return lastWriteTime;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    @Override
    public String toString() {
        return "BlockFileHeader{" +
                "blockFileName='" + blockFileName + '\'' +
                ", readBlockFile=" + readBlockFile +
                ", writeBlockFile=" + writeBlockFile +
                ", queueName='" + queueName + '\'' +
                ", readBlockFileIndex=" + readBlockFileIndex +
                ", writeBlockFileIndex=" + writeBlockFileIndex +
                '}';
    }
}
