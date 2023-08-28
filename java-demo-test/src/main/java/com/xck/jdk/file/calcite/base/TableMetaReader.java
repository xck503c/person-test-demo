package com.xck.jdk.file.calcite.base;

import java.io.File;
import java.util.LinkedHashMap;

public interface TableMetaReader {

    /**
     *
     * @param file
     * @return 字段名->字段类型映射 要固定顺序，否则会解析错乱
     */
    LinkedHashMap<String, String> getTableFieldTypeMap(String tableName, File file);
}
