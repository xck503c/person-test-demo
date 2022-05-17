package com.xck.persistentQueue.normal;

import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 普通读写
 *
 * @author xuchengkun
 * @date 2021/05/27 11:17
 **/
public class NormalPersistentQueue<E extends Serializable> extends AbstractQueue<E> {

    private String queueName;
    private String queueHomePath;
    //写入批次数量
    private int dealbatchSize;
    private int seq = 0; //文件序列号
    private long writeTimeout; //缓冲区刷新超时时间
    //缓冲队列
    private BlockingQueue<E> writeBuffer = new ArrayBlockingQueue<>(5000);
    private BlockingQueue<E> readBuff = new ArrayBlockingQueue<>(5000);
    private BlockingQueue<File> fileBuf = new ArrayBlockingQueue<>(5000);

    private volatile boolean isStop;
    private Thread flushBufferThread;
    private Thread readBufferThread;
    private AtomicLong writeSize = new AtomicLong(0L);
    private Class clzz;
    private volatile String curWriteFile; //正在写的文件

    public NormalPersistentQueue(String queueHomePath) {
        this.queueHomePath = queueHomePath;
        this.dealbatchSize = 5000;
        this.writeTimeout = 10000;
        this.isStop = false;

        this.flushBufferThread = new Thread(new FlushTask());
        this.flushBufferThread.start();

        this.readBufferThread = new Thread(new ReadTask());
        this.readBufferThread.start();

        this.clzz = String.class;
    }

    @Override
    public boolean offer(E e) {
        if (!writeBuffer.offer(e)) {
            flushBufferThread.interrupt();
            try {
                writeBuffer.put(e);
            } catch (InterruptedException e1) {
                return false;
                //save
            }
        }

        return true;
    }

    @Override
    public E poll() {
        E e = readBuff.poll();
        if (e == null) {
            try {
                e = readBuff.take();
            } catch (InterruptedException e1) {
                //
            }
        }
        return e;
    }

    @Override
    public Iterator<E> iterator() {
        return null;
    }

    @Override
    public E peek() {
        return null;
    }

    @Override
    public int size() {
        return writeBuffer.size() + readBuff.size() + (int) writeSize.get();
    }

    public void flush(List<E> list) {
        File file = getWriteFile(list.size());
        this.curWriteFile = file.getAbsolutePath();
        boolean isSuc = false;
        BufferedWriter fw = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file, true);
            fw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
            fw.write(JSONObject.toJSONString(list));
            fw.flush();
            fos.getFD().sync();
            isSuc = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e1) {
            }
            if (isSuc) {
                fileBuf.offer(file);
                writeSize.addAndGet(list.size());
                this.curWriteFile = null;
            }
        }
    }

    private File getWriteFile(int writeSize) {
        if (seq >= 10000) {
            seq = 1;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String fileName = sdf.format(System.currentTimeMillis()) + seq + "_" + writeSize;
        File file = new File(queueHomePath + "/" + fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }
        return file;
    }

    /**
     * 读很慢，300 ~ 500ms之间
     *
     * @return
     */
    public List<E> read(File file) {
        long start = System.currentTimeMillis();
        boolean isSuc = false;
        List<E> list = new ArrayList<>();
        BufferedReader br = null;
        String line = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));

            while ((line = br.readLine()) != null) {
                list.add((E) JSONObject.parseObject(line, clzz));
            }
            isSuc = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {
            }
            if (isSuc) {
                file.delete();
                writeSize.addAndGet(-list.size());
            }
            System.out.println("读: " + (System.currentTimeMillis() - start));
        }
        return list;
    }

    /**
     * 扫描目录文件放入文件信息缓冲区
     */
    private void scanDir() {
        File dir = new File(queueHomePath);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File[] fileList = dir.listFiles();
        if (fileList.length == 0) {
            return;
        }
        for (int i = 0; i < fileList.length; i++) {
            File f = fileList[i];
            if (!f.isFile() || f.getAbsolutePath().equals(curWriteFile)) {
                continue;
            }
            fileBuf.offer(f);
        }
    }

    /**
     * 刷新写缓冲区线程，刷新的条件有2个：
     * 1.缓冲区积压数量超过一定批次
     * 2.缓冲区积压数量未超过批次，但很长时间没刷了
     */
    private class FlushTask implements Runnable {
        @Override
        public void run() {
            long lastFlushTime = System.currentTimeMillis();
            while (!isStop) {
                try {
                    boolean isExceedDealBatchSize = writeBuffer.size() >= dealbatchSize;
                    boolean isLongTimeNoFlush = (System.currentTimeMillis() - lastFlushTime) > writeTimeout;
                    boolean isFlush = isExceedDealBatchSize || isLongTimeNoFlush;
                    if (writeBuffer.isEmpty()) {
                        Thread.sleep(100);
                        continue;
                    }
                    if (!isFlush) {
                        Thread.sleep(100);
                        continue;
                    }
                    List<E> batchList = new ArrayList<>();
                    for (int i = 0; i < dealbatchSize; i++) {
                        E e = writeBuffer.poll();
                        if (e == null) continue;
                        batchList.add(e);
                    }
                    lastFlushTime = System.currentTimeMillis();
                    if (batchList.isEmpty()) continue;

                    flush(batchList);
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * 填充读缓冲区线程：
     */
    private class ReadTask implements Runnable {
        @Override
        public void run() {
            while (!isStop) {
                try {
                    if (readBuff.remainingCapacity() < dealbatchSize) {
                        Thread.sleep(100);
                        continue;
                    }
                    //文件等待
                    File item = fileBuf.poll();
                    if (item == null) {
                        scanDir();
                        item = fileBuf.poll();
                        if (item == null) {
                            item = fileBuf.take();
                        }
                    }
                    //读取数据
                    List<E> list = read(item);
                    if (list == null || list.isEmpty()) {
                        continue;
                    }

                    for (E e : list) {
                        if (e == null) continue;
                        readBuff.put(e);
                    }
                } catch (InterruptedException e) {
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stop() {
        this.isStop = true;
        flushBufferThread.interrupt();
        readBufferThread.interrupt();
    }

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
        System.out.println(System.currentTimeMillis() / 1000 - 1640024291);
        System.out.println(1639982179700L);
    }
}
