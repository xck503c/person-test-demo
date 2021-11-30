package com.xck.jdk.file.pipe;

import cn.hutool.core.io.FileUtil;
import com.xck.jdk.file.config.DateUtil;
import com.xck.jdk.file.config.MyFileUtil;
import com.xck.jdk.file.config.SubmitConfigItem;

import java.io.File;

/**
 * 文件的前置和后置处理，如果开始前文件存在则删除，结束时如果有数据则压缩文件
 *
 * @author xuchengkun
 * @date 2021/11/28 19:00
 **/
public class ZipDealer implements Pipe{

    private SubmitConfigItem configItem;
    private String filePrefix;
    private String reportFile;
    private String zipFile;
    private int count;

    public ZipDealer(SubmitConfigItem configItem, String filePrefix) {
        this.configItem = configItem;
        this.filePrefix = filePrefix;
    }

    @Override
    public Object start(Object input) throws Exception {
        String mothTime = DateUtil.curTime(DateUtil.yyyyMM);
        String curDay = DateUtil.curTime(DateUtil.yyyyMMdd);
        String reportFile = configItem.getFile() + "/" + mothTime + "/" + filePrefix + "_"  + curDay;
        this.reportFile = reportFile + "." + configItem.getFileFormat();
        this.zipFile = reportFile + ".zip";
        MyFileUtil.mkParentDir(this.reportFile);
        MyFileUtil.ifExistDel(reportFile);
        MyFileUtil.ifExistDel(zipFile);
        this.count = 0;
        return this.reportFile;
    }

    @Override
    public Object deal(Object input) throws Exception {
        ++count;
        return input;
    }

    @Override
    public void close() throws Exception {
        File file = FileUtil.file(reportFile);
        if (file.exists() && count > 0) {
            MyFileUtil.zip(reportFile, zipFile, configItem.getZipPwd());
        }
        MyFileUtil.ifExistDel(reportFile);
        reportFile = null;
        zipFile = null;
        this.count = 0;
    }
}
