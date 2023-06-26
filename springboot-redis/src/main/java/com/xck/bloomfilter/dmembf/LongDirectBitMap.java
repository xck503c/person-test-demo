package com.xck.bloomfilter.dmembf;

import com.google.common.math.LongMath;
import com.google.common.primitives.Ints;
import sun.nio.ch.DirectBuffer;

import java.math.RoundingMode;
import java.nio.ByteBuffer;

public class LongDirectBitMap implements LongBitMap {

    /**
     * 实现需要兼容google guava，相当于将long[]拆成8字节
     */
    private ByteBuffer[] byteBuffer;

    private long bitSize;

    private long bitCount;

    public LongDirectBitMap(long numBits) {

        int len = Ints.checkedCast(LongMath.divide(numBits, 64, RoundingMode.CEILING));
        this.bitSize = len * 64L;

        byteBuffer = new ByteBuffer[8];
        for (int i = 0; i < byteBuffer.length; i++) {
            byteBuffer[i] = ByteBuffer.allocateDirect(len);
        }
    }

    public LongDirectBitMap(int len) {
        this.bitSize = len * 64L;
        byteBuffer = new ByteBuffer[8];
        for (int i = 0; i < byteBuffer.length; i++) {
            byteBuffer[i] = ByteBuffer.allocateDirect(len);
        }
    }

    @Override
    public boolean doStart() {
        return true;
    }

    @Override
    public boolean doStop() {
        for (int i = 0; i < byteBuffer.length; i++) {
            if (byteBuffer[i].isDirect()) {
                ((DirectBuffer) byteBuffer[i]).cleaner().clean();
            }
        }
        return true;
    }

    @Override
    public boolean set(long bitIndex) {
        if (get(bitIndex)) {
            return false;
        }

        // 计算索引位置
        int index = (int) (bitIndex >>> 6);
        long mask = 1L << (int) bitIndex;
        long oldValue = getLongValue(bitIndex);
        long newValue = oldValue | mask;

        ByteBuffer tmp = ByteBuffer.allocate(8);
        tmp.putLong(newValue);
        byte[] tmpArr = tmp.array();
        for (int i = 0; i < byteBuffer.length; i++) {
            byteBuffer[i].put(index, tmpArr[i]);
        }

        ++bitCount;

        return true;
    }

    @Override
    public void init(int longValueIndex, long longValue) {
        ByteBuffer tmp = ByteBuffer.allocate(8);
        tmp.putLong(longValue);
        byte[] tmpArr = tmp.array();
        for (int i = 0; i < byteBuffer.length; i++) {
            byteBuffer[i].put(longValueIndex, tmpArr[i]);
        }

        ++bitCount;
    }

    @Override
    public long bitSize() {
        return this.bitSize;
    }

    @Override
    public long bitCount() {
        return this.bitCount;
    }

    private long getLongValue(long bitIndex) {
        // 计算索引位置
        int index = (int) (bitIndex >>> 6);
        ByteBuffer tmp = ByteBuffer.allocate(8);
        for (int i = 0; i < byteBuffer.length; i++) {
            tmp.put(byteBuffer[i].get(index));
        }
        tmp.flip();
        return tmp.getLong();
    }

    @Override
    public boolean get(long bitIndex) {
        return (getLongValue(bitIndex) & (1L << bitIndex)) != 0;
    }

    @Override
    public long getLongValue(int longValueIndex) {
        ByteBuffer tmp = ByteBuffer.allocate(8);
        for (int i = 0; i < byteBuffer.length; i++) {
            tmp.put(byteBuffer[i].get(longValueIndex));
        }
        tmp.flip();
        return tmp.getLong();
    }

    @Override
    public String toString() {
        return "{bitSize=" + bitSize/8/1024 + "KB" +
                ", bitCount=" + bitCount +
                '}';
    }
}
