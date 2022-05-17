package com.xck.persistentQueue.bdb;

public class BDBMain {

    public static void main(String[] args) {
        //String.class:value类型
        String path = System.getProperty("user.dir");
        BDBPersistentQueue<String> queue = new BDBPersistentQueue<String>(path + "/mq/bdb", "test", String.class);
        queue.offer("first");
        queue.offer("double");
        queue.offer("String");
        queue.sync();

        String p1 = queue.poll();
        String p2 = queue.poll();
        String p3 = queue.poll();
        System.out.println(p1);
        System.out.println(p2);
        System.out.println(p3);
        queue.close();
    }
}
