package com.xck.redisDistributeLock;

public class RedisLockWatchThread extends Thread implements WatchState{

    private volatile boolean state;

    private String redisKey;
    private RedisNoFairLockRAO redisNoFairLockRAO;
    private long timeout;
    private String taskId;

    public RedisLockWatchThread(RedisNoFairLockRAO redisNoFairLockRAO, String redisKey, long timeout, String taskId){
        setDaemon(true);
        setName("RedisLockWatchThread-" + redisKey);
        this.redisKey = redisKey;
        this.redisNoFairLockRAO = redisNoFairLockRAO;
        this.timeout = timeout;
        this.taskId = taskId;
    }

    @Override
    public void taskRunningState() {
        state = true;
        interrupt();
    }

    @Override
    public void lockWaitState() {
        state = false;
    }

    @Override
    public void run() {
        System.out.println("start task to watch redis lock");
        while (true){
            try {
                if(state){
                    long result = redisNoFairLockRAO.continuationOfLife(redisKey, taskId, timeout);
                    Thread.sleep(timeout/3);
                }else {
                    Thread.sleep(10000);
                }
            } catch (InterruptedException e) {
            }
        }
    }
}
