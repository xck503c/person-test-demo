package com.xck.other.spring;

public class BeanDefinition {

    //别名
    private String suggestedName;
    private Object bean;

    public BeanDefinition(Object bean) {
        this.bean = bean;
    }

    public String getSuggestedName() {
        return suggestedName;
    }

    public void setSuggestedName(String suggestedName) {
        this.suggestedName = suggestedName;
    }

    public Object getBean() {
        return bean;
    }
}
