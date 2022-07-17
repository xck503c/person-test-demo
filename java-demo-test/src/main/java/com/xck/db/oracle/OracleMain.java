package com.xck.db.oracle;

import java.sql.DriverManager;

/**
 * @author xuchengkun
 * @date 2021/12/31 11:16
 **/
public class OracleMain {

    public static void main(String[] args) throws Exception{
        Class.forName("oracle.jdbc.driver.OracleDriver");//加载数据驱动

        String url="jdbc:oracle:thin:@172.17.114.194:1521:orcl";
        String user="SMS_CLUSTER";
        String password="Hstest2014";
        System.setProperty("oracle.jdbc.useThreadLocalBufferCache", "true");
        DriverManager.getConnection(url, user, password).close();// 连接数据库
    }
}
