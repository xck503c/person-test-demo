package com.xck.jdk.reflect;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.spi.LoggingEvent;

public class DailyRollingFileAppenderMonitor extends DailyRollingFileAppender {

    @Override
    protected void subAppend(LoggingEvent event){
        long n = System.currentTimeMillis();

    }
}
