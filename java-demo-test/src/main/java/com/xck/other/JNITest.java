package com.xck.other;

public class JNITest {

    static {
        //如果是Linux系统，这里要把libHelloWorld.dll改为libHelloWorld.so

        //使用JNI提供的System.load()方法读取目标目录下的cpp动态链接库文件
        System.load("D:/work/cpp/JNITest.dll");
    }

    /**
     * 在C++当中输出HelloWorld
     */
    public native void printHelloWorld();

    public static void main(String[] args) {
        JNITest helloWorld = new JNITest();
        helloWorld.printHelloWorld();
    }
}
