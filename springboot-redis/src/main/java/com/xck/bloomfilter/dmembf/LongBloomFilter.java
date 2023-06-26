package com.xck.bloomfilter.dmembf;

import com.google.common.hash.Funnel;
import com.google.common.primitives.UnsignedBytes;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LongBloomFilter<T> {

    private final LongBitMap longBitMap;

    private final int numHashFunctions;

    private final Strategy strategy;

    private final Funnel<? super T> funnel;

    public LongBloomFilter(LongBitMap longBitMap, int numHashFunctions, Funnel<? super T> funnel, Strategy strategy) {
        this.longBitMap = longBitMap;
        this.numHashFunctions = numHashFunctions;
        this.funnel = funnel;
        this.strategy = strategy;
    }

    public static <T> LongBloomFilter<T> createDirect(Funnel<? super T> funnel, long expectedInsertions, double fpp) {
        if (expectedInsertions == 0) {
            expectedInsertions = 1;
        }

        long numBits = optimalNumOfBits(expectedInsertions, fpp);
        int numHashFunctions = optimalNumOfHashFunctions(expectedInsertions, numBits);
        try {
            return new LongBloomFilter<T>(new LongDirectBitMap(numBits), numHashFunctions, funnel, BloomFilterStrategy.MURMUR128_MITZ_64);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Could not create LongBloomFilter of " + numBits + " bits", e);
        }
    }

    public boolean put(T object) {
        return strategy.put(object, funnel, numHashFunctions, longBitMap);
    }

    public boolean mightContain(T object) {
        return strategy.mightContain(object, funnel, numHashFunctions, longBitMap);
    }

    public long bitMapSize() {
        return longBitMap.bitSize();
    }

    /**
     * 计算需要多少位
     *
     * @param n 期望数据量
     * @param p 允许误差
     * @return
     */
    static long optimalNumOfBits(long n, double p) {
        if (p == 0) {
            p = Double.MIN_VALUE;
        }
        return (long) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
    }

    /**
     * 计算hash函数个数
     *
     * @param n 期望数据量
     * @param m 承载n个数据量所需的位数
     * @return
     */
    static int optimalNumOfHashFunctions(long n, long m) {
        return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
    }


    public static <T> LongBloomFilter<T> read2Direct(InputStream in, Funnel<? super T> funnel) throws IOException {
        int strategyOrdinal = -1;
        int numHashFunctions = -1;
        int dataLength = -1;
        try {
            DataInputStream din = new DataInputStream(in);
            strategyOrdinal = din.readByte();
            numHashFunctions = UnsignedBytes.toInt(din.readByte());
            dataLength = din.readInt();

            Strategy strategy = BloomFilterStrategy.values()[strategyOrdinal];
            LongDirectBitMap longDirectBitMap = new LongDirectBitMap(dataLength);
            for (int i = 0; i < dataLength; i++) {
                longDirectBitMap.init(i, din.readLong());
            }
            return new LongBloomFilter<T>(longDirectBitMap, numHashFunctions, funnel, strategy);
        } catch (RuntimeException e) {
            String message =
                    "Unable to deserialize LongBloomFilter from InputStream."
                            + " strategyOrdinal: "
                            + strategyOrdinal
                            + " numHashFunctions: "
                            + numHashFunctions
                            + " dataLength: "
                            + dataLength;
            throw new IOException(message, e);
        }
    }

    public boolean doStart() {
        return longBitMap.doStart();
    }

    public boolean doStop() {
        return longBitMap.doStop();
    }

    @Override
    public String toString() {
        return "LongBloomFilter{" +
                "longBitMap=" + longBitMap +
                ", numFunc=" + numHashFunctions +
                '}';
    }
}
