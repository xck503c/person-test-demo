package com.xck.rabbitmq.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

/**
 * 消费者
 *
 * @author xuchengkun
 * @date 2021/07/13 13:35
 **/
public class CustomConsumer {

    public static void main(String[] args) throws Exception{

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("192.168.118.117");
        connectionFactory.setPort(5672);
        connectionFactory.setVirtualHost("/");
        connectionFactory.setUsername("xck01");
        connectionFactory.setPassword("123456");

        Connection connection = connectionFactory.newConnection();

        Channel channel = connection.createChannel();
        //指定消息确认模式
        channel.confirmSelect(); //开启确认模式

        String exchangeName = "test_custom_exchange";
        String routingKey = "test.custom.#";
        String queueName = "test_custom_queue";

        //声明并绑定
        channel.exchangeDeclare(exchangeName, "topic", true);
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, exchangeName, routingKey);

        channel.basicConsume(queueName, true, new MyselfConsumer(channel));
    }
}
