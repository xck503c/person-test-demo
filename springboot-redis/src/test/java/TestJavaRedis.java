import com.xck.form.NetSwitchedMobileInfo;
import com.xck.redis.RedisPool;
import redis.clients.jedis.Jedis;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

public class TestJavaRedis {

    public static void main(String[] args) throws Exception{
        RedisPool redisPool = new RedisPool();
        redisPool.init();

        Jedis jedis = redisPool.getJedis();

//        Map<String, NetSwitchedMobileInfo> map = new HashMap<String, NetSwitchedMobileInfo>();
//        for(long i=15720604553L; i<15720604553L+5000000; i++){
//            NetSwitchedMobileInfo info = new NetSwitchedMobileInfo();
//            info.setMobile(i+"");
//            info.setDest_td_type(2);
//            map.put(info.getMobile(), info);
//        }
//
//        System.out.println("写入");
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        ObjectOutputStream oos=new ObjectOutputStream(baos);
////        ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(
////                new File("/Users/xck/workDir/project/redis/xxx")));
//
//        oos.writeObject(map);
//
//        jedis.set("a".getBytes(), baos.toByteArray());
//
//        oos.close();
//
//        redisPool.returnJedis(jedis);
        Map<String, String> map = new HashMap<String, String>();
        map.put("ffff", "1");
        map.put("fffffdsfdsf", "2");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos=new ObjectOutputStream(baos);

        oos.writeObject(map);

        jedis.set("a".getBytes(), baos.toByteArray());

        oos.close();

        redisPool.returnJedis(jedis);
    }

    /**
     * 字节数组转16进制
     * @param bytes 需要转换的byte数组
     * @return  转换后的Hex字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if(hex.length() < 2){
                sb.append(0);
            }
            sb.append("0x"+hex);
        }
        return sb.toString();
    }


}
