package com.xck.persistentQueue.block;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author xuchengkun
 * @date 2021/12/07 13:23
 **/
public class BlockFile implements AutoCloseable {

    private BlockFileHeader blockFileHeader;
    /*当前文件序号*/
    private int index;
    /*读写位置*/
    private long pos;
    /*是否读，否则写*/
    private boolean isRead;

    /*是否删除*/
    private boolean isDel;
    /*下一个文件序号*/
    private int nextIndex;

    private RandomAccessFile blockFileAccess;
    private FileChannel fileChannel;

    public BlockFile(BlockFileHeader header, int index, long pos, boolean isRead) {
        this.blockFileHeader = header;
        this.index = index;
        this.pos = pos;
        this.isRead = isRead;
    }

    public void load() throws IOException {
        String blockFileName = blockFileHeader.getBlockFileName() + "-" + index;
        File file = new File(blockFileName);
        if (!file.exists()) { //初始化块文件
            file.createNewFile();
            blockFileAccess = new RandomAccessFile(blockFileName, "rw");
            fileChannel = blockFileAccess.getChannel();
            blockFileAccess.writeBoolean(false);
            blockFileAccess.writeInt(0);
            setDel(false);
            setNextIndex(0);
        } else {
            blockFileAccess = new RandomAccessFile(
                    blockFileHeader.getBlockFileName() + "-" + index, "rw");
            fileChannel = blockFileAccess.getChannel();
            setDel(blockFileAccess.readBoolean());
            setNextIndex(blockFileAccess.readInt());
        }
        if (pos > 5) {
            blockFileAccess.seek(pos);
        }
    }

    @Override
    public void close() throws IOException {
        if (blockFileAccess != null) {
            blockFileAccess.seek(0);
            if (isRead) {
                blockFileAccess.writeBoolean(isDel());
            } else {
                blockFileAccess.skipBytes(1);
                blockFileAccess.writeInt(getNextIndex());
            }
            blockFileAccess.close();
        }
    }

    public void putData(byte[] data) throws IOException {
        blockFileAccess.write(data);
    }

    public byte[] readData() throws IOException {
        short dataLen = blockFileAccess.readShort();
        byte[] bytes = new byte[dataLen];
        blockFileAccess.readFully(bytes);
        return bytes;
    }

    public byte[] readData(long readPos) throws IOException {
        blockFileAccess.seek(readPos);
        long len = blockFileAccess.length() - 1;

        if (len - readPos + 1 <=2) {
            throw new EOFException();
        }

        if (len - readPos + 1 < blockFileHeader.getBufferSize()) {
            byte[] bytes = new byte[(int) (len - readPos + 1)];
            blockFileAccess.readFully(bytes);
            return bytes;
        }

        byte[] bytes = new byte[blockFileHeader.getBufferSize()];
        blockFileAccess.readFully(bytes);
        return bytes;
    }

    public boolean isDel() {
        return isDel;
    }

    public void setDel(boolean del) {
        isDel = del;
    }

    public int getNextIndex(){
        return nextIndex;
    }

    public int getDiskNextIndex() throws IOException{
        blockFileAccess.seek(0);
        blockFileAccess.readBoolean();
        return blockFileAccess.readInt();
    }

    public void setNextIndex(int nextIndex) {
        this.nextIndex = nextIndex;
    }

    public int getIndex() {
        return index;
    }

    public BlockFileHeader getBlockFileHeader() {
        return blockFileHeader;
    }

    public void setBlockFileHeader(BlockFileHeader blockFileHeader) {
        this.blockFileHeader = blockFileHeader;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setPos(long pos) {
        this.pos = pos;
    }

    public RandomAccessFile getBlockFileAccess() {
        return blockFileAccess;
    }

    public void setBlockFileAccess(RandomAccessFile blockFileAccess) {
        this.blockFileAccess = blockFileAccess;
    }

    @Override
    public String toString() {
        return "BlockFile{" +
                "index=" + index +
                ", pos=" + pos +
                ", isDel=" + isDel +
                ", nextIndex=" + nextIndex +
                ", blockFileAccess=" + blockFileAccess +
                '}';
    }
}
