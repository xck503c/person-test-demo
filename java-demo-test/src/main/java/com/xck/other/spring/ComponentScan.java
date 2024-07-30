package com.xck.other.spring;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ComponentScan {

    private Map<String, BeanDefinition> beanMap = new HashMap<>();

    public void scan(String basePackage) throws Exception{
        String basePackagePath = basePackage.replaceAll("\\.", "/");
        ClassLoader classLoader = ComponentScan.class.getClassLoader();
        Enumeration<URL> baseUrls = classLoader.getResources(basePackagePath);
        while (baseUrls.hasMoreElements()) {
            URL base = baseUrls.nextElement();
            String basePath = base.getPath();
            // 分解jar:file:/D:/dependency/mavenRepository/com/hskj/com.hskj.commonLog/V1.0.1.R_160505/com.hskj.commonLog-V1.0.1.R_160505.jar!/com/hskj
            if (basePath.contains("!")) {
                basePath = basePath.split("!")[0];
            }
            // 解析jar
            if (basePath.endsWith(".jar")) {
                // 遍历jar下的文件
                JarURLConnection jarURLConnection = (JarURLConnection) base.openConnection();
                JarFile jarFile = jarURLConnection.getJarFile();
                Enumeration<JarEntry> jarEntries = jarFile.entries();
                while (jarEntries.hasMoreElements()) {
                    JarEntry jarEntry = jarEntries.nextElement();
                    String jarEntryName = jarEntry.getName();
                    if (jarEntryName.startsWith(basePackagePath) && jarEntryName.endsWith(".class")) {
                        String className = jarEntryName.replace(".class", "").replaceAll("/", ".");
                        Class clazz = Class.forName(className);
                        scanClass(clazz);
                    }
                }
            } else {
                // 普通文件
                File baseFile = new File(base.getFile());
                for (File file : baseFile.listFiles()) {
                    if (file.isDirectory()) {
                        scan(basePackage + "." + file.getName());
                    } else {
                        if (file.getName().endsWith(".class")) {
                            String className = basePackage + "." + file.getName().replace(".class", "");
                            Class clazz = Class.forName(className);
                            scanClass(clazz);
                        }
                    }
                }
            }
        }

        scanClassField();
    }

    public void scanClass(Class<?> clazz) throws InstantiationException, IllegalAccessException {
        // class级别的注解
        Class[] classAnnotations = new Class[]{Service.class,  Component.class,  Repository.class};
        for (Class classAnnotation : classAnnotations) {
            // 判断该类上是否存在指定注解
            if (clazz.isAnnotationPresent(classAnnotation)) {
                // 如果存在别名，则用别名，否则用类名
                Component component = (Component)clazz.getAnnotation(classAnnotation);
                BeanDefinition beanDefinition = new BeanDefinition(clazz.newInstance());
                if (StringUtils.isNotBlank(component.value())) {
                    beanMap.put(component.value(), beanDefinition);
                } else {
                    beanMap.put(clazz.getSimpleName(), beanDefinition);
                }
                break;
            }
        }
    }

    public void scanClassField() {
        Class[] fieldClassAnnotations = new Class[]{Autowired.class, Resource.class};

        // 属性注入
        for (Map.Entry<String, BeanDefinition> entry : beanMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();

            Field[] fields = beanDefinition.getBean().getClass().getFields();
            for (Field field : fields) {
                for (Class fieldClassAnnotation : fieldClassAnnotations) {
                    if (field.isAnnotationPresent(fieldClassAnnotation)) {
                        field.setAccessible(true);
                        injectField(field, beanDefinition.getBean(), fieldClassAnnotation);
                        break;
                    }
                }
            }
        }
    }

    public void injectField(Field field,  Object bean, Class fieldClassAnnotatio) {

    }
}
