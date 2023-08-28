package com.xck.jdk.file.calcite.csv;

import cn.hutool.core.text.csv.CsvReadConfig;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvRow;
import com.xck.jdk.file.calcite.base.AbstractEnumerator;
import org.apache.calcite.rel.type.RelDataType;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class CSVEnumerator extends AbstractEnumerator {

    private CsvReader csvReader;

    private Iterator<CsvRow> csvIterator;

    /**
     * 构造器
     * @param file 所属文件
     * @param columnTypes 字段类型
     */
    public CSVEnumerator(File file, List<RelDataType> columnTypes) {
        super(file, columnTypes);
        this.csvReader = new CsvReader(file, CsvReadConfig.defaultConfig().setContainsHeader(true));
        this.csvIterator = csvReader.read().iterator();
    }

    @Override
    public boolean moveNext() {
        if (csvIterator.hasNext()) {
            CsvRow csvRow = csvIterator.next();
            Object[] o = new Object[columnTypes.size()];
            for (int i = 0; i < csvRow.size(); i++) {
                // 获取csv文件数据
                String value = csvRow.get(i);
                RelDataType relDataType = columnTypes.get(i);
                o[i] = getValueByRelDataType(relDataType, value);
            }
            current = o;
            return true;
        }

        return false;
    }

    @Override
    public void close() {
        try {
            this.csvReader.close();
        } catch (IOException e) {
        }
    }
}
