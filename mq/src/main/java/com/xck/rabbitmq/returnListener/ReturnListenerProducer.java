package com.xck.rabbitmq.returnListener;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.*;

/**
 * 生产者
 *
 * @author xuchengkun
 * @date 2021/07/13 13:36
 **/
public class ReturnListenerProducer {

    public static void main(String[] args) throws Exception{

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("192.168.118.117");
        connectionFactory.setPort(5672);
        connectionFactory.setVirtualHost("/");
        connectionFactory.setUsername("xck01");
        connectionFactory.setPassword("123456");

        Connection connection = connectionFactory.newConnection();

        final Channel channel = connection.createChannel();

        String exchangeName = "test_confirm_exchange";
        String routingKey = "test11111.confirm";

        //确认集合
        final SortedMap<Long, Object> confirmMap = Collections.synchronizedSortedMap(new TreeMap<Long, Object>());
        //添加确认监听器
        channel.addReturnListener(new ReturnListener() {
            @Override
            public void handleReturn(int replyCode, String replyText, String exchange, String routingKey, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.err.println("--------return listener--------");
                System.err.println("replyCode="+replyCode);
                System.err.println("replyText="+replyText);
                System.err.println("exchange="+exchange);
                System.err.println("routingKey="+routingKey);
                System.err.println("properties="+properties);
                System.err.println(new String(body));
            }
        });

        String msg = "hello world send confirm";
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(msg);
        }

        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties()
                .builder().userId("xck01").build();

        for (String s : list){
            long seq = channel.getNextPublishSeqNo();
            channel.basicPublish(exchangeName, routingKey, true, basicProperties, s.getBytes());
            confirmMap.put(seq, msg);
        }

        Thread.sleep(5000);

        channel.close();
        connection.close();
    }
}
