package com.xck.redis;

import com.xck.redisDistributeLock.RedisNoFairLock;
import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.*;

import java.util.*;

public class RedisPool {
    private String mode = "single";
    private JedisPool jedisPool = null;
    private JedisSentinelPool jedisSentinelPool = null;

    public void init(){
        if(jedisPool == null){
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxIdle(100);
            jedisPoolConfig.setMaxTotal(15);
            jedisPoolConfig.setMaxWaitMillis(10000);
            jedisPoolConfig.setTestOnBorrow(false);
            jedisPoolConfig.setMinIdle(100);

            jedisPool = new JedisPool(jedisPoolConfig, "127.0.0.1", 6379);
//            jedisPool.close();
        }
    }

    public void initPwd(){
        if(jedisPool == null){
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxIdle(60000);
            jedisPoolConfig.setMaxTotal(15);
            jedisPoolConfig.setMaxWaitMillis(10000);
            jedisPoolConfig.setTestOnBorrow(true);
            jedisPoolConfig.setMinIdle(60000);

            jedisPool = new JedisPool(jedisPoolConfig, "127.0.0.1", 8883, 15000, "123456");
        }
    }

    public void initPwdSentinel(){
        if(jedisSentinelPool == null){
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxIdle(60000);
            jedisPoolConfig.setMaxTotal(15);
            jedisPoolConfig.setMaxWaitMillis(10000);
            jedisPoolConfig.setTestOnBorrow(true);
            jedisPoolConfig.setMinIdle(60000);

            Set<String> ips = new HashSet<String>();
            ips.add("127.0.0.1:28881");
            ips.add("127.0.0.1:28882");
            ips.add("127.0.0.1:28883");

            jedisSentinelPool = new JedisSentinelPool("mymaster", ips, jedisPoolConfig, "123456");
            mode = "sentinel";
        }
    }

    public void close(){
        if ("single".equals(mode)) {
            if(jedisPool != null){
                jedisPool.close();
            }
        }else {
            if(jedisSentinelPool != null){
                jedisSentinelPool.close();
            }
        }
    }


    public Jedis getJedis(){
        if ("single".equals(mode)) {
            if(jedisPool == null) init();
            return jedisPool.getResource();
        }else {
            if(jedisSentinelPool == null) initPwdSentinel();
            return jedisSentinelPool.getResource();
        }
    }

    public void returnJedis(Jedis jedis){
        if(jedis != null){
            jedis.close();
        }
    }

    //返回ok，批量插入
    public String hmset(String hashKey, Map<String, String> valueMap){
        String result = "";
        Jedis jedis = null;
        try{
            jedis = getJedis();
            result = jedis.hmset(hashKey, valueMap);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            returnJedis(jedis);
        }
        return result;
    }

    //如果是批量删除，返回值只会返回一个数字，表示删除的个数，无法判断哪个删除成功了
    //所以如果要利用删除的返回值，就只能使用pipeline进行批量的单个删除
    public Long hdel(String hashKey, List<String> fieldValues){
        Long result = 0L;
        Jedis jedis = null;
        String[] fieldArr = new String[fieldValues.size()];
        for(int i=0; i<fieldValues.size(); i++){
            fieldArr[i] = fieldValues.get(i);
        }
        try{
            jedis = getJedis();
            result = jedis .hdel(hashKey, fieldArr);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            returnJedis(jedis);
        }
        return result;
    }

    //pipeline删除，对于删除失败的会返回0，和传入的字段对应，主要用于需要返回值的情况
    //返回的每个元素都是一个Long
    public List<Object> hdelPipeline(String hashKey, List<String> fieldValues){
        List<Object> results = null;
        Jedis jedis = null;
        try{
            jedis = getJedis();
            Pipeline pipeline = jedis.pipelined();
            for(int i=0; i<fieldValues.size(); i++){
                pipeline.hdel(hashKey, fieldValues.get(i));
            }
            results = pipeline.syncAndReturnAll();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            returnJedis(jedis);
        }
        return results;
    }

