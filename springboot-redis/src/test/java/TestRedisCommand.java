import com.alibaba.fastjson.JSONObject;
import com.xck.form.TestClass;
import com.xck.redis.RedisPool;
import org.junit.Test;
import redis.clients.jedis.Jedis;

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
    public void byteDisplay() {
        RedisPool pool = new RedisPool();
        pool.init();

        for (byte i = -128; i < 127; i++) {
            Jedis jedis = pool.getJedis();
            jedis.sadd("a".getBytes(), new byte[]{i});
            pool.returnJedis(jedis);
        }

        Jedis jedis = pool.getJedis();
        jedis.sadd("a".getBytes(), new byte[]{127});
        pool.returnJedis(jedis);
    }

    @Test
    public void byteDisplayTest() {
        String c = "\\xfc`\\x9fg\\b\\x00";
        byte[] bytes = longStrToByteArr("36098236668");
        String str = RedisPool.bytesToString(bytes);
        System.out.println(str);
        System.out.println(c);
        System.out.println(str.equals(c));
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
