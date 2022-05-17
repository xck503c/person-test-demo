package com.xck.persistentQueue.rocksdb;


import com.xck.persistentQueue.block.NumberUtil;
import org.rocksdb.CompactionPriority;
import org.rocksdb.FlushOptions;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;

/**
 * leveldb使用
 *
 * @author xuchengkun
 * @date 2021/12/17 15:59
 **/
public class RocksDBMain {

    public static void main(String[] args) throws Exception{
        write(100000);
        read(100000);


    }

    public static void write(int size) throws Exception{
        RocksDB.loadLibrary();

        final Options options = new Options()
                .setCreateIfMissing(true)
                .setLevelCompactionDynamicLevelBytes(true)
                .setBytesPerSync(1048576)
                .setCompactionPriority(CompactionPriority.MinOverlappingRatio)
                .setMaxBackgroundCompactions(4)
                .setMaxBackgroundFlushes(2);

        String home = System.getProperty("user.dir") + "/mq/rocksdb";
        final RocksDB db = RocksDB.open(options, home);

        try {

            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 1200; j++) {
                sb.append(1);
            }
            long start = System.currentTimeMillis();
            for (int i=0; i<size; i++) {
                sb.append(System.currentTimeMillis());
                db.put(NumberUtil.int2Bytes(i), sb.toString().getBytes());
            }
            System.out.println(System.currentTimeMillis() - start);
        } finally {
            db.close();
        }
    }

    public static void read(int size) throws Exception{
        RocksDB.loadLibrary();

        final Options options = new Options().setCreateIfMissing(true);

        String home = System.getProperty("user.dir") + "/mq/rocksdb";
        final RocksDB db = RocksDB.open(options, home);
        long start = System.currentTimeMillis();
        try {

            for (int i=0; i<size; i++) {
                byte[] b = db.get(NumberUtil.int2Bytes(i));
                String s = new String(b);
                int len = s.length();
//                System.out.println(s);
            }
            System.out.println(System.currentTimeMillis() - start);
        } finally {
            db.close();
        }
    }
}
