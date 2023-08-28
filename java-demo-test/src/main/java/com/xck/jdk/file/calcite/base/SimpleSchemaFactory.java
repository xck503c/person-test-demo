package com.xck.jdk.file.calcite.base;

import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaFactory;
import org.apache.calcite.schema.SchemaPlus;

import java.io.File;
import java.util.Map;

/**
 * {
 * "version": "1.0",
 * "defaultSchema": "csv",
 * "schemas": [
 * {
 * "name": "csv",
 * "type": "custom",
 * "factory": "com.xck.jdk.file.calcite.base.SimpleSchemaFactory",
 * "operand": {
 * "directory": "D:\\test",
 * "fileSuffix":".csv",
 * "tableMetaReaderClass":"com.xck.jdk.file.calcite.csv.CSVTableMetaReader",
 * "rowEnumeratorClass":"com.xck.jdk.file.calcite.csv.CSVEnumerator"
 * }
 * }
 * ]
 * }
 * <p>
 * csv：表示schem名，也就是数据库名称
 * operand：是一些自定义配置，
 * (1) directory：存放表数据文件
 * (2) fileSuffix: 文件后缀名
 * (3) tableMetaReaderClass: 表元数据读取类：表信息，表字段，表类型
 * (4) rowEnumeratorClass: 行迭代处理类：每个文件迭代发送不一样，需要具体实现
 */
public class SimpleSchemaFactory implements SchemaFactory {

    public final static String DIRECTORY = "directory";

    /**
     * 文件后缀，便于提取表名和过滤文件
     */
    public final static String FILE_SUFFIX = "fileSuffix";

    /**
     * 表元数据读取类
     */
    public final static String TABLE_META_READER_CLASS = "tableMetaReaderClass";

    /**
     * 表数据迭代器
     */
    public final static String ROW_ENUMERATOR_CLASS = "rowEnumeratorClass";

    /**
     * 创建数据库
     * @param parentSchema Parent schema
     * @param name Name of this schema
     * @param operand The "operand" JSON property json配置
     * @return
     */
    @Override
    public Schema create(SchemaPlus parentSchema, String name, Map<String, Object> operand) {
        String directory = (String) operand.get(DIRECTORY);
        String fileSuffix = (String) operand.get(FILE_SUFFIX);
        String tableMetaReaderClassName = (String) operand.get(TABLE_META_READER_CLASS);
        String rowEnumeratorClassName = (String) operand.get(ROW_ENUMERATOR_CLASS);
        try {
            Class tableMetaReaderClass = Class.forName(tableMetaReaderClassName);
            TableMetaReader tableMetaReader = (TableMetaReader) tableMetaReaderClass.newInstance();
            Class rowEnumeratorClass = Class.forName(rowEnumeratorClassName);
            return new SimpleSchema(new File(directory), rowEnumeratorClass, fileSuffix, tableMetaReader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
