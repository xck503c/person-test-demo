package com.xck.jdk.file.calcite.base;

import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;

import java.io.File;
import java.util.List;

public abstract class AbstractEnumerator implements Enumerator<Object[]> {

    protected List<RelDataType> columnTypes;

    protected File file;

    protected Object[] current;

    public AbstractEnumerator(File file, List<RelDataType> columnTypes) {
        this.columnTypes = columnTypes;
        this.file = file;
    }

    @Override
    public Object[] current() {
        return current;
    }

    public Object getValueByRelDataType(RelDataType relDataType, Object v) {
        String value = (String) v;
        switch (relDataType.getSqlTypeName()) {
            case INTEGER:
                return Integer.parseInt(value);
            case BIGINT:
                return Long.parseLong(value);
            case REAL:
                return Float.parseFloat(value);
            case DOUBLE:
                return Double.parseDouble(value);
            case VARCHAR:
            default:
                return value;
        }
    }

    @Override
    public void reset() {

    }
}
