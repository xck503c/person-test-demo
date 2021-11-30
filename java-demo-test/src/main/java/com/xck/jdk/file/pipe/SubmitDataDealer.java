package com.xck.jdk.file.pipe;

import com.xck.jdk.file.config.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 提交处理器，会拦截，成功通过的会转化为report
 *
 * @author xuchengkun
 * @date 2021/11/26 16:22
 **/
public class SubmitDataDealer implements Pipe {

    public static Log infoLog = LogFactory.getLog("info");

    private SubmitConfigItem configItem;
    private StringBuilder sns;
    private int logCount;
    private int total = 0, success = 0;
    private long startTime;

    public SubmitDataDealer(SubmitConfigItem configItem) {
        this.configItem = configItem;
    }

    @Override
    public Object start(Object input) {
        this.startTime = System.currentTimeMillis();
        this.sns = new StringBuilder();
        this.total = this.success = 0;

        return input;
    }

    @Override
    public void close() {

        if (logCount > 0) {
            infoLog.info("SubmitDataDealer name=" + configItem.getName() + " scanning primary key sns="
                    + sns.toString() + ", size=" + logCount);
        }
        infoLog.info("SubmitDataDealer close name=" + configItem.getName() + ", successSize=" + success
                + ", totalSize=" + total + ", useTime=" + (System.currentTimeMillis() - startTime));

        this.total = this.success = 0;
        this.sns = null;
    }

    @Override
    public Object deal(Object o) {
        Message message = (Message) o;
        ++total;
        if (!configItem.isUsersContains(message.getUser_id())) {
            return null;
        }

        Report report = new Report();

        report.setContent(message.getComplete_content());

        report.setUser_id(message.getUser_id());
        report.setMsg_id(message.getMsg_id());

        ++success;

        sns.append(message.getSn()).append(",");
        if (++logCount >= 1000) {
            infoLog.info("SubmitDataDealer name=" + configItem.getName() + " scanning primary key sns="
                    + sns.toString() + ", size=1000");
            logCount = 0;
            sns = new StringBuilder();
        }

        return report;
    }

}
