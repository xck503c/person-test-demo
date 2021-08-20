package com.xck.rabbitmq.limit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 限流生产者
 *
 * @author xuchengkun
 * @date 2021/07/15 09:58
 **/
public class LimitProducer {

    public static void main(String[] args) throws Exception{
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUsername("xck01");
        connectionFactory.setPassword("123456");
        connectionFactory.setPort(5672);
        connectionFactory.setVirtualHost("/");

        Connection connection = connectionFactory.newConnection();

        Channel channel = connection.createChannel();

        String exchangeName = "test_qos_exchange";
        String routingKey = "qos.ttt";

        String msg = "send msg";

        for (int i = 0; i < 5; i++) {
            channel.basicPublish(exchangeName, routingKey, null, msg.getBytes());
        }
    }
}
