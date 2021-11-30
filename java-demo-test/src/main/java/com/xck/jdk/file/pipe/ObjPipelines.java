package com.xck.jdk.file.pipe;

import java.util.ArrayList;
import java.util.List;

/**
 * 多个对象管道，管道和管道之间互不影响
 * @author xuchengkun
 * @date 2021/11/28 12:43
 **/
public class ObjPipelines implements Pipe {

    private List<ObjPipeline> list;

    private ObjPipelines(){
        list = new ArrayList<>();
    }

    private ObjPipelines(int size){
        list = new ArrayList<>();
    }

    public static ObjPipelines create(){
        return new ObjPipelines();
    }

    public static ObjPipelines create(int size){
        return new ObjPipelines(size);
    }

    public ObjPipelines pipeline(ObjPipeline pipe){
        list.add(pipe);
        return this;
    }

    public Object start() throws Exception {
        for (Pipe pipe : list) {
            pipe.start(null);
        }
        return null;
    }

    @Override
    public Object start(Object input) throws Exception {
        for (Pipe pipe : list) {
            pipe.start(input);
        }
        return input;
    }

    @Override
    public void close() throws Exception {
        for (int i = list.size()-1; i >= 0; i--) {
            list.get(i).close();
        }
    }

    @Override
    public Object deal(Object input) throws Exception {
        for (Pipe pipe : list) {
            pipe.deal(input);
        }
        return input;
    }

    //清理管道
    public void clear(){
        list.clear();
    }
}
