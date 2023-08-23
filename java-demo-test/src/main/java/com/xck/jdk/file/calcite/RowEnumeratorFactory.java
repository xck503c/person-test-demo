package com.xck.jdk.file.calcite;

import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;

import java.io.File;
import java.lang.reflect.Constructor;

public class RowEnumeratorFactory {

    public static Enumerator<Object[]> createRowEnumerator(Class rowEnumeratorClassName, File file, RelDataType rowType) {
        try {
            Constructor constructor = rowEnumeratorClassName.getDeclaredConstructor(new Class[]{File.class, RelDataType.class});
            constructor.setAccessible(true);
            return (Enumerator<Object[]>) constructor.newInstance(file, rowType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
