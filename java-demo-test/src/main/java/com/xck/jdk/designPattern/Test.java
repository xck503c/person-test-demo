package com.xck.jdk.designPattern;

import java.util.HashMap;

/**
 * 方法
 *
 * @author xuchengkun
 * @date 2021/06/03 16:58
 **/
public class Test {

    static HashMap<Integer, Integer> hashMap = new HashMap<>();
    static {
        for (int i=0; i<1000; i++){
            hashMap.put(i, i);
        }
    }

    public static void main(String[] args) throws Exception{
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    HashMap<Integer, Integer> hashMap1 = new HashMap<>();
                    for (int i=0; i<10000; i++){
                        hashMap1.put(i, i);
                    }
                    setHashMap(hashMap1);

//                    try {
//                        Thread.sleep(10);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        }).start();

        for (int i = 0; i < 4; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        Integer i =  getHashMap().get(1);
                        if(i == null){
                            System.out.println("null");
                        }
//                        try {
//                            Thread.sleep(1);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                    }
                }
            }).start();
        }

        Thread.sleep(100000);
    }

    public static HashMap<Integer, Integer> getHashMap() {
        return hashMap;
    }

    public static void setHashMap(HashMap<Integer, Integer> hashMap) {
        Test.hashMap = hashMap;
    }
}
