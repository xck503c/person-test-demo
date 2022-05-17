package com.xck.persistentQueue.bdb;

import java.io.File;
import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.collections.StoredSortedMap;
import com.sleepycat.je.*;

/**
 * @contributor
 * @param <E>
 */
public class BDBPersistentQueue<E extends Serializable> extends AbstractQueue<E> implements
        Serializable {
    private static final long serialVersionUID = 3427799316155220967L;
    private transient BdbEnvironment dbEnv;            // 数据库环境,无需序列化
    private transient Database queueDb;             // 数据库,用于保存值,使得支持队列持久化,无需序列化
    private transient StoredMap<Long,E> queueMap;   // 持久化Map,Key为指针位置,Value为值,无需序列化
    private transient String dbDir;                 // 数据库所在目录
    private transient String dbName;                // 数据库名字
    private AtomicLong headIndex;                   // 头部指针
    private AtomicLong tailIndex;                   // 尾部指针
    private transient E peekItem=null;              // 当前获取的值
    private volatile boolean isStop = false;

    /**
     * 构造函数,传入BDB数据库
     *
     * @param db
     * @param valueClass
     * @param classCatalog
     */
    public BDBPersistentQueue(Database db, Class<E> valueClass, StoredClassCatalog classCatalog){
        this.queueDb=db;
        this.dbName=db.getDatabaseName();
        headIndex=new AtomicLong(0);
        tailIndex=new AtomicLong(0);
        bindDatabase(queueDb,valueClass,classCatalog);
        SyncTask syncTask = new SyncTask();
        syncTask.setDaemon(true);
        syncTask.start();
    }
    /**
     * 构造函数,传入BDB数据库位置和名字,自己创建数据库
     *
     * @param dbDir
     * @param dbName
     * @param valueClass
     */
    public BDBPersistentQueue(String dbDir, String dbName, Class<E> valueClass){
        this.dbDir=dbDir;
        this.dbName=dbName;
        createAndBindDatabase(dbDir,dbName,valueClass);
        SyncTask syncTask = new SyncTask();
        syncTask.setDaemon(true);
        syncTask.start();
    }
    /**
     * 绑定数据库
     *
     * @param db
     * @param valueClass
     * @param classCatalog
     */
    public void bindDatabase(Database db, Class<E> valueClass, StoredClassCatalog classCatalog){
        EntryBinding<E> valueBinding = TupleBinding.getPrimitiveBinding(valueClass);
        if(valueBinding == null) {
            valueBinding = new SerialBinding<E>(classCatalog, valueClass);   // 序列化绑定
        }
        queueDb = db;
        queueMap = new StoredSortedMap<Long,E>(
                db,                                             // db
                TupleBinding.getPrimitiveBinding(Long.class),   //Key 序列化类型
                valueBinding,                                   // Value
                true);                                // allow write
        //todo
        Long firstKey = ((StoredSortedMap<Long, E>) queueMap).firstKey();
        Long lastKey = ((StoredSortedMap<Long, E>) queueMap).lastKey();

        headIndex=new AtomicLong(firstKey == null ? 0 : firstKey);
        tailIndex=new AtomicLong(lastKey==null?0:lastKey+1);
    }
    /**
     * 创建以及绑定数据库
     *
     * @param dbDir
     * @param dbName
     * @param valueClass
     * @throws DatabaseNotFoundException
     * @throws DatabaseExistsException
     * @throws DatabaseException
     * @throws IllegalArgumentException
     */
    private void createAndBindDatabase(String dbDir, String dbName,Class<E> valueClass) throws DatabaseNotFoundException,
            DatabaseExistsException,DatabaseException,IllegalArgumentException{
        File envFile = null;
        EnvironmentConfig envConfig = null;
        DatabaseConfig dbConfig = null;
        Database db=null;

        try {
            // 数据库位置
            envFile = new File(dbDir);

            // 数据库环境配置
            envConfig = new EnvironmentConfig();
            envConfig.setAllowCreate(true);
            //不支持事务
            envConfig.setTransactional(false);

            // 数据库配置
            dbConfig = new DatabaseConfig();
            dbConfig.setAllowCreate(true);
            dbConfig.setTransactional(false);
            //是否要延迟写
            dbConfig.setDeferredWrite(true);
//            dbConfig.setCacheMode(CacheMode.EVICT_LN);
//            envConfig.setConfigParam(EnvironmentConfig.CLEANER_MIN_UTILIZATION, "90");
//            envConfig.setConfigParam(EnvironmentConfig.CLEANER_EXPUNGE, "true");
            // 创建环境
            dbEnv = new BdbEnvironment(envFile, envConfig);
            // 打开数据库
            db = dbEnv.openDatabase(null, dbName, dbConfig);
            // 绑定数据库
            bindDatabase(db,valueClass,dbEnv.getClassCatalog());

        } catch (DatabaseNotFoundException e) {
            throw e;
        } catch (DatabaseExistsException e) {
            throw e;
        } catch (DatabaseException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw e;
        }


    }

    /**
     * 值遍历器
     */
    @Override
    public Iterator<E> iterator() {
        return queueMap.values().iterator();
    }
    /**
     * 大小
     */
    @Override
    public int size() {
        return (int)(tailIndex.get()-headIndex.get());
    }

    /**
     * 插入值
     */
    @Override
    public boolean offer(E e) {
        synchronized(tailIndex){
            queueMap.put(tailIndex.getAndIncrement(), e);// 从尾部插入
        }
        return true;
    }

    /**
     * 获取值,从头部获取
     */
    @Override
    public E peek() {
        synchronized(headIndex){
            if(peekItem!=null){
                return peekItem;
            }
            E headItem=null;
            while(headItem==null&&headIndex.get()<tailIndex.get()){ // 没有超出范围
                headItem=queueMap.get(headIndex.get());
                if(headItem!=null){
                    peekItem=headItem;
                    continue;
                }
                headIndex.incrementAndGet();    // 头部指针后移
            }
            return headItem;
        }
    }
    /**
     * 移出元素,移出头部元素
     */
    @Override
    public E poll() {
        synchronized(headIndex){
            E headItem=peek();
            if(headItem!=null){
                queueMap.remove(headIndex.getAndIncrement());
                peekItem=null;
                return headItem;
            }
        }
        return null;
    }

    public void sync(){
        synchronized (tailIndex) {
            synchronized (headIndex) {
                if(queueDb!=null){
                    queueDb.sync();
                }
                if (dbEnv != null){
                    dbEnv.cleanLog();
                }
            }
        }
    }

    /**
     * 关闭,也就是关闭所是用的BDB数据库但不关闭数据库环境
     */
    public void close(){
        try {
            if(queueDb!=null){
                queueDb.sync();
                queueDb.close();
            }
            if (dbEnv != null){
                dbEnv.cleanLog();
                dbEnv.close();
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        }
    }
    /**
     * 清理,会清空数据库,并且删掉数据库所在目录,慎用.如果想保留数据,请调用close()
     */
    @Override
    public void clear() {
        try {
            close();
            if(dbEnv!=null&&queueDb!=null){
                dbEnv.removeDatabase(null, dbName==null?queueDb.getDatabaseName():dbName);
                dbEnv.close();
            }
        } catch (DatabaseNotFoundException e) {
            e.printStackTrace();
        } catch (DatabaseException e) {
            e.printStackTrace();
        } finally{
            if(this.dbDir!=null){
                new File(this.dbDir).delete();
            }
        }
    }

    public class SyncTask extends Thread {
        @Override
        public void run() {
            try {
                while (!isStop) {
                    Thread.sleep(10);
                    sync();
                }
            } catch (InterruptedException e) {
            }
        }
    }
}
