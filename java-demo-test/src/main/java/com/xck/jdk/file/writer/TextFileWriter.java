package com.xck.jdk.file.writer;

import cn.hutool.core.io.FileUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xuchengkun
 * @date 2021/11/25 14:38
 **/
public class TextFileWriter extends ObjectFileWriter {

    private String fileName;
    private List<Object> list = new ArrayList<>();

    public TextFileWriter() {
    }

    @Override
    public Object start(Object input) throws Exception {
        this.fileName = (String) input;
        return input;
    }

    @Override
    public Object deal(Object input) throws Exception{
        list.add(input);
        if (list.size() >= 1000) {
            FileUtil.writeLines(list, fileName, "UTF-8", true);
            list.clear();
        }
        return null;
    }

    @Override
    public void close() throws Exception{
        if (list.size() > 0) {
            FileUtil.writeLines(list, fileName, "UTF-8", true);
            list.clear();
        }
        fileName = null;
    }
}
