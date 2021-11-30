package com.xck.jdk.file.writer;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.xck.jdk.file.pipe.Pipe;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * @author xuchengkun
 * @date 2021/11/25 14:38
 **/
public class ExcelFileWriter extends ObjectFileWriter {

    private List<Object> list = new ArrayList<>();
    private ExcelWriter excelWriter;
    private TreeMap<Integer, String> fieldMap;

    public ExcelFileWriter(Class c) {
        Field[] fields = ClassUtil.getDeclaredFields(c);
        fieldMap = new TreeMap<>();
        for (Field field : fields) {
            Integer order = AnnotationUtil.getAnnotationValue(field, Order.class);
            if (order == null) continue;
            fieldMap.put(order, field.getName());
        }
    }

    @Override
    public Object start(Object input) throws Exception {
        this.excelWriter = new ExcelWriter((String) input);
        for (String value : fieldMap.values()) {
            excelWriter.addHeaderAlias(value, value);
        }
        return input;
    }

    @Override
    public Object deal(Object input) {
        list.add(input);
        if (list.size() >= 1000) {
            excelWriter.write(list);
            list.clear();
        }
        return null;
    }

    public void close() {
        if (list.size() > 0) {
            excelWriter.write(list);
            list.clear();
        }
        excelWriter.close();
    }
}
