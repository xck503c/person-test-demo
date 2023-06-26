import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.xck.bloomfilter.dmembf.LongBitMap;
import com.xck.bloomfilter.dmembf.LongBloomFilter;
import org.junit.Test;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLongArray;

public class TestGoogleLongBloomFilter {


    /**
     * 测试在内存中，google实现和自定义实现储存映射的数据一致，数据判断一致
     */
    @Test
    public void testGoogleAndMyselfEquals() throws Exception {
        long dataSize = 100000000;
        double errRate = 0.0001;
        int testDataSize = 10000000;

        BloomFilter<String> bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), dataSize, errRate);
        LongBloomFilter dLongBloomFilter = LongBloomFilter.createDirect(
                Funnels.stringFunnel(Charsets.UTF_8), dataSize, errRate
        );
        String[] s = new String[testDataSize];
        for (int i = 0; i < testDataSize; i++) {
            String data = UUID.randomUUID().toString() + System.currentTimeMillis();
            s[i] = data;
            bloomFilter.put(data);
            dLongBloomFilter.put(data);
        }

        System.out.println("初始化完成，开始比对");

        compareGoogleAndMyself(bloomFilter, dLongBloomFilter);

        System.out.println("内存映射储存数据一致, 开始比对数据判断");

        for (int i = 0; i < testDataSize; i++) {
            boolean left = bloomFilter.mightContain(s[i]);
            boolean right = dLongBloomFilter.mightContain(s[i]);
            if (left != right) {
                System.err.println("not equals index=" + i + ", left=" + left + ", right=" + right + ", data=" + s[i]);
                System.exit(-1);
            }
        }
    }

    public static void compareGoogleAndMyself(BloomFilter<String> bloomFilter, LongBloomFilter dLongBloomFilter) throws Exception {
        Field bitsField = BloomFilter.class.getDeclaredField("bits");
        bitsField.setAccessible(true);
        Object bitObj = bitsField.get(bloomFilter);
        Field dataField = bitObj.getClass().getDeclaredField("data");
        dataField.setAccessible(true);
        AtomicLongArray dataObj = (AtomicLongArray) dataField.get(bitObj);
        Field arrayField = dataObj.getClass().getDeclaredField("array");
        arrayField.setAccessible(true);
        long[] arrObj = (long[]) arrayField.get(dataObj);

        Field bitMapField = LongBloomFilter.class.getDeclaredField("longBitMap");
        bitMapField.setAccessible(true);
        LongBitMap longBitMap = (LongBitMap) bitMapField.get(dLongBloomFilter);

        for (int i = 0; i < arrObj.length; i++) {
            long left = arrObj[i];
            long right = longBitMap.getLongValue(i);
            if (left != right) {
                longBitMap.getLongValue(i);
                System.err.println("not equals index=" + i + ", left=" + left + ", right=" + right);
                System.exit(-1);
            }
        }
    }

    /**
     * 测试google bloomFilter写文件，再读到自定义实现是否兼容，数据判断是否一致
     *
     * @throws Exception
     */
    @Test
    public void testGoogleWriteAndMyselfRead() throws Exception {

        long dataSize = 100000000;
        double errRate = 0.0001;
        int testDataSize = 10000000;

        System.out.println("开始初始化");

        BloomFilter<String> bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), dataSize, errRate);
        String[] s = new String[testDataSize];
        for (int i = 0; i < testDataSize; i++) {
            String data = UUID.randomUUID().toString() + System.currentTimeMillis();
            s[i] = data;
            bloomFilter.put(data);
        }

        System.out.println("初始化完成，开始写文件");

        String path = "D:/testGoogleWriteAndMyselfRead.txt";
        DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(path));
        bloomFilter.writeTo(dataOutputStream);
        dataOutputStream.flush();
        dataOutputStream.close();

        System.out.println("写文件完成，开始读文件到自定义结构");

        FileInputStream fis = new FileInputStream(path);
        LongBloomFilter<String> dLongBloomFilter = LongBloomFilter
                .read2Direct(fis, Funnels.stringFunnel(Charsets.UTF_8));
        fis.close();

        compareGoogleAndMyself(bloomFilter, dLongBloomFilter);

        for (int i = 0; i < testDataSize; i++) {
            boolean left = bloomFilter.mightContain(s[i]);
            boolean right = dLongBloomFilter.mightContain(s[i]);
            if (left != right) {
                System.err.println("not equals index=" + i + ", left=" + left + ", right=" + right + ", data=" + s[i]);
                System.exit(-1);
            }
        }
    }

    @Test
    public void testDirectFree() throws Exception {
        long dataSize = 100000000;
        double errRate = 0.0001;
        LongBloomFilter dLongBloomFilter = LongBloomFilter.createDirect(Funnels.stringFunnel(Charsets.UTF_8), dataSize, errRate);
        System.out.println(dLongBloomFilter.bitMapSize()/8/1024/1024);
        dLongBloomFilter.doStop();
        System.out.println("sleep");
        Thread.sleep(20000);
    }

    /**
     * 生成文件布隆过滤器
     *
     * @throws Exception
     */
    @Test
    public void generateMobileBloomFile() throws Exception {

        long dataSize = 100000;
        double errRate = 0.0001;
        int testDataSize = 1000;

        System.out.println("开始初始化");

        long mobile = 15700000000L;
        BloomFilter<String> bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), dataSize, errRate);
        for (int i = 0; i < testDataSize; i++) {
            String m = (mobile + i) + "";
            bloomFilter.put(m);
        }

        System.out.println("初始化完成，开始写文件");

        String path = "D:/generateMobile.txt";
        DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(path));
        bloomFilter.writeTo(dataOutputStream);
        dataOutputStream.flush();
        dataOutputStream.close();
    }
}
