package com.xck.jdk.file.calcite;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 示例，csv来通过sql操作
 * 来源：https://blog.csdn.net/u010034713/article/details/112766677
 */
public class CalciteOperateCSVBySQL {

    /**
     * {
     *   "version": "1.0",
     *   "defaultSchema": "csv",
     *   "schemas": [
     *     {
     *       "name": "csv",
     *       "type": "custom",
     *       "factory": "org.apache.calcite.adapter.csv.CsvSchemaFactory",
     *       "operand": {
     *         "directory": "D:\\test",
     *       }
     *     }
     *   ]
     * }
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Properties config = new Properties();
        config.put("model", "D://test/calcite_model.json");
        // Object 'CSV' not found; did you mean 'csv'?
        config.put("caseSensitive", "false");
        Connection conn = DriverManager.getConnection("jdbc:calcite:", config);
        List<String> sqlList = new ArrayList<>();
        sqlList.add("select * from csv.calcite_people");
        sqlList.add("select id, name || '_after_append' from calcite_people");
        sqlList.add("select t.id,t.name,t2.age from calcite_people t left join calcite_detail t2 on t.id = t2.id");
        for (String sql : sqlList) {
            System.out.println("-----------------");
            System.out.println(sql);
            printResultSet(conn.createStatement().executeQuery(sql));
        }
    }

    private static void printResultSet(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (resultSet.next()) {
            List<Object> row = new ArrayList<>();
            for (int i = 1; i < columnCount + 1; i++) {
                row.add(resultSet.getObject(i));
            }
            System.out.println(row);
        }
    }

}
