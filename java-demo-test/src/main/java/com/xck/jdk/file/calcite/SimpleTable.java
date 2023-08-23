package com.xck.jdk.file.calcite;

import com.xck.jdk.file.calcite.csv.CSVEnumerator;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.type.SqlTypeName;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SimpleTable extends AbstractTable implements ScannableTable {

    private File file;

    private Class rowEnumeratorClass;

    private TableMetaReader tableMetaReader;

    public SimpleTable(File file, Class rowEnumeratorClass, TableMetaReader tableMetaReader) {
        this.file = file;
        this.rowEnumeratorClass = rowEnumeratorClass;
        this.tableMetaReader = tableMetaReader;
    }

    @Override
    public Enumerable<Object[]> scan(DataContext root) {
        final RelDataType relDataType = getRowType(root.getTypeFactory());
        return new AbstractEnumerable<Object[]>() {
            @Override
            public Enumerator<Object[]> enumerator() {
                return RowEnumeratorFactory.createRowEnumerator(rowEnumeratorClass, file, relDataType);
            }
        };
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        //
        List<RelDataType> fieldTypes = new ArrayList<>();
        List<String> fieldNames = new ArrayList<>();

        Map<String, String> tableFieldTypeMap = tableMetaReader.getTableFieldTypeMap(file);
        for (Map.Entry<String, String> entry : tableFieldTypeMap.entrySet()) {
            fieldNames.add(entry.getKey());
            fieldTypes.add(getByFieldType(typeFactory, entry.getValue()));
        }

        return typeFactory.createStructType(fieldTypes, fieldNames);
    }

    public RelDataType getByFieldType(RelDataTypeFactory typeFactory, String fieldType) {
        fieldType = fieldType.toLowerCase();
        switch (fieldType) {
            case "string":
                return typeFactory.createSqlType(SqlTypeName.VARCHAR);
            case "int":
                return typeFactory.createSqlType(SqlTypeName.INTEGER);
            case "long":
                return typeFactory.createSqlType(SqlTypeName.BIGINT);
            case "float":
                return typeFactory.createSqlType(SqlTypeName.REAL);
            case "double":
                return typeFactory.createSqlType(SqlTypeName.DOUBLE);
        }

        return null;
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
