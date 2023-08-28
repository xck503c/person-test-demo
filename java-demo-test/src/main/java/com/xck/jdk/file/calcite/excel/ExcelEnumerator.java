package com.xck.jdk.file.calcite.excel;

import cn.hutool.poi.excel.ExcelReader;
import com.xck.jdk.file.calcite.base.AbstractEnumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.File;
import java.util.List;

public class ExcelEnumerator extends AbstractEnumerator {

    private ExcelReader excelReader;
    private Sheet sheet;
    private int rowIndex = 1;

    public ExcelEnumerator(File file, List<RelDataType> columnTypes) {
        super(file, columnTypes);
        this.excelReader = new ExcelReader(file, 0);
        this.sheet = excelReader.getSheet();
    }

    @Override
    public Object[] current() {
        return current;
    }

    @Override
    public boolean moveNext() {
        if (rowIndex < sheet.getLastRowNum()) {
            Row row = sheet.getRow(rowIndex);
            int columnSize = row.getLastCellNum();
            Object[] o = new Object[columnSize];
            for (int i = 0; i < columnSize; i++) {
                String value = row.getCell(i).toString();
                RelDataType relDataType = columnTypes.get(i);
                o[i] = getValueByRelDataType(relDataType, value);
            }
            current = o;
            ++rowIndex;
            return true;
        }

        return false;
    }

    @Override
    public void close() {
        this.excelReader.close();
        this.sheet = null;
        this.rowIndex = 1;
    }
}
