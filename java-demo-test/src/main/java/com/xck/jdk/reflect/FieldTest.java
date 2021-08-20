package com.xck.jdk.reflect;

import java.lang.reflect.Field;

public class FieldTest {

    private String name = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static void main(String[] args) throws Exception{
        FieldTest fieldTest = new FieldTest();
        fieldTest.setName("xck");
        Field field = fieldTest.getClass().getDeclaredField("name");
        field.setAccessible(true);
        System.out.println(field.get(fieldTest));
    }
}
