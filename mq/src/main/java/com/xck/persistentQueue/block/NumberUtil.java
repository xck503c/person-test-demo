package com.xck.persistentQueue.block;

/**
 * @author xuchengkun
 * @date 2021/12/14 09:54
 **/
public class NumberUtil {

    public static byte[] short2Bytes(short value) {
        byte[] b = new byte[2];
        b[0] = (byte) ((value >>> 8) & 0xFF);
        b[1] = (byte) (value & 0xFF);
        return b;
    }

    public static byte[] int2Bytes(int value) {
        byte[] b = new byte[4];
        b[0] = (byte) ((value >>> 24) & 0xFF);
        b[1] = (byte) ((value >>> 16) & 0xFF);
        b[2] = (byte) ((value >>> 8) & 0xFF);
        b[3] = (byte) ((value >>> 0) & 0xFF);
        return b;
    }

    public static byte[] long2Bytes(long value) {
        byte[] b = new byte[8];
        b[0] = (byte) ((value >>> 56) & 0xFF);
        b[1] = (byte) ((value >>> 48) & 0xFF);
        b[2] = (byte) ((value >>> 40) & 0xFF);
        b[3] = (byte) ((value >>> 32) & 0xFF);
        b[4] = (byte) ((value >>> 24) & 0xFF);
        b[5] = (byte) ((value >>> 16) & 0xFF);
        b[6] = (byte) ((value >>> 8) & 0xFF);
        b[7] = (byte) ((value >>> 0) & 0xFF);
        return b;
    }
}
