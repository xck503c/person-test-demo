package com.xck.jdk.file.calcite;

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

    public SimpleSchema(File baseDir, Class rowEnumeratorClass, String fileSuffix, TableMetaReader tableMetaReader) {
        this.baseDir = baseDir;
        this.rowEnumeratorClass = rowEnumeratorClass;
        this.fileSuffix = fileSuffix;
        this.tableMetaReader = tableMetaReader;
    }

    @Override
    protected Map<String, Table> getTableMap() {
        final Source baseSource = Sources.of(baseDir);
        // 过滤.csv文件结尾的文件
        File[] files = baseDir.listFiles((dir, name) -> name.endsWith(fileSuffix));

        // 将路径和csvTable对象进行映射
        Map<String, Table> tables = new HashMap<>();
        for (File file : files) {
            final Source source = Sources.of(file);
            final Source sourceSansCsv = source.trimOrNull(fileSuffix);
            if (sourceSansCsv != null) {
                SimpleTable simpleTable = new SimpleTable(file, rowEnumeratorClass, tableMetaReader);
                tables.put(sourceSansCsv.relative(baseSource).path(), simpleTable);
            }
        }

        return tables;
    }
}
