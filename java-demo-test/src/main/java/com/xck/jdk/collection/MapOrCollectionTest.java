package com.xck.jdk.collection;

import java.util.HashMap;
import java.util.Random;

/**
 * map或集合测试
 *
 * @author xuchengkun
 * @date 2021/11/11 16:56
 **/
public class MapOrCollectionTest {

    public static void main(String[] args) {
//        testCMapNodeIndex();
        map78ResizeTest();
    }

    /**
     * 移位操作效率更好
     */
    public static void testCMapNodeIndex() {
        long start = System.nanoTime();
        int scale = 2, ABASE = 16;
        for (int i = 0; i < 1000000; i++) {
            int index = i * scale + ABASE;
        }
        System.out.println((System.nanoTime() - start));

        start = System.nanoTime();
        int ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
        for (int i = 0; i < 1000000; i++) {
            long index = ((long) i << ASHIFT) + ABASE;
        }
        System.out.println((System.nanoTime() - start));
    }

    /**
     * 扩容效率测试
     */
    public static void map78ResizeTest() {
        int reSizeTotal = 1024 * 8;
        int total = 10000;

        long max = -1, min = Integer.MAX_VALUE;
        long totalTime = 0;
        for (int j = 0; j < total; j++) {
            HashMap<String, Integer> map = new HashMap<>();
            long start = System.nanoTime();
            for (int i = 0; i < reSizeTotal; i++) {
                map.put("i" + i, 1);
            }
            long diff = System.nanoTime() - start;
            max = max < diff ? diff : max;
            min = min < diff ? min : diff;
            totalTime += (System.nanoTime() - start);
        }
        System.out.println("平均值: " + totalTime / total);
        System.out.println("最大值: " + max);
        System.out.println("最小值: " + min);
    }
}
