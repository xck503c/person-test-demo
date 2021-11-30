package com.xck.jdk.file.writer;

import com.xck.jdk.file.pipe.Pipe;

/**
 * @author xuchengkun
 * @date 2021/11/27 23:57
 **/
public class ObjectWriterFactory {

    public static <T> Pipe fileWriter(String fileFormat, Class<T> writeObj){
        Pipe objectWriter = null;
        if ("excel".equals(fileFormat)) {
            return new ExcelFileWriter(writeObj);
        } else if ("txt".equals(fileFormat)) {
            objectWriter = new TextFileWriter();
        } else if("csv".equals(fileFormat)) {
            objectWriter = new CSVFileWriter(writeObj);
        }
        return objectWriter;
    }
}
