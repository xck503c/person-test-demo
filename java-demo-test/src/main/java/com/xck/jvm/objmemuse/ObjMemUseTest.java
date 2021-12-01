package com.xck.jvm.objmemuse;

import org.openjdk.jol.info.ClassLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * 对象内存占用测试
 *
 * @author xuchengkun
 * @date 2021/04/08 11:03
 **/
public class ObjMemUseTest {

    public static void main(String[] args) {
        System.out.println(ClassLayout.parseInstance(new Long(10L)).toPrintable());
        System.out.println(ClassLayout.parseInstance(new Integer(10)).toPrintable());
        System.out.println(ClassLayout.parseInstance(new HashMap<>()).toPrintable());
        System.out.println(ClassLayout.parseInstance(new HashSet<>()).toPrintable());
        System.out.println(ClassLayout.parseInstance(new ArrayList<>()).toPrintable());
    }

    public static void impleClass() {
        ImpleClass impleClass = new ImpleClass();
        System.out.println(ClassLayout.parseInstance(impleClass).toPrintable());
    }

    public static void stringClass() {
        String s = "1";
        System.out.println(ClassLayout.parseInstance(s).toPrintable());
    }

    public static void charArrClass() {
        char[] c = new char[]{'1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1'};
        System.out.println(ClassLayout.parseInstance(c).toPrintable());
    }

    public static void charClass() {
        Character c = '你';
        System.out.println(ClassLayout.parseInstance(c).toPrintable());
    }
}
