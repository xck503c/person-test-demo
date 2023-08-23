package com.xck.jdk.file.calcite;

import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaFactory;
import org.apache.calcite.schema.SchemaPlus;

import java.io.File;
import java.util.Map;

public class SimpleSchemaFactory implements SchemaFactory {

    @Override
    public Schema create(SchemaPlus parentSchema, String name, Map<String, Object> operand) {
        String directory = (String) operand.get("directory");
        String fileSuffix = (String) operand.get("fileSuffix");
        String tableMetaReaderClassName = (String) operand.get("tableMetaReaderClass");
        String rowEnumeratorClassName = (String) operand.get("rowEnumeratorClass");
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
