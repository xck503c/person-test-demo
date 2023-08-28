package com.xck.jdk.file.calcite.excel;

import cn.hutool.poi.excel.ExcelReader;
import com.xck.jdk.file.calcite.base.TableMetaReader;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.File;
import java.util.LinkedHashMap;

public class ExcelTableMetaReader implements TableMetaReader {

    @Override
    public LinkedHashMap<String, String> getTableFieldTypeMap(String tableName, File file) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        // 读文件第一行标题行
        try(ExcelReader excelReader = new ExcelReader(file, 0)) {
            Sheet sheet = excelReader.getSheet();
            Row row = sheet.getRow(0);
            for (Cell cell : row) {
                // 标题行解析：字段名称:字段类型
                String[] arr = cell.getStringCellValue().split(":");
                String fieldName = arr[0];
                String fieldType = arr[1];
                map.put(fieldName, fieldType);
            }
        }

        return map;
    }
}
