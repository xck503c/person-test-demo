package com.xck.jdk.file.converter;


import com.xck.jdk.file.pipe.Pipe;

/**
 * @author xuchengkun
 * @date 2021/11/26 13:23
 **/
public abstract class ObjectConverter implements Pipe {

    public ObjectConverter() {
    }

    @Override
    public Object start(Object input) throws Exception {
        return input;
    }

    @Override
    public Object deal(Object input) throws Exception {
        return input;
    }

    @Override
    public void close() throws Exception {}
}
