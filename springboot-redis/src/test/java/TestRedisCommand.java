import com.alibaba.fastjson.JSONObject;
import com.xck.form.TestClass;
import com.xck.redis.RedisPool;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestRedisCommand {

    @Test
    public void testHmset() throws Exception {
        RedisPool pool = new RedisPool();

        String hashKey = "test:hmset:user";

        Map<String, String> valuesMap = new HashMap<String, String>();
        for (int j = 6000; j < 7000; j++) {
            for (int i = 1; i < 5000; i++) {
                String name = "xck" + i + j;
                valuesMap.put(name, JSONObject.toJSONString(new TestClass("徐成昆", 25)));
            }
            pool.hmset(hashKey, valuesMap);
            valuesMap.clear();
            Thread.sleep(5);
        }

//        System.out.println(pool.hmset(hashKey, valuesMap));
    }

    @Test
    public void testhDel() {
        RedisPool pool = new RedisPool();

        String hashKey = "test:hmset:user";

        List<String> list = new ArrayList<String>();
        for (int i = 0; i < 500; i++) {
            list.add("xck" + i);
        }

        System.out.println(pool.hdel(hashKey, list));
    }

    @Test
    public void testHdelPipeline() {
        RedisPool pool = new RedisPool();

        String hashKey = "test:hmset:user";

        List<String> list = new ArrayList<String>();
        for (int i = 0; i < 500; i++) {
            list.add("xck" + i);
        }

        List<Long> results = new ArrayList<Long>();
        for (Object o : pool.hdelPipeline(hashKey, list)) {
            results.add((Long) o);
        }
        System.out.println(results);
    }

    @Test
    public void testhmgetPipeline() {
        RedisPool pool = new RedisPool();

        //----
        String hashKey = "test:hmset:user";
        Map<String, String> valuesMap = new HashMap<String, String>();
        for (int i = 1; i < 250; i++) {
            String name = "xck" + i;
            valuesMap.put(name, JSONObject.toJSONString(new TestClass("徐成昆" + i, i)));
        }

        System.out.println(pool.hmset(hashKey, valuesMap));
        //---

        //----
        String hashKey1 = "test:hmset:user1";
        Map<String, String> valuesMap1 = new HashMap<String, String>();
        for (int i = 250; i < 500; i++) {
            String name = "xck" + i;
            valuesMap1.put(name, JSONObject.toJSONString(new TestClass("徐成昆" + i, i)));
        }

        System.out.println(pool.hmset(hashKey1, valuesMap1));
        //---

        Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
        map.put(hashKey, valuesMap);
        map.put(hashKey1, valuesMap1);
        List<Object> list = pool.hmgetPipeline(map);
        List<String> result = new ArrayList<String>();
        for (Object o : list) {
            if (o instanceof List) {
                result.addAll((List) o);
                System.out.println(o);
            } else {
                result.add((String) o);
            }
        }

        System.out.println(list.size());
        System.out.println(map);
        System.out.println(result.size());
    }

    @Test
    public void testsadd() {
        String key = "a";

        RedisPool pool = new RedisPool();
        pool.init();

        int count = 0;
        long logcount = 0;
        int batchprint = 0;
        List<String> list = new ArrayList<String>(500);
        for (long i = 5700000000L; i < 5700000000L + 10; i++) {
            ++count;
            ++logcount;
            list.add(i + "99");
            if (count % 10 == 0) {
                while (!pool.sadd(key, list)) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                list.clear();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (logcount % 500000 == 0) {
                System.out.println("数据量: " + logcount + ", 次数:" + (++batchprint));
            }
        }
    }

    @Test
    public void testsmember() {
        String key = "a";

        RedisPool pool = new RedisPool();
        pool.init();

        int count = 0;
        long logcount = 0;
        int batchprint = 0;
        List<String> list = new ArrayList<String>(500);
        for (long i = 5700000000L; i < 5700000000L + 2000000; i++) {
            ++count;
            ++logcount;
            list.add(i + "999");
            if (count % 1000 == 0) {
                while (!pool.sadd(key, list)) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                list.clear();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (logcount % 500000 == 0) {
                System.out.println("数据量: " + logcount + ", 次数:" + (++batchprint));
            }
        }
    }

    @Test
    public void testoutQueue() {
        String key = "a";

        RedisPool pool = new RedisPool();
        pool.init();

        pool.outQueueRPop(key, 10);
    }

    @Test
    public void byteDisplay() throws Exception{
        System.out.println(Long.MAX_VALUE);
        RedisPool pool = new RedisPool();
        pool.init();

//        for (byte i = -128; i < 127; i++) {
//            Jedis jedis = pool.getJedis();
//            jedis.sadd("a".getBytes(), new byte[]{i});
//            pool.returnJedis(jedis);
//        }
//
//        Jedis jedis = pool.getJedis();
//        jedis.sadd("a".getBytes(), new byte[]{127});

        for (int i = 10000; i < 20000; i++) {
            Jedis jedis = pool.getJedis();
            Pipeline pipeline = jedis.pipelined();
            for (int j = 1000; j < 2000; j++) {
//                pipeline.hset("td1", "123456789abc" + i + j, "【北京欢迎您哈】:" + (System.currentTimeMillis() / (24*3660000L)));
                pipeline.zadd("td1", (System.currentTimeMillis() / (24*3660000L)), "123456789abc" + i + j +  "【北京欢迎您哈】:");
            }
            pipeline.syncAndReturnAll();
            long l  = 999999999999999999L;
            System.out.println(Long.MAX_VALUE);

            pool.returnJedis(jedis);
            if (i % 100 == 0) {
                System.out.println(i * 1000);
            }
        }

    }

    @Test
    public void byteDisplayTest() {
        String c = "\\xfc`\\x9fg\\b\\x00";
        byte[] bytes = longStrToByteArr("345004965653");
        String str = RedisPool.bytesToString(bytes);
        System.out.println(str);
        System.out.println(c);
        System.out.println(str.equals(c));
    }

    @Test
    public void onlineblackMobileCacheSizeTest() {
        RedisPool pool = new RedisPool();
        pool.init();

        Jedis jedis = pool.getJedis();
        Pipeline pipeline = jedis.pipelined();
        int count = 0;
        for (int j = 0; j < 10; j++) {
            for (int i = 8000000; i < 8000000 + 1000000; i++) {
//                pipeline.sadd("test" + j, String.valueOf(i));
//              pipeline.zadd("test" + j, System.currentTimeMillis(), String.valueOf(i));
//                pipeline.hset("test" + j,  String.valueOf(i), "{\"age\":\""+System.currentTimeMillis()+"\"}");
                pipeline.hset("test" + j,  String.valueOf(i)
                        , Long.valueOf(Long.MAX_VALUE) + ":" + (System.currentTimeMillis()/3600000L));
                ++count;
                if (count > 1000) {
                    pipeline.sync();
                    count = 0;
                }
            }
        }
        if (count > 0) {
            pipeline.sync();
            count = 0;
        }
        pool.returnJedis(jedis);
    }

    @Test
    public void testNormalBlackString() throws Exception {
        RedisPool pool = new RedisPool();
        pool.initPwdSentinel();

        Jedis jedis = pool.getJedis();

        String isBlackLuaString = "local isHit"
                + " for i=1,table.getn(ARGV),1 do"
                + " isHit = redis.call('sismember', KEYS[i], ARGV[i])"
                + " if(isHit == 1) then return i end"
                + " end"
                + " return -1";

        try {
            System.out.println(jedis.scriptLoad(isBlackLuaString));;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断黑名单是否命中，命中了是否在时间范围内
     * @throws Exception
     */
    @Test
    public void testIsBlackLuaStringV3() throws Exception{
        RedisPool pool = new RedisPool();
        pool.initPwdSentinel();
//        pool.init();

        Jedis jedis = pool.getJedis();

//        jedis.hset("a".getBytes("UTF-8")
//                , new byte[]{1,2,3,4}
//                , "1983".getBytes("UTF-8"));
//
//        String isBlackLuaStringV3 =
//                "local minTime=tonumber(ARGV[1])" +
//                        " for i=1,table.getn(ARGV),1 do" +
//                        " local dateTime=redis.call('hget', KEYS[i], ARGV[i+1])" +
//                        " if dateTime then" +
//                        " if minTime == -1 then" +
//                        " return i" +
//                        " end" +
//                        " local dateTimeNum=tonumber(dateTime)" +
//                        " if dateTimeNum then " +
//                        " if dateTimeNum>=minTime then " +
//                        " return i" +
//                        " end" +
//                        " end" +
//                        " end" +
//                        " end" +
//                        " return -1";
//
//        List<byte[]> keys = new ArrayList<>();
//        keys.add("a".getBytes("UTF-8"));
//
//        List<byte[]> values = new ArrayList<>();
//        values.add("-1".getBytes("UTF-8"));
//        values.add(new byte[]{1,2,3,4});
//        Object o = jedis.eval(isBlackLuaStringV3.getBytes("UTF-8"), keys, values);
//        System.out.println(o);

        // 错误
//        String isBlackLuaStringV3 =
//                "local minTime=tonumber(ARGV[1])" +
//                        " for i=1,table.getn(ARGV),1 do" +
//                        " local dateTime=redis.call('hget', KEYS[i], ARGV[i+1])" +
//                        " if dateTime then" +
//                        " if minTime == -1 then" +
//                        " return i" +
//                        " end" +
//                        " local dateTimeNum=tonumber(dateTime)" +
//                        " if dateTimeNum then " +
//                        " if dateTimeNum>=minTime then " +
//                        " return i" +
//                        " end" +
//                        " end" +
//                        " end" +
//                        " end" +
//                        " return -1";
//
//        byte[] isBlackLuaStringV3BytesWrong = isBlackLuaStringV3.getBytes(StandardCharsets.UTF_8);

        //正确
        String luaScript =
                "local minTime=tonumber(ARGV[1])" +
                        " for i=1,table.getn(KEYS),1 do" +
                        " local dateTime=redis.call('hget', KEYS[i], ARGV[i+1])" +
                        " if dateTime then" +
                        " if minTime == -1 then" +
                        " return i" +
                        " end" +
                        " local dateTimeNum=tonumber(dateTime)" +
                        " if dateTimeNum then " +
                        " if dateTimeNum>=minTime then " +
                        " return i" +
                        " end" +
                        " end" +
                        " end" +
                        " end" +
                        " return -1";

        byte[] isBlackLuaStringV3Bytes = luaScript.getBytes(StandardCharsets.UTF_8);

//        List<byte[]> mobilesByte = new ArrayList<>();
//        mobilesByte.add(String.valueOf(-1).getBytes("utf-8"));
//
//        List<byte[]> keyList = new ArrayList<>();
//        mobilesByte.add(longStrToByteArr("51608258924"));
//        keyList.add("black:mobile:set:server:v3".getBytes("utf-8"));
//        long o = -1;
//        try {
//            o = (long) jedis.eval(isBlackLuaStringV3Bytes, keyList, mobilesByte);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println(o);

        List<byte[]> mobilesByte = new ArrayList<>();
//        mobilesByte.add(String.valueOf(-1).getBytes("utf-8"));
        mobilesByte.add(String.valueOf(19864).getBytes("utf-8"));
//        mobilesByte.add(String.valueOf(19866).getBytes("utf-8"));

        List<byte[]> keyList = new ArrayList<>();
        mobilesByte.add(longStrToByteArr("51608258924"));
//        mobilesByte.add(longStrToByteArr("51608258925"));
        keyList.add("black:mobile:set:server:v3".getBytes("utf-8"));
        try {
            Object o = jedis.eval(isBlackLuaStringV3Bytes, keyList, mobilesByte);
            System.out.println(o);
            System.out.println(new String((byte[] )o));
            System.out.println(jedis.eval(isBlackLuaStringV3Bytes, keyList, mobilesByte));;
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(-1);
    }

    @Test
    public void testUpdateOnlineBlackLuaStringV2() throws Exception{
        RedisPool pool = new RedisPool();
        pool.init();

        Jedis jedis = pool.getJedis();

//        jedis.hset("a"
//                , "-1"
//                , "4:1983");

        String luaScript = "local rediskey = KEYS[1];"
                + " local field = ARGV[1];"
                + " local newTime = ARGV[2];"
                + " local currentValue = redis.call('HGET', rediskey, field);"
                + " if currentValue then"
                + "    local index = string.find(currentValue, ':')"
                + "    if index then"
                + "        local valueBeforeColon = string.sub(currentValue, 1, index);"
                + "        local valueAfterColon = string.sub(currentValue, index + 1);"
                + "        currentValue = valueBeforeColon..':'..newTime;"
                + "        redis.call('HSET', rediskey, field, currentValue);"
                + "    end"
                + " end";

        List<String> keys = new ArrayList<>();
        keys.add("a");

        List<String> values = new ArrayList<>();
        values.add("-1");
        values.add("ffdsfdsfdsf");
        Object o = jedis.eval(luaScript, keys, values);
        System.out.println(o);
    }

    public static void main(String[] args) {
        System.out.println(String.valueOf(5f/100000f));
        System.out.println(Float.valueOf(0.00005f));

        long millisSecond = System.currentTimeMillis() + 1000;
        System.out.println(Math.abs((System.currentTimeMillis() - millisSecond)));
    }

    //目的只是为了可以用更少的字节表示整数，6字节可以表示整数14位，而号码本身可以10位，so，后续有变动需要兼容
    private static byte[] longStrToByteArr(String longStr) {
        long lv = Long.parseLong(longStr);

        byte[] b = new byte[6];
        for (int i = 0; i < b.length; i++) {
            int offset = i << 3;
            b[i] = (byte) ((lv >>> offset) & 0xff);
        }

        return b;
    }
}
