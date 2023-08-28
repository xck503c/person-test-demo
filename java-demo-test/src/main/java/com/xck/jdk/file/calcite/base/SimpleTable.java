package com.xck.jdk.file.calcite.base;

import com.alibaba.druid.sql.visitor.functions.Char;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.impl.AbstractTable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SimpleTable extends AbstractTable implements ScannableTable {

    private String tableName;

    private File file;

    private Class rowEnumeratorClass;

    private TableMetaReader tableMetaReader;

    protected RelDataType rowType;

    public SimpleTable(String tableName, File file, Class rowEnumeratorClass, TableMetaReader tableMetaReader) {
        this.tableName = tableName;
        this.file = file;
        this.rowEnumeratorClass = rowEnumeratorClass;
        this.tableMetaReader = tableMetaReader;
    }

    @Override
    public Enumerable<Object[]> scan(DataContext root) {
        RelDataType rowType = getRowType(root.getTypeFactory());
        List<RelDataType> fieldTypes = rowType.getFieldList()
                .stream()
                .map(RelDataTypeField::getType)
                .collect(Collectors.toList());
        return new AbstractEnumerable<Object[]>() {
            @Override
            public Enumerator<Object[]> enumerator() {
                return RowEnumeratorFactory.createRowEnumerator(rowEnumeratorClass, file, fieldTypes);
            }
        };
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        //
        if (rowType == null) {
            List<RelDataType> fieldTypes = new ArrayList<>();
            List<String> fieldNames = new ArrayList<>();

            Map<String, String> tableFieldTypeMap = tableMetaReader.getTableFieldTypeMap(tableName, file);
            for (Map.Entry<String, String> entry : tableFieldTypeMap.entrySet()) {
                fieldNames.add(entry.getKey());
                fieldTypes.add(getRelDataType(typeFactory, entry.getValue()));
            }
            this.rowType = typeFactory.createStructType(fieldTypes, fieldNames);
        }

        return this.rowType;
    }

    public RelDataType getRelDataType(RelDataTypeFactory typeFactory, String fieldType) {
        fieldType = fieldType.toLowerCase();
        switch (fieldType) {
            case "int":
                return typeFactory.createJavaType(Integer.class);
            case "long":
                return typeFactory.createJavaType(Long.class);
            case "float":
                return typeFactory.createJavaType(Float.class);
            case "double":
                return typeFactory.createJavaType(Double.class);
            case "char":
                return typeFactory.createJavaType(Char.class);
            case "boolean":
                return typeFactory.createJavaType(Boolean.class);
            case "string":
                return typeFactory.createJavaType(String.class);
            default:
                throw new IllegalArgumentException("非法字段类型" + fieldType);
        }
    }

    @Override
    public <C> C unwrapOrThrow(Class<C> aClass) {
        return super.unwrapOrThrow(aClass);
    }

    @Override
    public <C> Optional<C> maybeUnwrap(Class<C> aClass) {
        return super.maybeUnwrap(aClass);
    }
}
