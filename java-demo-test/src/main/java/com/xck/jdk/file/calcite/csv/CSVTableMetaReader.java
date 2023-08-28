package com.xck.jdk.file.calcite.csv;

import cn.hutool.core.text.csv.CsvData;
import cn.hutool.core.text.csv.CsvReadConfig;
import cn.hutool.core.text.csv.CsvReader;
import com.xck.jdk.file.calcite.base.TableMetaReader;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

public class CSVTableMetaReader implements TableMetaReader {

    @Override
    public LinkedHashMap<String, String> getTableFieldTypeMap(String tableName, File file) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        // 读文件第一行标题行
        try(CsvReader csvReader = new CsvReader(file, CsvReadConfig.defaultConfig().setContainsHeader(true))) {
            CsvData csvData = csvReader.read();
            List<String> headers = csvData.getHeader();
            for (String header : headers) {
                // 标题行解析：字段名称:字段类型
                String[] arr = header.split(":");
                String fieldName = arr[0];
                String fieldType = arr[1];
                map.put(fieldName, fieldType);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }
}
