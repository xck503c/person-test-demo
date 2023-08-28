package com.xck.jdk.file.calcite;

import org.apache.calcite.adapter.java.ReflectiveSchema;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 示例，通过Java构建内存Schema来通过sql操作
 * 来源：https://blog.csdn.net/u010034713/article/details/112766677
 */
public class TestJavaSchemaBySQL {

    public static class CalciteSchemaPeople {

        public int id;
        public String name;

        public CalciteSchemaPeople(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class CalciteSchemaPeopleDetail {
        public int id;
        public int age;

        public CalciteSchemaPeopleDetail(int id, int age) {
            this.id = id;
            this.age = age;
        }
    }

    public static class JavaSchema {
        public CalciteSchemaPeople[] calciteSchemaPeople11 = new CalciteSchemaPeople[]{
                new CalciteSchemaPeople(1, "xck01"),
                new CalciteSchemaPeople(2, "xck02"),
                new CalciteSchemaPeople(3, "xck03")
        };

        public CalciteSchemaPeopleDetail[] calciteSchemaPeopleDetail11 = new CalciteSchemaPeopleDetail[]{
                new CalciteSchemaPeopleDetail(1, 1000),
                new CalciteSchemaPeopleDetail(2, 3),
                new CalciteSchemaPeopleDetail(3, 18)
        };
    }

    public static void main(String[] args) throws Exception {
        // 主动加载class
        Class.forName("org.apache.calcite.jdbc.Driver");
        Properties info = new Properties();
        info.setProperty("lex", "JAVA");
        // 创建连接
        Connection connection = DriverManager.getConnection("jdbc:calcite:", info);
        CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
        // SchemaPlus相当databases
        SchemaPlus rootSchema = calciteConnection.getRootSchema();
        // 创建schema, ReflectiveSchema内部通过放射会根据提供的schema创建table,field
        Schema schema = new ReflectiveSchema(new JavaSchema());
        // databases添加schema
        rootSchema.add("sc", schema);
        // 创建 Statement
        Statement statement = calciteConnection.createStatement();
        // 执行sql
        ResultSet resultSet = statement.executeQuery("" +
                "select p.id, p.name, d.age " +
                "from sc.calciteSchemaPeople11 p left join sc.calciteSchemaPeopleDetail11 d  on p.id = d.id " +
                "where p.id >= 2" +
                "");
        printResultSet(resultSet);
        resultSet.close();
        statement.close();
        connection.close();
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
