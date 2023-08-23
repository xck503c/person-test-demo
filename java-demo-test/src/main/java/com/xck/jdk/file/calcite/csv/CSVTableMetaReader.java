package com.xck.jdk.file.calcite.csv;

import cn.hutool.core.text.csv.CsvData;
import cn.hutool.core.text.csv.CsvReadConfig;
import cn.hutool.core.text.csv.CsvReader;
import com.xck.jdk.file.calcite.TableMetaReader;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVTableMetaReader implements TableMetaReader {

    @Override
    public Map<String, String> getTableFieldTypeMap(File file) {
        Map<String, String> map = new HashMap<>();
        // 读文件第一行标题行
        CsvReader csvReader = new CsvReader(file, CsvReadConfig.defaultConfig().setContainsHeader(true));
        CsvData csvData = csvReader.read();
        List<String> headers = csvData.getHeader();
        for (String header : headers) {
            // 标题行解析：字段名称:字段类型
            String[] arr = header.split(":");
            String fieldName = arr[0];
            String fieldType = arr[1];
            map.put(fieldName, fieldType);
        }

        return map;
    }
}
