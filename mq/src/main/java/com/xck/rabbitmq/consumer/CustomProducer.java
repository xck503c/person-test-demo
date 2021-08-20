package com.xck.rabbitmq.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.*;

/**
 * 生产者
 *
 * @author xuchengkun
 * @date 2021/07/13 13:36
 **/
public class CustomProducer {

    public static void main(String[] args) throws Exception{

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("192.168.118.117");
        connectionFactory.setPort(5672);
        connectionFactory.setVirtualHost("/");
        connectionFactory.setUsername("xck01");
        connectionFactory.setPassword("123456");

        Connection connection = connectionFactory.newConnection();

        final Channel channel = connection.createChannel();

        String exchangeName = "test_custom_exchange";
        String routingKey = "test.custom.gfg";


        String msg = "hello world send confirm";
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(msg);
        }

        for (String s : list){
            channel.basicPublish(exchangeName, routingKey, null, s.getBytes());
        }


        Thread.sleep(5000);

        channel.close();
        connection.close();
    }
}
