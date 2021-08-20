package com.xck.rabbitmq.confirm;

import com.rabbitmq.client.*;

import java.io.IOException;

/**
 * 消费者
 *
 * @author xuchengkun
 * @date 2021/07/13 13:35
 **/
public class ConfirmConsumer {

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

        String exchangeName = "test_confirm_exchange";
        String routingKey = "test.confirm.#";
        String queueName = "test_confirm_queue";

        //声明并绑定
        channel.exchangeDeclare(exchangeName, "topic", true);
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, exchangeName, routingKey);

        QueueingConsumer queueingConsumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, true, queueingConsumer);

        while (true){
            QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery();
            String msg = new String(delivery.getBody());

            System.out.println(msg);
        }
    }
}
