package redisson;

import com.xck.RunMain;
import com.xck.redis.JdkObjValueStringCodec;
import com.xck.redis.RedisPool;
import com.xck.redis.RedissonPool;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RMap;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RunMain.class)
public class CommandTest {

    @Autowired
    public RedissonPool redissonPool;

    /**
     * jdk的序列化和redisson默认的反序列化不兼容
     * ==> java.io.IOException: java.lang.RuntimeException: unknown object tag -84
     * @throws Exception
     */
    @Test
    public void putJdkObjAndGetBucket() throws Exception{
        String key = "user:xck01:name";

        RedisPool redisPool = new RedisPool();
        redisPool.init();
        Jedis jedis = redisPool.getJedis();
        redisPool.returnJedis(jedis);

        String s = "xck";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ObjectOutputStream(baos).writeObject(s);
        byte[] t = baos.toByteArray();
        jedis.set(key.getBytes(), t);

        RBucket<Object> result = redissonPool.getClient().getBucket(key);
        System.out.println(result.get());
    }

    @Test
    public void scan(){
        Set<String> keys = new HashSet<>();
        RKeys rKeys = redissonPool.getClient().getKeys();
        Iterator<String> it = rKeys.getKeysByPattern("q1:*", 1).iterator();
        while (it.hasNext()){
            String key = it.next();
            keys.add(key);
            System.out.println(key);
        }
        System.out.println(keys);
    }

    /**
     * 测试hscan，key-value为普通的String类型，非对象
     * ==> 需要灵活指定编码器
     */
    @Test
    public void hscanMapString(){
        String key = "user:xck01:info";

        RedisPool redisPool = new RedisPool();
        redisPool.init();
        Jedis jedis = redisPool.getJedis();
        Map<String, String> map = new HashMap<>();
        map.put("name", "xck");
        map.put("age", "1");
        jedis.hmset(key, map);
        redisPool.returnJedis(jedis);

        RMap rKeys = redissonPool.getClient().getMap(key, JdkObjValueStringCodec.INSTANCE);

//        Iterator<Map.Entry<String, String>> it = rKeys.entrySet("*", 1).iterator();
        Iterator<Map.Entry<String, String>> it = rKeys.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, String> entry = it.next();
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
    }

    /**
     * 测试队列的出入队
     */
    @Test
    public void enOutQueue() throws Exception{
        String queueName = "msgQ1";
        int getSize = 1000;

        List<Integer> list = new ArrayList<>();
        for(int i=0; i<10; i++){
            list.add(i);
        }
        boolean enResult = redissonPool.enQueue(queueName, list);
        Assert.assertEquals(enResult, true);
        Assert.assertEquals(redissonPool.queueSize(queueName), 10);

        list.clear();

        System.out.println(redissonPool.outQueue(queueName, getSize));

        Assert.assertEquals(redissonPool.queueSize(queueName), 0);

        Thread.sleep(10000);
    }

    /**
     * 测试队列的出入队
     */
    @Test
    public void enOutQueueMulti() throws Exception{
        String queueName = "msgQ1";
        int getSize = 1000;

        List<Integer> list = new ArrayList<>();
        for(int i=0; i<10; i++){
            list.add(i);
        }
        boolean enResult = redissonPool.enQueue(queueName, list);
        Assert.assertEquals(enResult, true);
        Assert.assertEquals(redissonPool.queueSize(queueName), 10);

        list.clear();

        System.out.println(redissonPool.outQueue(queueName, getSize));

        Assert.assertEquals(redissonPool.queueSize(queueName), 0);

        Thread.sleep(10000);
    }

    public class Sms implements Serializable {

        long timeStamp;
        String mobile = "";
        String content = "";

        public Sms(String mobile, String content) {
            this.timeStamp = System.currentTimeMillis();
            this.mobile = mobile;
            this.content = content;
        }

        @Override
        public String toString() {
            return "{" +
                    "timeStamp=" + timeStamp +
                    ", mobile='" + mobile + '\'' +
                    ", content='" + content + '\'' +
                    '}';
        }
    }
}
