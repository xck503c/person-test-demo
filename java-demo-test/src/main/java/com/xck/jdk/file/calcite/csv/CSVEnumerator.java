package com.xck.jdk.file.calcite.csv;

import cn.hutool.core.text.csv.CsvData;
import cn.hutool.core.text.csv.CsvReadConfig;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvRow;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeField;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class CSVEnumerator implements Enumerator<Object[]> {

    private File file;

    private RelDataType rowType;

    private CsvReader csvReader;

    private final CsvData csvData;

    private Iterator<CsvRow> csvIterator;

    public CSVEnumerator(File file, RelDataType rowType) {
        this.file = file;
        this.rowType = rowType;
        this.csvReader = new CsvReader(file, CsvReadConfig.defaultConfig().setContainsHeader(true));
        this.csvData = csvReader.read();
    }

    @Override
    public Object[] current() {
        if (csvIterator == null) {
            reset();
        }

        CsvRow csvRow = csvIterator.next();
        Object[] o = new Object[csvRow.size()];
        List<RelDataType> typeList = rowType.getFieldList()
                .stream()
                .map(RelDataTypeField::getType).collect(Collectors.toList());
        for (int i = 0; i < csvRow.size(); i++) {
            String value = csvRow.get(i);
            RelDataType relDataType = typeList.get(i);
            switch (relDataType.getSqlTypeName().getName()) {
                case "INTEGER":
                    o[i] = Integer.parseInt(value);
                    break;
                case "BIGINT":
                    o[i] = Long.parseLong(value);
                    break;
                case "REAL":
                    o[i] = Float.parseFloat(value);
                    break;
                case "DOUBLE":
                    o[i] = Double.parseDouble(value);
                    break;
                case "VARCHAR":
                default:
                    o[i] = value;
                    break;
            }
        }


        return o;
    }

    @Override
    public boolean moveNext() {
        if (csvIterator == null) {
            reset();
        }

        return csvIterator.hasNext();
    }

    @Override
    public void reset() {
        csvIterator = csvData.iterator();
    }

    @Override
    public void close() {
        try {
            this.csvReader.close();
        } catch (IOException e) {
        }
    }
}
