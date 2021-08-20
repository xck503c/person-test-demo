package com.xck.rabbitmq.limit;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;

/**
 * 自定义消费者
 *
 * @author xuchengkun
 * @date 2021/07/15 09:08
 **/
public class MyselfConsumer extends DefaultConsumer {

    public MyselfConsumer(Channel channel) {
        super(channel);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        System.out.println("---------------handleDelivery---------------");
        System.out.println(consumerTag);
        System.out.println(envelope);
        System.out.println(properties);
        System.out.println(new String(body));

        getChannel().basicAck(envelope.getDeliveryTag(), false);
    }
}
