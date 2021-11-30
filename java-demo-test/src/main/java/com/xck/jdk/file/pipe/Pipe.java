package com.xck.jdk.file.pipe;

/**
 * 管道
 *
 * @author xuchengkun
 * @date 2021/11/28 12:48
 **/
public interface Pipe {

    /**
     * 初始化
     *
     * @param input
     * @return
     * @throws Exception
     */
    Object start(Object input) throws Exception;

    /**
     * 处理，并返回处理后的结果
     *
     * @param input
     * @return
     * @throws Exception
     */
    Object deal(Object input) throws Exception;

    /**
     * 关闭
     *
     * @throws Exception
     */
    void close() throws Exception;
}
