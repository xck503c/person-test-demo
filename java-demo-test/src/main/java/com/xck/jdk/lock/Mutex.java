package com.xck.jdk.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class Mutex implements Lock {

    // 真正同步类的实现都依赖继承于AQS的自定义同步器！
    private final Sync sync;

    public Mutex() {
        this.sync = new NoFairSync();
    }

    public Mutex(boolean isFair) {
        this.sync = isFair ? new FairSync() : new NoFairSync();
    }

    private abstract class Sync extends AbstractQueuedSynchronizer{

        boolean tryLock(){
            return tryAcquire(1);
        }

        @Override
        protected boolean tryRelease(int arg) {
            if(getState() == 0){
                return false; //如果中断异常
            }
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        final ConditionObject newCondition() {
            return new ConditionObject();
        }
    }

    private class NoFairSync extends Sync{
        @Override
        protected boolean tryAcquire(int arg) {
            //只有cas成功变为1才表示争夺资源成功
            if(compareAndSetState(0, 1)){
                //设置独占线程
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }
    }

    private class FairSync extends Sync{
        @Override
        protected boolean tryAcquire(int arg) {
            int c = getState();
            //hasQueuedPredecessors是AQS封装的用来判断是否还有线程在排队
            if(c == 0 && !hasQueuedPredecessors() && compareAndSetState(0, 1)){
                //设置独占线程
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }
    }

    @Override
    public void lock() {
        sync.acquire(1);
    }

    @Override
    public boolean tryLock() {
        return sync.tryLock();
    }

    @Override
    public void unlock() {
        sync.release(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        return;
    }

    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }

    public boolean tryLock(long timeout, TimeUnit unit)
            throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(timeout));
    }

    public static void main(String[] args) {
        System.out.println(332 & (64|8));
    }
}
