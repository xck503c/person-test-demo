package com.xck.jdk.file.pipe;

import cn.hutool.core.io.FileUtil;
import com.xck.jdk.file.config.DateUtil;
import com.xck.jdk.file.config.MyFileUtil;
import com.xck.jdk.file.config.SubmitConfigItem;

import java.io.File;

/**
 * 压缩处理
 *
 * @author xuchengkun
 * @date 2021/11/28 19:00
 **/
public class ZipReRunDealer implements Pipe{

    private SubmitConfigItem configItem;
    private String filePrefix;
    private String reportFile;
    private String zipFile;
    private int count = 0;

    public ZipReRunDealer(SubmitConfigItem configItem, String filePrefix) {
        this.configItem = configItem;
        this.filePrefix = filePrefix;
    }

    @Override
    public Object start(Object input) throws Exception {
        String mothTime = DateUtil.curTime(DateUtil.yyyyMM);
        String curDay = DateUtil.curTime(DateUtil.yyyyMMdd);
        String reportFile = configItem.getFile() + "/" + mothTime + "/" + filePrefix + "_" + curDay;
        this.reportFile = reportFile + "." + configItem.getFileFormat();
        this.zipFile = reportFile + ".zip";
        MyFileUtil.mkParentDir(this.reportFile);
        MyFileUtil.ifExistDel(this.reportFile);
        MyFileUtil.ifExistDel(this.zipFile);
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
        File file = FileUtil.file(this.reportFile);
        if (file.exists() && count > 0) {
            MyFileUtil.zip(this.reportFile, this.zipFile, configItem.getZipPwd());
        }
        MyFileUtil.ifExistDel(this.reportFile);
        this.reportFile = null;
        this.zipFile = null;
        this.count = 0;
    }
}
