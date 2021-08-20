package com.xck.db;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * myisam myi文件解析
 *
 * @author xuchengkun
 * @date 2021/07/17 22:37
 **/
public class MYIFileParser {

    private static String myiFilePath = "D:\\BaiduNetdiskDownload\\mysql\\ttt_myisam_varchar_no3.MYI";
//    private static String myiFilePath = "D:\\BaiduNetdiskDownload\\mysql\\T.MYI";

    private static char[] arr = {'0', '1', '2', '3', '4', '5', '6', '7', '8'
            , '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static void main(String[] args) throws Exception{
        File file = new File(myiFilePath);
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");

        randomAccessFile.skipBytes(4); //magic
        randomAccessFile.skipBytes(2);
        System.out.println("header_len: " + randomAccessFile.readUnsignedShort());
        System.out.println("state_info_length: " + randomAccessFile.readUnsignedShort());
        System.out.println("base_info_length: " + randomAccessFile.readUnsignedShort());
        System.out.println("base_pos: " + randomAccessFile.readUnsignedShort());
        System.out.println("key_parts: " + randomAccessFile.readUnsignedShort());
        randomAccessFile.skipBytes(2); //unique_key_parts
        int keySize = randomAccessFile.readUnsignedByte();
        System.out.println("keys: " + keySize);
        randomAccessFile.skipBytes(1); //uniques
        randomAccessFile.skipBytes(1); //language for indexes
        System.out.println("max_block_size: " + randomAccessFile.readUnsignedByte());
        randomAccessFile.skipBytes(1); //fulltext_keys
        randomAccessFile.skipBytes(1); //not_used
        System.out.println("state->open_count: " + randomAccessFile.readUnsignedShort());
        randomAccessFile.skipBytes(1); //state->changed
        randomAccessFile.skipBytes(1); //state->sortkey
        System.out.println("state->state.records: " + randomAccessFile.readLong());
        System.out.println("state->state.del: " + randomAccessFile.readLong());
        System.out.println("state->split: " + randomAccessFile.readLong());

        byte[] dellinks = new byte[8];
        randomAccessFile.read(dellinks);
        System.out.println("state->dellink: " + byte2Hex(dellinks));

        System.out.println("state->state.key_file_length: " + randomAccessFile.readLong());
        System.out.println("state->state.data_file_length: " + randomAccessFile.readLong());
        randomAccessFile.skipBytes(8); //state->state.key_empty
        randomAccessFile.skipBytes(8); //state->state.empty
        randomAccessFile.skipBytes(8); //state->auto_increment
        randomAccessFile.skipBytes(8); //state->checksum
        randomAccessFile.skipBytes(4); //state->process
        randomAccessFile.skipBytes(4); //state->unique
        randomAccessFile.skipBytes(4); //state->status
        System.out.println("state->update_count: " + randomAccessFile.readInt());

        List<Long> keyRootList = new ArrayList<>();
        for (int i=0; i<keySize; i++) {
//            byte[] keyRoot = new byte[8];
            long keyRoot = randomAccessFile.readLong();
//            randomAccessFile.read(keyRoot);
            keyRootList.add(keyRoot);
//            System.out.println("state->key_root: " + byte2Hex(keyRoot));
            System.out.println("state->key_root: " + keyRoot);
        }

        String keyRootStr;
        do {
            byte[] keyRoot1 = new byte[8];
            randomAccessFile.read(keyRoot1);
            keyRootStr = byte2Hex(keyRoot1);
        }while ("ffffffffffffffff".equals(keyRootStr));

        System.out.println("skep 8 byte: " + keyRootStr);

        byte[] version = new byte[4];
        randomAccessFile.read(version);
        System.out.println("state->version: " + byte2Hex(version));

        byte[] key_map = new byte[8];
        randomAccessFile.read(key_map);
        System.out.println("state->key_map: " + byte2Hex(key_map));

        byte[] create_time = new byte[8];
        randomAccessFile.read(create_time);
        System.out.println("state->create_time: " + byte2Hex(create_time));

        byte[] recover_time = new byte[8];
        randomAccessFile.read(recover_time);
        System.out.println("state->recover_time: " + byte2Hex(recover_time));

        byte[] check_time = new byte[8];
        randomAccessFile.read(check_time);
        System.out.println("state->check_time: " + byte2Hex(check_time));

        byte[] rec_per_key_rows = new byte[8];
        randomAccessFile.read(rec_per_key_rows);
        System.out.println("state->rec_per_key_rows: " + byte2Hex(rec_per_key_rows));

        for (int i = 0; i < 2; i++) { //key_part num
            byte[] rec_per_key_parts = new byte[4];
            randomAccessFile.read(rec_per_key_parts);
            System.out.println("state->rec_per_key_parts: " + byte2Hex(rec_per_key_parts));
        }

        System.out.println("---------------------------------");

        System.out.println("base->keystart: " + randomAccessFile.readLong());
//        print("base->keystart", 8, randomAccessFile);
        print("base->max_data_file_length", 8, randomAccessFile);
        print("base->max_key_file_length", 8, randomAccessFile);
        print("base->records", 8, randomAccessFile);
        print("base->reloc", 8, randomAccessFile);
        print("base->mean_row_length", 4, randomAccessFile);

        print("base->reclength", 4, randomAccessFile);

        randomAccessFile.skipBytes(16);

        print("base->fields", 4, randomAccessFile);
        print("base->pack_fields", 4, randomAccessFile);
        print("base->rec_reflength", 1, randomAccessFile);
        print("base->key_reflength", 1, randomAccessFile);
        print("base->keys", 1, randomAccessFile);
        print("base->auto_key", 1, randomAccessFile);

        randomAccessFile.skipBytes(6);

        print("base->max_key_length", 2, randomAccessFile);
        print("base->extra_alloc_bytes", 2, randomAccessFile);

        randomAccessFile.skipBytes(14);

        System.out.println("---------------------------------");

        print("keydef->keysegs", 1, randomAccessFile);
        print("keydef->key_alg", 1, randomAccessFile);
        print("keydef->flag", 2, randomAccessFile);
        print("keydef->block_length", 2, randomAccessFile);
        print("key def->keylength", 2, randomAccessFile);
        print("skip 4B", 4, randomAccessFile);
        print("keyseg->type", 1, randomAccessFile);
        print("keyseg->language", 1, randomAccessFile);
        print("skip 6B", 6, randomAccessFile);
        print("keyseg->length", 2, randomAccessFile);
        print("skip 8B", 8, randomAccessFile);

        System.out.println("---------------------------------");

        print("keydef->keysegs", 1, randomAccessFile);
        print("keydef->key_alg", 1, randomAccessFile);
        print("keydef->flag", 2, randomAccessFile);
        print("keydef->block_length", 2, randomAccessFile);
        print("key def->keylength", 2, randomAccessFile);
        print("skip 4B", 4, randomAccessFile);
        print("keyseg->type", 1, randomAccessFile);
        print("keyseg->language", 1, randomAccessFile);
        print("skip 6B", 6, randomAccessFile);
        print("keyseg->length", 2, randomAccessFile);
        print("skip 8B", 8, randomAccessFile);

        System.out.println("---------------------------------");

        long keyRoot1 = keyRootList.get(0);
        randomAccessFile.seek(keyRoot1);
        print("block header", 2, randomAccessFile);
        randomAccessFile.skipBytes(2);

        String flag;
        do {
            flag = byte2Hex(new byte[]{randomAccessFile.readByte()});
        }while (!"01".equals(flag));

        int size = randomAccessFile.readByte();
        print("first key value", size, randomAccessFile);
        print("first key pointer", 4, randomAccessFile);



        randomAccessFile.close();
    }

    private static void readKey(RandomAccessFile randomAccessFile) throws Exception{
        String flag;
        do {
            flag = byte2Hex(new byte[]{randomAccessFile.readByte()});
        }while (!"01".equals(flag));

        int size = randomAccessFile.readByte();
        print("first key value", size, randomAccessFile);
        print("first key pointer", 4, randomAccessFile);
    }

    private static void print(String key, int byteLen, RandomAccessFile randomAccessFile) throws Exception{
        byte[] value = new byte[byteLen];
        randomAccessFile.read(value);
        System.out.println(key + ": " + byte2Hex(value));
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                // 1得到一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

    public static int byte2int(byte[] bytes){
        for (int i = bytes.length-1; i >= 0; i--) {
            for (char c : arr){

            }
        }
        return 0;
    }
}
