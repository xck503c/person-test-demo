package com.xck.jdk.lock;

/**
 * DLC问题复现
 *
 * @author xuchengkun
 * @date 2021/07/07 09:26
 **/
public class SingleClassDLCTest {

    int a;
    int b;

    private static SingleClassDLCTest instance;

    private SingleClassDLCTest(){
        this.a = 1;
        this.b = 2;
    }

    public static SingleClassDLCTest newInstance(){
        if(instance==null) {
            synchronized(SingleClassDLCTest.class){
                if(instance==null){
                    instance = new SingleClassDLCTest();
                }
            }
        }
        return instance;
    }

    public static void main(String[] args) {

    }
}
