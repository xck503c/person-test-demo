package com.xck.bloomfilter.dmembf;

/**
 * long的bitMap实现
 * @param <T>
 */
public interface LongBitMap<T> extends BitMap<T> {

    /**
     * 初始化，在指定long索引上设置long值，用于初始化
     * @param longValueIndex
     * @param longValue
     */
    void init(int longValueIndex, long longValue);

    /**
     * 查询指定long索引的long值
     * @param longValueIndex
     * @return
     */
    long getLongValue(int longValueIndex);
}
