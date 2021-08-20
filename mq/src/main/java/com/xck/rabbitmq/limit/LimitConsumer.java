package com.xck.rabbitmq.limit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 消费者
 *
 * @author xuchengkun
 * @date 2021/07/12 13:26
 **/
public class LimitConsumer {

    public static void main(String[] args) throws Exception{

        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("xck01");
        factory.setPassword("123456");
        factory.setHost("192.168.118.117");
        factory.setPort(5672);
        factory.setVirtualHost("/");

        Connection connection = factory.newConnection();

        Channel channel = connection.createChannel();

        String exchangeName = "test_qos_exchange";
        String routingKey = "qos.#";
        String queueName = "test_qos";

        channel.exchangeDeclare(exchangeName, "topic", true, false, null);
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, exchangeName, routingKey);

        channel.basicQos(0, 1, false);

        //autoAck设置为false
        channel.basicConsume(queueName, false, new MyselfConsumer(channel));
    }
}
