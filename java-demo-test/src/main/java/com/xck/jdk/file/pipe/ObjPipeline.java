package com.xck.jdk.file.pipe;

import java.util.ArrayList;
import java.util.List;

/**
 * 对象管道，里面各个pipe之间会互相影响，有个先后顺序
 * @author xuchengkun
 * @date 2021/11/28 12:43
 **/
public class ObjPipeline implements Pipe {

    private List<Pipe> list;

    private ObjPipeline(){
        list = new ArrayList<>();
    }

    private ObjPipeline(int size){
        list = new ArrayList<>();
    }

    public static ObjPipeline create(){
        return new ObjPipeline();
    }

    public static ObjPipeline create(int size){
        return new ObjPipeline(size);
    }

    public ObjPipeline pipe(Pipe pipe){
        list.add(pipe);
        return this;
    }

    public Object start() throws Exception {
        Object cur = null;
        for (Pipe pipe : list) {
            cur = pipe.start(cur);
        }
        return cur;
    }

    @Override
    public Object start(Object input) throws Exception{
        Object cur = input;
        for (Pipe pipe : list) {
            cur = pipe.start(cur);
        }
        return cur;
    }

    /**
     * 关闭的时候倒序关闭
     * @throws Exception
     */
    @Override
    public void close() throws Exception{
        for (int i = list.size()-1; i >= 0; i--) {
            list.get(i).close();
        }
    }

    @Override
    public Object deal(Object input) throws Exception{
        Object cur = input;
        for (Pipe pipe : list) {
            cur = pipe.deal(cur);
            if (cur == null) break;
        }
        return cur;
    }
}
