package com.xck.jdk.file.converter;

import com.xck.jdk.file.pipe.Pipe;

/**
 * @author xuchengkun
 * @date 2021/11/27 23:57
 **/
public class ObjectConvertFactory {

    public static Pipe objConverter(String type, String pwd){
        Pipe objConverter = null;
        if ("json".equals(type)) {
            return new JsonStrConverter(pwd);
        } else if ("default".equals(type)) {
            objConverter = new DefaultObjConverter();
        }
        return objConverter;
    }
}
