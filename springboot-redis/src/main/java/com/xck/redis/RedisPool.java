package com.xck.redis;

import com.alibaba.fastjson.JSONObject;
import com.xck.redisDistributeLock.RedisNoFairLock;
import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.*;

import java.util.*;

public class RedisPool {
    private String mode = "single";
    private JedisPool jedisPool = null;
    private JedisSentinelPool jedisSentinelPool = null;

    public void init() {
        if (jedisPool == null) {
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

    public void initPwd() {
        if (jedisPool == null) {
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxIdle(60000);
            jedisPoolConfig.setMaxTotal(15);
            jedisPoolConfig.setMaxWaitMillis(10000);
            jedisPoolConfig.setTestOnBorrow(true);
            jedisPoolConfig.setMinIdle(60000);

            jedisPool = new JedisPool(jedisPoolConfig, "127.0.0.1", 8883, 15000, "123456");
        }
    }

    public void initPwdSentinel() {
        if (jedisSentinelPool == null) {
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

    public void close() {
        if ("single".equals(mode)) {
            if (jedisPool != null) {
                jedisPool.close();
            }
        } else {
            if (jedisSentinelPool != null) {
                jedisSentinelPool.close();
            }
        }
    }


    public Jedis getJedis() {
        if ("single".equals(mode)) {
            if (jedisPool == null) init();
            return jedisPool.getResource();
        } else {
            if (jedisSentinelPool == null) initPwdSentinel();
            return jedisSentinelPool.getResource();
        }
    }

    public void returnJedis(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    //返回ok，批量插入
    public String hmset(String hashKey, Map<String, String> valueMap) {
        String result = "";
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.hmset(hashKey, valueMap);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return result;
    }

    //如果是批量删除，返回值只会返回一个数字，表示删除的个数，无法判断哪个删除成功了
    //所以如果要利用删除的返回值，就只能使用pipeline进行批量的单个删除
    public Long hdel(String hashKey, List<String> fieldValues) {
        Long result = 0L;
        Jedis jedis = null;
        String[] fieldArr = new String[fieldValues.size()];
        for (int i = 0; i < fieldValues.size(); i++) {
            fieldArr[i] = fieldValues.get(i);
        }
        try {
            jedis = getJedis();
            result = jedis.hdel(hashKey, fieldArr);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return result;
    }

    //pipeline删除，对于删除失败的会返回0，和传入的字段对应，主要用于需要返回值的情况
    //返回的每个元素都是一个Long
    public List<Object> hdelPipeline(String hashKey, List<String> fieldValues) {
        List<Object> results = null;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            Pipeline pipeline = jedis.pipelined();
            for (int i = 0; i < fieldValues.size(); i++) {
                pipeline.hdel(hashKey, fieldValues.get(i));
            }
            results = pipeline.syncAndReturnAll();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return results;
    }

    //正常用pipeline比较好，但是可以看到这里为了测试塞入两个Map用于取出不同key的值
    //返回值是一个List<List>里面的每个元素都对应hmget的一个结果列表
    //当然也可以自己弄个结果集合利用Response<List<String>>
    public List<Object> hmgetPipeline(Map<String, Map<String, String>> fieldsMap) {
        List<Object> results = null;
        Jedis jedis = null;
        try {
            List<Response<List<String>>> responses = new ArrayList<Response<List<String>>>();
            jedis = getJedis();
            Pipeline pipeline = jedis.pipelined();
            for (String key : fieldsMap.keySet()) {
                Map<String, String> tmpMap = fieldsMap.get(key);
                String[] fields = new String[tmpMap.size()];
                int i = 0;
                for (String field : tmpMap.keySet()) {
                    fields[i++] = field;
                }
                Response<List<String>> response = pipeline.hmget(key, fields);
                responses.add(response);
            }
            results = pipeline.syncAndReturnAll();
//            System.out.println(responses.get(0).get());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }

        return results;
    }

    public void lpushMultiPipe(String listKey, List<String> list, int limit) {
        Jedis jedis = null;
        try {
            if (limit < 4) {
                limit = 4;
            }
            if (list.size() > limit) {
                jedis = getJedis();
                Pipeline pipeline = jedis.pipelined();

                List<List<String>> allBatchList = new ArrayList<List<String>>();
                List<String> batchList = new ArrayList<String>();

                List<String> tmp = new ArrayList<String>(limit);
                for (int i = 0; i < list.size(); i++) {
                    tmp.add(list.get(i));
                    batchList.add(list.get(i));
                    if ((i + 1) % limit == 0) {
                        pipeline.lpush(listKey, tmp.toArray(new String[tmp.size()]));
                        tmp.clear();
                        allBatchList.add(batchList);
                        batchList = new ArrayList<String>();
                    }
                }
                if (tmp.size() > 0) {
                    pipeline.lpush(listKey, tmp.toArray(new String[tmp.size()]));
                    tmp.clear();
                    allBatchList.add(batchList);
                }
                List<Object> results = pipeline.syncAndReturnAll();
                List<Object> deleteList = new ArrayList<Object>();
                for (int i = 0; i < results.size(); i++) {
                    Long r = (Long) results.get(i);
                    if (r > 0) {
                        deleteList.add(allBatchList.get(i));
                    }
                }
                for (Object deleteO : deleteList) {
                    allBatchList.remove(deleteO);
                }
                list.clear();
                for (List<String> l : allBatchList) {
                    list.addAll(l);
                }
                if (list.size() > 0) {
                    System.out.println(list.size() + " fail");
                }
            } else {
                String[] arr = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
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

    public List<String> popMulitTransac(String listKey, long count) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            long len = jedis.llen(listKey);
            if (len <= 0) return new ArrayList<String>();
            else if (len < count) count = len;

            if (count > len) {

            }

            Transaction transaction = jedis.multi();
            Response<List<String>> response = transaction.lrange(listKey, -count, -1);
            //保留0~-count-1
            transaction.ltrim(listKey, 0, -count - 1);
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

    public List<Object> outQueueRPop(String key, int size) {
        List<Object> list = new ArrayList<>();

        List<String> keys = new ArrayList<>();
        keys.add(key);
        //这里不能做为values因为内部会当成Integer，序列化为对象，太坑了。
        List<String> args = new ArrayList<>();
        args.add(size + "");

        Jedis jedis = null;
        try {
            jedis = getJedis();
            List<Object> tmp = (List<Object>) jedis.eval(script, keys, args);
            return tmp;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }

        return list;
    }

    public void lpushMulit(String listKey, List<String> list) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            Pipeline pipeline = jedis.pipelined();
            for (int i = 0; i < list.size(); i++) {
                pipeline.lpush(listKey, list.get(i));
            }
            pipeline.sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
    }

    public void lpushMulitInBytes(String listKey, List<byte[]> list) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            Pipeline pipeline = jedis.pipelined();
            for (int i = 0; i < list.size(); i++) {
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
    public List<Object> rpopMulitSet(Map<String, Integer> scanMap) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            Pipeline pipeline = jedis.pipelined();
            for (String key : scanMap.keySet()) {
                Integer count = scanMap.get(key);
                for (int i = 0; i < count; i++) {
                    pipeline.rpop(key);
                }
            }
            List<Object> list = pipeline.syncAndReturnAll();
            if (list != null) {
                List<Object> tmp = new ArrayList<>();
                for (Object o : list) {
                    if (o == null) continue;
                    tmp.add(o);
                }
                return tmp;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return null;
    }

    public Map<String, List<Response<String>>> getMessageFromDataCenterAsync(Map<String, Integer> queue2CountMap) {
        Map<String, List<Response<String>>> resultMap = new HashMap<>();

        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis != null) {
                Pipeline pipeline = jedis.pipelined();

                for (String key : queue2CountMap.keySet()) {
                    List<Response<String>> list = resultMap.get(key);
                    if (list == null) {
                        list = new ArrayList<>();
                        resultMap.put(key, list);
                    }

                    int count = queue2CountMap.get(key);
                    for (int i = 0; i < count; i++) {
                        list.add(pipeline.rpop(key));
                    }
                }
                pipeline.sync();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return resultMap;
    }

    //取出来会有null的情况，即便用了llen判断也是一样
    public List<Object> rpopMulit(String listKey, long count) {
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
            for (int i = 0; i < count; i++) {
                pipeline.rpop(listKey);
            }
            List<Object> list = pipeline.syncAndReturnAll();
            if (list != null) {
                List<Object> tmp = new ArrayList<>();
                for (Object o : list) {
                    if (o == null) continue;
                    tmp.add(o);
                }
                return tmp;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
        return null;
    }

    public long listLen(String listKey) {
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

    public List<Object> listLens(List<String> queueNames) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            Pipeline pipeline = jedis.pipelined();
            for (String queueName : queueNames) {
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
    public boolean sadd(String key, List<String> list) {
        Jedis jedis = null;
        boolean result = false;
        try {
            jedis = getJedis();
            Pipeline pipeline = jedis.pipelined();
            for (int i = 0; i < list.size(); i++) {
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

    public boolean sismember(String key, String member) {
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

    public boolean sismember(byte[] key, byte[] member) {
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

    public boolean setnx(String key, String member) {
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
     *
     * @param longStr
     * @return
     */
    public byte[] stringToLongByte(String longStr) {
        long lv = Long.parseLong(longStr);

        byte[] b = new byte[5];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) ((lv & (0xff << i * 8)) >>> (i * 8));
        }
        return b;
//        return longStr.getBytes();
    }

    @RedisNoFairLock(lockName = "${lockKey}", timeout = 15000)
    public void inc() {
        Jedis jedis = null;
        int testCount = 0;
        try {
            jedis = getJedis();
            if (jedis != null) {
                String strValue = jedis.get("testCount");
                if (StringUtils.isBlank(strValue)) {
                    jedis.set("testCount", "1");
                    testCount = 1;
                } else {
                    testCount = Integer.parseInt(strValue);
                    ++testCount;
                    jedis.set("testCount", testCount + "");
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

    /**
     * 模糊匹配
     *
     * @param pattern key的正则表达式
     * @param count   每次扫描多少条记录，值越大消耗的时间越短，但会影响redis性能。建议设为一千到一万
     * @return 匹配的key集合
     */
    public Set<String> scan(String pattern, int count) {
        Set<String> set = new HashSet<String>();
        Jedis jedis = getJedis();
        try {
            String cursor = ScanParams.SCAN_POINTER_START;
            ScanParams scanParams = new ScanParams();
            scanParams.count(count);
            scanParams.match(pattern);
            do {
                ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                List<String> result = scanResult.getResult();
                if (result != null && result.size() > 0) {
                    set.addAll(result);
                }
                cursor = scanResult.getCursor();

            } while (!"0".equals(cursor));

            return set;
        } catch (Exception e) {
            e.printStackTrace();
            return set;
        } finally {
            returnJedis(jedis);
        }
    }

    public static String bytesToString(byte[] bytes) {
        String s = "1686738642.716967 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x80\"\n" +
                "1686738642.717640 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x81\"\n" +
                "1686738642.717774 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x82\"\n" +
                "1686738642.717936 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x83\"\n" +
                "1686738642.718050 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x84\"\n" +
                "1686738642.718248 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x85\"\n" +
                "1686738642.718377 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x86\"\n" +
                "1686738642.718488 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x87\"\n" +
                "1686738642.718598 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x88\"\n" +
                "1686738642.718693 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x89\"\n" +
                "1686738642.718781 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x8a\"\n" +
                "1686738642.718870 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x8b\"\n" +
                "1686738642.718956 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x8c\"\n" +
                "1686738642.719044 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x8d\"\n" +
                "1686738642.719155 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x8e\"\n" +
                "1686738642.719262 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x8f\"\n" +
                "1686738642.719419 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x90\"\n" +
                "1686738642.719515 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x91\"\n" +
                "1686738642.719609 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x92\"\n" +
                "1686738642.719700 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x93\"\n" +
                "1686738642.719790 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x94\"\n" +
                "1686738642.719876 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x95\"\n" +
                "1686738642.719964 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x96\"\n" +
                "1686738642.720051 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x97\"\n" +
                "1686738642.720136 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x98\"\n" +
                "1686738642.720222 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x99\"\n" +
                "1686738642.720409 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x9a\"\n" +
                "1686738642.720675 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x9b\"\n" +
                "1686738642.720839 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x9c\"\n" +
                "1686738642.721017 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x9d\"\n" +
                "1686738642.721137 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x9e\"\n" +
                "1686738642.721251 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x9f\"\n" +
                "1686738642.721360 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xa0\"\n" +
                "1686738642.721593 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xa1\"\n" +
                "1686738642.721701 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xa2\"\n" +
                "1686738642.721792 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xa3\"\n" +
                "1686738642.721901 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xa4\"\n" +
                "1686738642.722016 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xa5\"\n" +
                "1686738642.722126 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xa6\"\n" +
                "1686738642.722220 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xa7\"\n" +
                "1686738642.722313 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xa8\"\n" +
                "1686738642.722400 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xa9\"\n" +
                "1686738642.722493 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xaa\"\n" +
                "1686738642.722601 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xab\"\n" +
                "1686738642.722704 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xac\"\n" +
                "1686738642.722796 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xad\"\n" +
                "1686738642.722886 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xae\"\n" +
                "1686738642.722970 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xaf\"\n" +
                "1686738642.723058 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xb0\"\n" +
                "1686738642.723144 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xb1\"\n" +
                "1686738642.723234 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xb2\"\n" +
                "1686738642.723352 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xb3\"\n" +
                "1686738642.723466 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xb4\"\n" +
                "1686738642.723572 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xb5\"\n" +
                "1686738642.723671 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xb6\"\n" +
                "1686738642.723793 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xb7\"\n" +
                "1686738642.723900 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xb8\"\n" +
                "1686738642.724009 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xb9\"\n" +
                "1686738642.724101 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xba\"\n" +
                "1686738642.724194 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xbb\"\n" +
                "1686738642.724285 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xbc\"\n" +
                "1686738642.724391 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xbd\"\n" +
                "1686738642.724480 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xbe\"\n" +
                "1686738642.724599 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xbf\"\n" +
                "1686738642.724714 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xc0\"\n" +
                "1686738642.724814 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xc1\"\n" +
                "1686738642.724905 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xc2\"\n" +
                "1686738642.724995 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xc3\"\n" +
                "1686738642.725081 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xc4\"\n" +
                "1686738642.725168 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xc5\"\n" +
                "1686738642.725276 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xc6\"\n" +
                "1686738642.725512 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xc7\"\n" +
                "1686738642.725647 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xc8\"\n" +
                "1686738642.725769 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xc9\"\n" +
                "1686738642.725888 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xca\"\n" +
                "1686738642.725996 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xcb\"\n" +
                "1686738642.726103 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xcc\"\n" +
                "1686738642.726206 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xcd\"\n" +
                "1686738642.726309 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xce\"\n" +
                "1686738642.726434 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xcf\"\n" +
                "1686738642.726549 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xd0\"\n" +
                "1686738642.726658 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xd1\"\n" +
                "1686738642.726770 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xd2\"\n" +
                "1686738642.726873 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xd3\"\n" +
                "1686738642.726978 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xd4\"\n" +
                "1686738642.727089 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xd5\"\n" +
                "1686738642.727193 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xd6\"\n" +
                "1686738642.727302 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xd7\"\n" +
                "1686738642.727408 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xd8\"\n" +
                "1686738642.727512 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xd9\"\n" +
                "1686738642.727617 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xda\"\n" +
                "1686738642.727729 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xdb\"\n" +
                "1686738642.727828 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xdc\"\n" +
                "1686738642.727931 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xdd\"\n" +
                "1686738642.728018 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xde\"\n" +
                "1686738642.728119 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xdf\"\n" +
                "1686738642.728224 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xe0\"\n" +
                "1686738642.728409 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xe1\"\n" +
                "1686738642.728519 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xe2\"\n" +
                "1686738642.728960 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xe3\"\n" +
                "1686738642.729081 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xe4\"\n" +
                "1686738642.729224 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xe5\"\n" +
                "1686738642.729498 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xe6\"\n" +
                "1686738642.729614 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xe7\"\n" +
                "1686738642.729705 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xe8\"\n" +
                "1686738642.729791 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xe9\"\n" +
                "1686738642.729873 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xea\"\n" +
                "1686738642.729954 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xeb\"\n" +
                "1686738642.730034 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xec\"\n" +
                "1686738642.730111 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xed\"\n" +
                "1686738642.730189 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xee\"\n" +
                "1686738642.730286 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xef\"\n" +
                "1686738642.730389 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xf0\"\n" +
                "1686738642.730487 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xf1\"\n" +
                "1686738642.730611 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xf2\"\n" +
                "1686738642.730720 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xf3\"\n" +
                "1686738642.730826 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xf4\"\n" +
                "1686738642.730923 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xf5\"\n" +
                "1686738642.731008 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xf6\"\n" +
                "1686738642.731090 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xf7\"\n" +
                "1686738642.731175 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xf8\"\n" +
                "1686738642.731256 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xf9\"\n" +
                "1686738642.731383 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xfa\"\n" +
                "1686738642.731485 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xfb\"\n" +
                "1686738642.731590 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xfc\"\n" +
                "1686738642.731705 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xfd\"\n" +
                "1686738642.731839 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xfe\"\n" +
                "1686738642.731978 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\xff\"\n" +
                "1686738642.732108 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x00\"\n" +
                "1686738642.732211 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x01\"\n" +
                "1686738642.732372 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x02\"\n" +
                "1686738642.732480 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x03\"\n" +
                "1686738642.732584 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x04\"\n" +
                "1686738642.732687 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x05\"\n" +
                "1686738642.732779 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x06\"\n" +
                "1686738642.732864 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\a\"\n" +
                "1686738642.732950 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\b\"\n" +
                "1686738642.733031 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\t\"\n" +
                "1686738642.733112 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\n\"\n" +
                "1686738642.733189 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x0b\"\n" +
                "1686738642.733270 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x0c\"\n" +
                "1686738642.733351 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\r\"\n" +
                "1686738642.733430 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x0e\"\n" +
                "1686738642.733509 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x0f\"\n" +
                "1686738642.733589 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x10\"\n" +
                "1686738642.733667 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x11\"\n" +
                "1686738642.733746 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x12\"\n" +
                "1686738642.733827 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x13\"\n" +
                "1686738642.733902 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x14\"\n" +
                "1686738642.733980 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x15\"\n" +
                "1686738642.734057 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x16\"\n" +
                "1686738642.734196 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x17\"\n" +
                "1686738642.734322 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x18\"\n" +
                "1686738642.734427 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x19\"\n" +
                "1686738642.734523 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x1a\"\n" +
                "1686738642.734623 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x1b\"\n" +
                "1686738642.734703 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x1c\"\n" +
                "1686738642.734782 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x1d\"\n" +
                "1686738642.734860 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x1e\"\n" +
                "1686738642.734940 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\x1f\"\n" +
                "1686738642.735019 [0 127.0.0.1:50058] \"SADD\" \"a\" \" \"\n" +
                "1686738642.735099 [0 127.0.0.1:50058] \"SADD\" \"a\" \"!\"\n" +
                "1686738642.735179 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\\"\"\n" +
                "1686738642.735260 [0 127.0.0.1:50058] \"SADD\" \"a\" \"#\"\n" +
                "1686738642.735344 [0 127.0.0.1:50058] \"SADD\" \"a\" \"$\"\n" +
                "1686738642.735421 [0 127.0.0.1:50058] \"SADD\" \"a\" \"%\"\n" +
                "1686738642.735501 [0 127.0.0.1:50058] \"SADD\" \"a\" \"&\"\n" +
                "1686738642.735581 [0 127.0.0.1:50058] \"SADD\" \"a\" \"'\"\n" +
                "1686738642.735658 [0 127.0.0.1:50058] \"SADD\" \"a\" \"(\"\n" +
                "1686738642.735737 [0 127.0.0.1:50058] \"SADD\" \"a\" \")\"\n" +
                "1686738642.735849 [0 127.0.0.1:50058] \"SADD\" \"a\" \"*\"\n" +
                "1686738642.735958 [0 127.0.0.1:50058] \"SADD\" \"a\" \"+\"\n" +
                "1686738642.736062 [0 127.0.0.1:50058] \"SADD\" \"a\" \",\"\n" +
                "1686738642.736180 [0 127.0.0.1:50058] \"SADD\" \"a\" \"-\"\n" +
                "1686738642.736343 [0 127.0.0.1:50058] \"SADD\" \"a\" \".\"\n" +
                "1686738642.736810 [0 127.0.0.1:50058] \"SADD\" \"a\" \"/\"\n" +
                "1686738642.737432 [0 127.0.0.1:50058] \"SADD\" \"a\" \"0\"\n" +
                "1686738642.737607 [0 127.0.0.1:50058] \"SADD\" \"a\" \"1\"\n" +
                "1686738642.737718 [0 127.0.0.1:50058] \"SADD\" \"a\" \"2\"\n" +
                "1686738642.737828 [0 127.0.0.1:50058] \"SADD\" \"a\" \"3\"\n" +
                "1686738642.737935 [0 127.0.0.1:50058] \"SADD\" \"a\" \"4\"\n" +
                "1686738642.738036 [0 127.0.0.1:50058] \"SADD\" \"a\" \"5\"\n" +
                "1686738642.738137 [0 127.0.0.1:50058] \"SADD\" \"a\" \"6\"\n" +
                "1686738642.738262 [0 127.0.0.1:50058] \"SADD\" \"a\" \"7\"\n" +
                "1686738642.738364 [0 127.0.0.1:50058] \"SADD\" \"a\" \"8\"\n" +
                "1686738642.738448 [0 127.0.0.1:50058] \"SADD\" \"a\" \"9\"\n" +
                "1686738642.738530 [0 127.0.0.1:50058] \"SADD\" \"a\" \":\"\n" +
                "1686738642.738621 [0 127.0.0.1:50058] \"SADD\" \"a\" \";\"\n" +
                "1686738642.738716 [0 127.0.0.1:50058] \"SADD\" \"a\" \"<\"\n" +
                "1686738642.738813 [0 127.0.0.1:50058] \"SADD\" \"a\" \"=\"\n" +
                "1686738642.738911 [0 127.0.0.1:50058] \"SADD\" \"a\" \">\"\n" +
                "1686738642.739019 [0 127.0.0.1:50058] \"SADD\" \"a\" \"?\"\n" +
                "1686738642.739109 [0 127.0.0.1:50058] \"SADD\" \"a\" \"@\"\n" +
                "1686738642.739192 [0 127.0.0.1:50058] \"SADD\" \"a\" \"A\"\n" +
                "1686738642.739277 [0 127.0.0.1:50058] \"SADD\" \"a\" \"B\"\n" +
                "1686738642.739354 [0 127.0.0.1:50058] \"SADD\" \"a\" \"C\"\n" +
                "1686738642.739435 [0 127.0.0.1:50058] \"SADD\" \"a\" \"D\"\n" +
                "1686738642.739516 [0 127.0.0.1:50058] \"SADD\" \"a\" \"E\"\n" +
                "1686738642.739597 [0 127.0.0.1:50058] \"SADD\" \"a\" \"F\"\n" +
                "1686738642.739678 [0 127.0.0.1:50058] \"SADD\" \"a\" \"G\"\n" +
                "1686738642.739783 [0 127.0.0.1:50058] \"SADD\" \"a\" \"H\"\n" +
                "1686738642.739867 [0 127.0.0.1:50058] \"SADD\" \"a\" \"I\"\n" +
                "1686738642.739947 [0 127.0.0.1:50058] \"SADD\" \"a\" \"J\"\n" +
                "1686738642.740029 [0 127.0.0.1:50058] \"SADD\" \"a\" \"K\"\n" +
                "1686738642.740114 [0 127.0.0.1:50058] \"SADD\" \"a\" \"L\"\n" +
                "1686738642.740195 [0 127.0.0.1:50058] \"SADD\" \"a\" \"M\"\n" +
                "1686738642.740299 [0 127.0.0.1:50058] \"SADD\" \"a\" \"N\"\n" +
                "1686738642.740401 [0 127.0.0.1:50058] \"SADD\" \"a\" \"O\"\n" +
                "1686738642.740489 [0 127.0.0.1:50058] \"SADD\" \"a\" \"P\"\n" +
                "1686738642.740579 [0 127.0.0.1:50058] \"SADD\" \"a\" \"Q\"\n" +
                "1686738642.740660 [0 127.0.0.1:50058] \"SADD\" \"a\" \"R\"\n" +
                "1686738642.740745 [0 127.0.0.1:50058] \"SADD\" \"a\" \"S\"\n" +
                "1686738642.740841 [0 127.0.0.1:50058] \"SADD\" \"a\" \"T\"\n" +
                "1686738642.740927 [0 127.0.0.1:50058] \"SADD\" \"a\" \"U\"\n" +
                "1686738642.741015 [0 127.0.0.1:50058] \"SADD\" \"a\" \"V\"\n" +
                "1686738642.741098 [0 127.0.0.1:50058] \"SADD\" \"a\" \"W\"\n" +
                "1686738642.741174 [0 127.0.0.1:50058] \"SADD\" \"a\" \"X\"\n" +
                "1686738642.741268 [0 127.0.0.1:50058] \"SADD\" \"a\" \"Y\"\n" +
                "1686738642.741365 [0 127.0.0.1:50058] \"SADD\" \"a\" \"Z\"\n" +
                "1686738642.741461 [0 127.0.0.1:50058] \"SADD\" \"a\" \"[\"\n" +
                "1686738642.741560 [0 127.0.0.1:50058] \"SADD\" \"a\" \"\\\\\"\n" +
                "1686738642.741645 [0 127.0.0.1:50058] \"SADD\" \"a\" \"]\"\n" +
                "1686738642.741724 [0 127.0.0.1:50058] \"SADD\" \"a\" \"^\"\n" +
                "1686738642.741800 [0 127.0.0.1:50058] \"SADD\" \"a\" \"_\"\n" +
                "1686738642.741887 [0 127.0.0.1:50058] \"SADD\" \"a\" \"`\"\n" +
                "1686738642.741970 [0 127.0.0.1:50058] \"SADD\" \"a\" \"a\"\n" +
                "1686738642.742047 [0 127.0.0.1:50058] \"SADD\" \"a\" \"b\"\n" +
                "1686738642.742133 [0 127.0.0.1:50058] \"SADD\" \"a\" \"c\"\n" +
                "1686738642.742217 [0 127.0.0.1:50058] \"SADD\" \"a\" \"d\"\n" +
                "1686738642.742319 [0 127.0.0.1:50058] \"SADD\" \"a\" \"e\"\n" +
                "1686738642.742416 [0 127.0.0.1:50058] \"SADD\" \"a\" \"f\"\n" +
                "1686738642.742512 [0 127.0.0.1:50058] \"SADD\" \"a\" \"g\"\n" +
                "1686738642.742616 [0 127.0.0.1:50058] \"SADD\" \"a\" \"h\"\n" +
                "1686738642.742717 [0 127.0.0.1:50058] \"SADD\" \"a\" \"i\"\n" +
                "1686738642.742813 [0 127.0.0.1:50058] \"SADD\" \"a\" \"j\"\n" +
                "1686738642.742909 [0 127.0.0.1:50058] \"SADD\" \"a\" \"k\"\n" +
                "1686738642.743007 [0 127.0.0.1:50058] \"SADD\" \"a\" \"l\"\n" +
                "1686738642.743099 [0 127.0.0.1:50058] \"SADD\" \"a\" \"m\"\n" +
                "1686738642.743194 [0 127.0.0.1:50058] \"SADD\" \"a\" \"n\"\n" +
                "1686738642.743333 [0 127.0.0.1:50058] \"SADD\" \"a\" \"o\"\n" +
                "1686738642.743431 [0 127.0.0.1:50058] \"SADD\" \"a\" \"p\"\n" +
                "1686738642.743566 [0 127.0.0.1:50058] \"SADD\" \"a\" \"q\"\n" +
                "1686738642.743678 [0 127.0.0.1:50058] \"SADD\" \"a\" \"r\"\n" +
                "1686738642.743812 [0 127.0.0.1:50058] \"SADD\" \"a\" \"s\"\n" +
                "1686738642.743930 [0 127.0.0.1:50058] \"SADD\" \"a\" \"t\"\n" +
                "1686738642.744033 [0 127.0.0.1:50058] \"SADD\" \"a\" \"u\"\n" +
                "1686738642.744139 [0 127.0.0.1:50058] \"SADD\" \"a\" \"v\"\n" +
                "1686738642.744259 [0 127.0.0.1:50058] \"SADD\" \"a\" \"w\"\n" +
                "1686738642.744381 [0 127.0.0.1:50058] \"SADD\" \"a\" \"x\"\n" +
                "1686738642.744490 [0 127.0.0.1:50058] \"SADD\" \"a\" \"y\"\n" +
                "1686738642.744591 [0 127.0.0.1:50058] \"SADD\" \"a\" \"z\"\n" +
                "1686738642.744695 [0 127.0.0.1:50058] \"SADD\" \"a\" \"{\"\n" +
                "1686738642.744798 [0 127.0.0.1:50058] \"SADD\" \"a\" \"|\"\n" +
                "1686738642.744943 [0 127.0.0.1:50058] \"SADD\" \"a\" \"}\"\n" +
                "1686738642.745080 [0 127.0.0.1:50058] \"SADD\" \"a\" \"~\"\n" +
                "1686883757.366445 [0 127.0.0.1:49328] \"SADD\" \"a\" \"\\x7f\"";

        String tmp = "1686738642.723234 [0 127.0.0.1:50058] \"SADD\" \"a\" ";
        String[] arr = s.split("\\n");
        Map<Byte, String> byteStringMap = new HashMap<>(256);
        int index = -128;
        for (String item : arr) {
            String lastItemStr = item.substring(tmp.length());
            lastItemStr = lastItemStr.substring(1, lastItemStr.length() - 1);
            int i = index++;
            byteStringMap.put((byte) i, lastItemStr);
            System.out.println(i + "==" + lastItemStr);
        }

        StringBuilder sb = new StringBuilder();
        for (byte paramByte : bytes) {
            sb.append(byteStringMap.get(paramByte));
        }

        return sb.toString();
    }

    public static void main(String[] args) {

    }
}
