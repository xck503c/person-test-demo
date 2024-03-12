package com.xck.db.mapdbtest;

import cn.hutool.core.util.RandomUtil;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DbCacheTest {

    public static void main(String[] args) throws Exception{
//        testUserCache();
        testTemplete();
    }

    public static void testUserCache() {
        DB db = DBMaker.memoryDB().make();
        Map<String, User> userCache = db.hashMap("test", Serializer.STRING, Serializer.JAVA).create();
        User user1 = new User();
        userCache.put("xck01", user1);
        User user2 = userCache.get("xck01");

        System.out.println(user1 == user2);

        for (int i = 0; i < 10000; i++) {
            String s = RandomUtil.randomString(1024*16);
            User users1 = new User();
            users1.s = s;
            userCache.put("xck01" + i, users1);
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            User users1 = userCache.get("xck01" + i);
        }
        System.out.println(System.currentTimeMillis() - start);
    }

    public static void testTemplete() throws Exception{
        Thread.sleep(60000);
        DB db = DBMaker.memoryDB().make();
        Map<String, List<String>> templeteCache = db.hashMap("test", Serializer.STRING, Serializer.JAVA).create();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 2000; i++) {
            String s = RandomUtil.randomString(20);
            list.add(s);
        }
        templeteCache.put("xck01", list);
        templeteCache = null;

        System.out.println("wait 1 min");
        Thread.sleep(60000);

        System.out.println("start");
        templeteCache = db.hashMap("test", Serializer.STRING, Serializer.JAVA).open();
        Thread.sleep(60000);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            List<String> list1 = templeteCache.get("xck01");
        }
        System.out.println(System.currentTimeMillis() - start);
        Thread.sleep(60000);
    }

    private static class User implements Serializable {

        private static final long serialVersionUID = 4788047531244724358L;
        private String name;
        private int age;

        private String s = "";

        @Override
        public String toString() {
            return "User{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}
