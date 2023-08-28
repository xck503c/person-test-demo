package com.xck.jdk.file.calcite.base;

import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.List;

public class RowEnumeratorFactory {

    public static Enumerator<Object[]> createRowEnumerator(Class rowEnumeratorClassName, File file, List<RelDataType> typeList) {
        try {
            Constructor constructor = rowEnumeratorClassName.getDeclaredConstructor(new Class[]{File.class, List.class});
            constructor.setAccessible(true);
            return (Enumerator<Object[]>) constructor.newInstance(file, typeList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
