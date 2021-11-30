package com.xck.jdk.file.config;

/**
 * 系统配置异常
 *
 * @author xuchengkun
 * @date 2021/11/25 09:56
 **/
public class SystemConfigurationException extends RuntimeException{

    public SystemConfigurationException(String message) {
        super(message);
    }

    public SystemConfigurationException(Throwable cause) {
        super(cause);
    }

    protected SystemConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