    //正常用pipeline比较好，但是可以看到这里为了测试塞入两个Map用于取出不同key的值
    //返回值是一个List<List>里面的每个元素都对应hmget的一个结果列表
    //当然也可以自己弄个结果集合利用Response<List<String>>
    public List<Object> hmgetPipeline(Map<String, Map<String, String>> fieldsMap){
        List<Object> results = null;
        Jedis jedis = null;
        try{
            List<Response<List<String>>> responses = new ArrayList<Response<List<String>>>();
            jedis = getJedis();
            Pipeline pipeline = jedis.pipelined();
            for(String key : fieldsMap.keySet()){
                Map<String, String> tmpMap = fieldsMap.get(key);
                String[] fields = new String[tmpMap.size()];
                int i=0;
                for(String field : tmpMap.keySet()){
                    fields[i++] = field;
                }
                Response<List<String>> response = pipeline.hmget(key, fields);
                responses.add(response);
            }
            results = pipeline.syncAndReturnAll();
//            System.out.println(responses.get(0).get());
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            returnJedis(jedis);
        }

        return results;
    }

    public void lpushMultiPipe(String listKey, List<String> list, int limit){
        Jedis jedis = null;
        try {
            if(limit < 4){
                limit = 4;
            }
            if(list.size()>limit){
                jedis = getJedis();
                Pipeline pipeline = jedis.pipelined();

                List<List<String>> allBatchList = new ArrayList<List<String>>();
                List<String> batchList = new ArrayList<String>();

                List<String> tmp = new ArrayList<String>(limit);
                for(int i=0; i<list.size(); i++){
                    tmp.add(list.get(i));
                    batchList.add(list.get(i));
                    if((i+1)%limit == 0){
                        pipeline.lpush(listKey, tmp.toArray(new String[tmp.size()]));
                        tmp.clear();
                        allBatchList.add(batchList);
                        batchList = new ArrayList<String>();
                    }
                }
                if(tmp.size()>0){
                    pipeline.lpush(listKey, tmp.toArray(new String[tmp.size()]));
                    tmp.clear();
                    allBatchList.add(batchList);
                }
                List<Object> results = pipeline.syncAndReturnAll();
                List<Object> deleteList = new ArrayList<Object>();
                for(int i=0; i<results.size(); i++){
                    Long r = (Long)results.get(i);
                    if(r > 0){
                        deleteList.add(allBatchList.get(i));
                    }
                }
                for(Object deleteO : deleteList){
                    allBatchList.remove(deleteO);
                }
                list.clear();
                for(List<String> l : allBatchList){
                    list.addAll(l);
                }
                if(list.size() > 0){
                    System.out.println(list.size() + " fail");
                }
            }else {
                String[] arr = new String[list.size()];
                for(int i=0; i<list.size(); i++){
                    arr[i] = list.get(i);
                }
                jedis = getJedis();
                jedis.lpush(listKey, arr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
    }

    public List<String> popMulitTransac(String listKey, long count){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            long len = jedis.llen(listKey);
            if(len <= 0) return new ArrayList<String>();
            else if(len < count) count = len;

            if(count > len){

            }

            Transaction transaction = jedis.multi();
            Response<List<String>> response = transaction.lrange(listKey, -count, -1);
            //保留0~-count-1
            transaction.ltrim(listKey, 0, -count-1);
            transaction.exec();
            return response.get();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return null;
    }

    private String script = "local elemNum = tonumber(ARGV[1]);"
            + "local vals = redis.call('lrange', KEYS[1], -elemNum, -1);"
            + "redis.call('ltrim', KEYS[1], 0, -elemNum-1);"
            + "return vals";
    public List<Object> outQueueRPop(String key, int size){
        List<Object> list = new ArrayList<>();

        List<String> keys = new ArrayList<>();
        keys.add(key);
        //这里不能做为values因为内部会当成Integer，序列化为对象，太坑了。
        List<String> args = new ArrayList<>();
        args.add(size+"");

        Jedis jedis = null;
        try{
            jedis = getJedis();
            List<Object> tmp = (List<Object>)jedis.eval(script, keys, args);
            System.out.println(tmp);
        }catch (Exception e){
            e.printStackTrace();
        }

        return list;
    }

    public void lpushMulit(String listKey, List<String> list){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            Pipeline pipeline = jedis.pipelined();
            for(int i=0; i<list.size(); i++){
                pipeline.lpush(listKey, list.get(i));
            }
            pipeline.sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
    }

    public void lpushMulitInBytes(String listKey, List<byte[]> list){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            Pipeline pipeline = jedis.pipelined();
            for(int i=0; i<list.size(); i++){
                pipeline.lpush(listKey.getBytes("UTF-8"), list.get(i));
            }
            pipeline.sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
    }

    //取出来会有null的情况，即便用了llen判断也是一样
    public List<Object> rpopMulit(String listKey, long count){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            Pipeline pipeline = jedis.pipelined();
//            Response<Long> l = pipeline.llen(listKey);
//            pipeline.sync();
//            long len = l.get();
//            if(len < 0) return new ArrayList<Object>();
//            else if(count > len){
//                count = len;
//            }
            for(int i=0; i<count; i++){
                pipeline.rpop(listKey);
            }
            return pipeline.syncAndReturnAll();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return null;
    }

    public long listLen(String listKey){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.llen(listKey);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return 0L;
    }

    public List<Object> listLens(List<String> queueNames){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            Pipeline pipeline = jedis.pipelined();
            for(String queueName : queueNames){
                pipeline.llen(queueName.getBytes());
            }
            List<Object> lenList = pipeline.syncAndReturnAll();
            return lenList;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return null;
    }

    //取出来会有null的情况，即便用了llen判断也是一样
    public boolean sadd(String key, List<String> list){
        Jedis jedis = null;
        boolean result = false;
        try {
            jedis = getJedis();
            Pipeline pipeline = jedis.pipelined();
            for(int i=0; i<list.size(); i++){
                pipeline.sadd(key.getBytes(), stringToLongByte(list.get(i)));
            }
            pipeline.sync();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return result;
    }

    public boolean sismember(String key, String member){
        Jedis jedis = null;
        boolean result = false;
        try {
            jedis = getJedis();
            jedis.sismember(key, member);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return result;
    }

    public boolean setnx(String key, String member){
        Jedis jedis = null;
        boolean result = false;
        try {
            jedis = getJedis();
            jedis.setnx(key, member);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return result;
    }

    /**
     * 根据情况将存储空间压缩
     * @param longStr
     * @return
     */
    public byte[] stringToLongByte(String longStr){
        long lv = Long.parseLong(longStr);

        byte[] b = new byte[5];
        for(int i=0; i<b.length; i++){
            b[i] = (byte)((lv & (0xff << i*8)) >>> (i*8));
        }
        return b;
//        return longStr.getBytes();
    }

    @RedisNoFairLock(lockName = "${lockKey}", timeout = 15000)
    public void inc(){
        Jedis jedis = null;
        int testCount = 0;
        try {
            jedis = getJedis();
            if (jedis != null) {
                String strValue = jedis.get("testCount");
                if(StringUtils.isBlank(strValue)){
                    jedis.set("testCount", "1");
                    testCount = 1;
                }else {
                    testCount = Integer.parseInt(strValue);
                    ++testCount;
                    jedis.set("testCount", testCount+"");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);

            System.out.println(String.format(
                    "线程id: %d, 自增值: %d", Thread.currentThread().getId(), testCount));
        }
    }

    //
//    public byte[]

    public static void main(String[] args) {

    }
}
