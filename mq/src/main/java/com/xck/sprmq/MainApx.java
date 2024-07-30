package com.xck.sprmq;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.support.GenericMessage;

@SpringBootApplication
public class MainApx {

    public static void main(String[] args) {
        ApplicationContext apx = SpringApplication.run(MainApx.class, args);
        System.out.println("xxxx");

        RocketMQTemplate rocketMQTemplate = apx.getBean(RocketMQTemplate.class);
        SendResult sendResult = rocketMQTemplate.syncSend("TopicTest", "Hello RocketMQ 同步消息");
        System.out.println(sendResult);
    }
}
