package com.xck.jdk.reflect;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Category;
import org.apache.log4j.DailyRollingFileAppender;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Enumeration;

/**
 * 创建时间: 2020-11-18
 * 功能: 主要测试如果监控DailyRollingFileAppender的滚动方法
 */
public class DailyRollingFileAppenderRefect {

    public static Log log = LogFactory.getLog("info");
    public static DailyRollingFileAppender infoAppender;

    static {
        try {
            Log4JLogger log4JLogger = (Log4JLogger)log;
            Category category = log4JLogger.getLogger();
            DailyRollingFileAppender appender = getDailyRollingFileAppender(category);
            if(appender == null){
                Field parentField = Category.class.getDeclaredField("parent");
                parentField.setAccessible(true);
                Category categorParent = (Category)parentField.get(category);
                appender = getDailyRollingFileAppender(categorParent);
            }

            Field scheduledField = DailyRollingFileAppender.class.getDeclaredField("scheduledFilename");
            scheduledField.setAccessible(true);

            String scheduledFileName = (String)scheduledField.get(appender);

            Field nowField = DailyRollingFileAppender.class.getDeclaredField("now");
            nowField.setAccessible(true);
            Date nowDate = (Date)nowField.get(appender);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception{
        refect();
        log.info("ffsfdfsdfsdfdsfdsfsfdsffffffffffffffffffffffffffff");
        refect();
    }

    public final static void refect() throws Exception{

        Log4JLogger log4JLogger = (Log4JLogger)log;
        Category category = log4JLogger.getLogger();
        DailyRollingFileAppender appender = getDailyRollingFileAppender(category);
        if(appender == null){
            Field parentField = Category.class.getDeclaredField("parent");
            parentField.setAccessible(true);
            Category categorParent = (Category)parentField.get(category);
            appender = getDailyRollingFileAppender(categorParent);
        }

        Field scheduledField = DailyRollingFileAppender.class.getDeclaredField("scheduledFilename");
        scheduledField.setAccessible(true);

        String scheduledFileName = (String)scheduledField.get(appender);

        Field nowField = DailyRollingFileAppender.class.getDeclaredField("now");
        nowField.setAccessible(true);
        Date nowDate = (Date)nowField.get(appender);

        Field nextCheck = DailyRollingFileAppender.class.getDeclaredField("nextCheck");
        nextCheck.setAccessible(true);
        long nextCheckL = (Long)nextCheck.get(appender);

        System.out.println(String.format("%s日志, 需要rename的源文件%s, 计划生成的文件%s, 上次检查的时间%s, "
                        + "下次检查的时间%s(%s)", appender.getName(), appender.getFile()
                , scheduledFileName, nowDate.toString(), nextCheckL+"", new Date(nextCheckL).toString()));
    }

    public static DailyRollingFileAppender getDailyRollingFileAppender(Category category){

        Enumeration enumeration = category.getAllAppenders();
        while (enumeration.hasMoreElements()){
            Object o = enumeration.nextElement();
            if(o instanceof DailyRollingFileAppender){
                return (DailyRollingFileAppender)o;
            }
        }

        return null;
    }
}
