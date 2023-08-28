package com.xck.jdk.file.calcite.base;

import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.util.Source;
import org.apache.calcite.util.Sources;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SimpleSchema extends AbstractSchema {

    private File baseDir;

    private Class rowEnumeratorClass;

    /**
     * 文件的后缀，过滤不符合条件的文件
     */
    private String fileSuffix;

    private TableMetaReader tableMetaReader;

    private Map<String, Table> tableMap;

    public SimpleSchema(File baseDir, Class rowEnumeratorClass, String fileSuffix, TableMetaReader tableMetaReader) {
        this.baseDir = baseDir;
        this.rowEnumeratorClass = rowEnumeratorClass;
        this.fileSuffix = fileSuffix;
        this.tableMetaReader = tableMetaReader;
    }

    @Override
    protected Map<String, Table> getTableMap() {
        if (tableMap != null) {
            return tableMap;
        }

        final Source baseSource = Sources.of(baseDir);
        // 过滤指定文件结尾的文件
        File[] files = baseDir.listFiles((dir, name) -> name.endsWith(fileSuffix));

        // 将路径和csvTable对象进行映射
        Map<String, Table> tables = new HashMap<>();
        for (File file : files) {
            final Source source = Sources.of(file);
            // 去除文件名后缀名，获取表名
            final Source sourceSansCsv = source.trimOrNull(fileSuffix);
            if (sourceSansCsv != null) {
                String tableName = sourceSansCsv.relative(baseSource).path();
                SimpleTable simpleTable = new SimpleTable(tableName, file, rowEnumeratorClass, tableMetaReader);
                tables.put(tableName, simpleTable);
            }
        }
        tableMap = tables;
        return tableMap;
    }
}
