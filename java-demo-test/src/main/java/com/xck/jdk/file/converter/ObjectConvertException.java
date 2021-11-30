package com.xck.jdk.file.converter;

/**
 * 系统配置异常
 *
 * @author xuchengkun
 * @date 2021/11/25 09:56
 **/
public class ObjectConvertException extends RuntimeException{

    public ObjectConvertException(String message) {
        super(message);
    }

    public ObjectConvertException(Throwable cause) {
        super(cause);
    }

    protected ObjectConvertException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
