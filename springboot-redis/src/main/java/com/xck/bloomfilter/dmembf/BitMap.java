package com.xck.bloomfilter.dmembf;

public interface BitMap<T> {

    /**
     * 前后置调用
     * @return
     */
    boolean doStart();

    boolean doStop();

    /**
     * 根据位索引去设置标识
     * @param bitIndex
     * @return
     */
    boolean set(long bitIndex);

    /**
     * 根据位索引去获取标识
     * @param bitIndex
     * @return true-存在，false-不存在
     */
    boolean get(long bitIndex);

    /**
     * 位图大小
     */
    long bitSize();

    /**
     * 总数据量
     */
    long bitCount();
}
