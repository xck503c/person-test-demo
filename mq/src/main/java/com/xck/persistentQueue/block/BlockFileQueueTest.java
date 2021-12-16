package com.xck.persistentQueue.block;

import java.io.IOException;

/**
 * 读写块文件队列测试
 *
 * @author xuchengkun
 * @date 2021/12/08 09:47
 **/
public class BlockFileQueueTest {

    public static void main(String[] args) throws Exception {
//        System.out.println(1213 >>> 8);
//        System.out.println(4 << 8);
//        byte[] b = NumberUtil.short2Bytes((short)1213);
//        int len = ((int)b[1]) & 0xff;
//        System.out.println(len);
//        write(5000000);
        read(5000000);
//        writeAndRead();
//        delBlockFile();
    }

    public static void write(int size) throws Exception{
        String home = System.getProperty("user.dir") + "/mq/home/";
        BlockFileHeader header = new BlockFileHeader(home, "test");
        try {
            header.load();

            long start = System.currentTimeMillis();
            for (int i=0; i<size; i++) {
//                StringBuilder sb = new StringBuilder();
//                for (int j = 0; j < 1200; j++) {
//                    sb.append(1);
//                }
//                sb.append(System.currentTimeMillis());
//                header.putData(sb.toString().getBytes());
                header.putData((i+"").getBytes());
            }
            System.out.println(System.currentTimeMillis() - start);
        } finally {
            System.out.println(header);
            header.close();
            System.out.println(header);
        }
    }

    public static void read(int size) throws Exception{
        String home = System.getProperty("user.dir") + "/mq/home/";
        BlockFileHeader header = new BlockFileHeader(home, "test");
        long start = System.currentTimeMillis();
        try {
            header.load();

            for (int i=0; i<size; i++) {
//                header.readData();
//                new String(header.readData());
                if (i == 1987331) {
                    System.out.println(i);
                }
                byte[] b = header.readData(true);
                String s = new String(b);
                if (!(s.equals(i+""))){
                    System.out.println(i);
                }
//                byte[] b = header.readData();
//                if (b == null) continue;
//                System.out.println("读: " + new String(b));
//                header.readData();
            }
        } finally {
            System.out.println(System.currentTimeMillis() - start);
            System.out.println(header);
            header.close();
            System.out.println(header);
        }
    }

    public static void writeAndRead() throws Exception{
        String home = System.getProperty("user.dir") + "/mq/home/";
        final BlockFileHeader header = new BlockFileHeader(home, "test");
        try {
            header.load();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    long start = System.currentTimeMillis();
                    try {
                        for (int i=0; i<5000000; i++) {
                            header.putData((i+"").getBytes());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println(System.currentTimeMillis() - start);
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    long start = System.currentTimeMillis();
                    try {
                        int count = 0;
                        while (count != 5000000) {
                            byte[] b = header.readData(true);
                            if (b != null) {
//                                System.out.println(new String(b));
                                ++count;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println(System.currentTimeMillis() - start);
                }
            }).start();

            Thread.sleep(100000000L);
        } finally {
            System.out.println(header);
            header.close();
            System.out.println(header);
        }
    }

    public static void delBlockFile() throws Exception{
        String home = System.getProperty("user.dir") + "/mq/home/";
        BlockFileHeader header = new BlockFileHeader(home, "test");
        try {
            header.load();
            Thread.sleep(60000);
        } finally {
            System.out.println(header);
            header.close();
            System.out.println(header);
        }
    }
}
