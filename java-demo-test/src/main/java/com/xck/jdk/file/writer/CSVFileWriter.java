package com.xck.jdk.file.writer;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ClassUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.core.annotation.Order;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * @author xuchengkun
 * @date 2021/11/25 14:38
 **/
public class CSVFileWriter extends ObjectFileWriter {

    private List<Object> list = new ArrayList<>(1000);
    private CSVPrinter csvPrinter;
    private String[] fieldArr;
    private Class clzz;

    public CSVFileWriter(Class c) {
        this.clzz = c;
        Field[] fields = ClassUtil.getDeclaredFields(c);
        TreeMap<Integer, String> fieldMap = new TreeMap<>();
        for (Field field : fields) {
            Integer order = AnnotationUtil.getAnnotationValue(field, Order.class);
            if (order == null) continue;
            fieldMap.put(order, field.getName());
        }
        this.fieldArr = new String[fieldMap.size()];
        int count = 0;
        for (String value : fieldMap.values()) {
            fieldArr[count] = value;
            ++count;
        }
    }

    @Override
    public Object start(Object input) throws Exception {
        CSVFormat formator = CSVFormat.DEFAULT.withRecordSeparator("\n");
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream((String) input), "UTF-8");
        this.csvPrinter = new CSVPrinter(osw, formator);
        this.csvPrinter.printRecord(fieldArr);
        return null;
    }

    @Override
    public Object deal(Object input) throws Exception {
        list.add(input);
        if (list.size() >= 1000) {
            write();
            list.clear();
        }
        return null;
    }

    /**
     * 为了保证顺序利用反射进行获取数据
     * @throws IOException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public void write() throws IOException, NoSuchFieldException, IllegalAccessException {
        for (Object o : list) {
            String[] values = new String[fieldArr.length];
            for (int i = 0; i < fieldArr.length; i++) {
                Field field = clzz.getDeclaredField(fieldArr[i]);
                field.setAccessible(true);
                values[i] = String.valueOf(field.get(o));
            }
            csvPrinter.printRecord(values);
        }
        csvPrinter.flush();
    }

    public void close() {
        try {
            if (list.size() > 0) {
                write();
                list.clear();
            }
            csvPrinter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        csvPrinter = null;
    }
}
