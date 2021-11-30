package com.xck.jdk.file;

import cn.hutool.core.lang.UUID;
import com.xck.jdk.file.config.Message;
import com.xck.jdk.file.config.SubmitConfig;
import com.xck.jdk.file.pipe.ObjPipelines;
import com.xck.jdk.file.pipe.SubmitService;
import org.apache.commons.lang.RandomStringUtils;

/**
 * 类似日志格式的配置测试
 *
 * @author xuchengkun
 * @date 2021/11/25 09:13
 **/
public class SimilarLogFormPropertiesTest {

    public static void main(String[] args) {

        run();
    }

    public static void run() {
        try {
            SubmitConfig submitConfig = new SubmitConfig();
            submitConfig.newConfig("system-T1.setting");
            System.out.println(submitConfig);

            SubmitService submitDataDealer = new SubmitService(submitConfig);
            submitDataDealer.startTask();
            for (int i = 0; i < 100; i++) {
                submitDataDealer.deal(get());
            }
            submitDataDealer.endTask();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testrun() {
        try {
            SubmitConfig submitConfig = new SubmitConfig();
            submitConfig.newConfig("system-T1.setting");
            System.out.println(submitConfig);

            SubmitService submitDataDealer = new SubmitService(submitConfig);
            ObjPipelines pipelines = ObjPipelines.create(2)
                    .pipeline(submitDataDealer.getSubmitDataReDealers().get("CA"))
                    .pipeline(submitDataDealer.getSubmitDataReDealers().get("CB"));

            pipelines.start("20211102");
            for (int i = 0; i < 100; i++) {
                pipelines.deal(get());
            }
            pipelines.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Message get() {
        Message message = new Message();
        message.setUser_id("jim");
        message.setMsg_id(UUID.fastUUID().toString());
        message.setComplete_content(RandomStringUtils.random(70, 0x4e00, 0x9fa5, true, true));
        return message;
    }
}
