package com.xck.rabbitmq.confirm;

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
public class ConfirmProducer {

    public static void main(String[] args) throws Exception{

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("192.168.118.117");
        connectionFactory.setPort(5672);
        connectionFactory.setVirtualHost("/");
        connectionFactory.setUsername("xck01");
        connectionFactory.setPassword("123456");

        Connection connection = connectionFactory.newConnection();

        final Channel channel = connection.createChannel();
        //指定消息确认模式
        channel.confirmSelect(); //开启确认模式

        String exchangeName = "test_confirm_exchange";
        String routingKey = "test.confirm";

        //确认集合
        final SortedMap<Long, Object> confirmMap = Collections.synchronizedSortedMap(new TreeMap<Long, Object>());
        //添加确认监听器
        //deliveryTag
        channel.addConfirmListener(new ConfirmListener() {
            /**
             *
             * @param deliveryTag 消息投递唯一标识ID
             * @param multiple
             * @throws IOException
             */
            @Override
            public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                if (multiple){
                    System.out.println("ack, remove <" + (deliveryTag+1));
                    confirmMap.headMap(deliveryTag+1).clear();
                }else {
                    System.out.println("ack, remove =" + (deliveryTag));
                    confirmMap.remove(deliveryTag);
                }
            }

            @Override
            public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                if (multiple){
                    System.out.println("no ack, remove <" + deliveryTag+1);
                    SortedMap sortedMap = confirmMap.headMap(deliveryTag+1); //重发队列重发
                }else {
                    System.out.println("no ack, remove =" + deliveryTag);
                    Object o = confirmMap.remove(deliveryTag);
                }
            }
        });

//        String msg = "hello world send confirm";
//        for (int i = 0; i < 5; i++) {
//            channel.basicPublish(exchangeName, routingKey, null, msg.getBytes());
//            while (!channel.waitForConfirms()){
//                channel.basicPublish(exchangeName, routingKey, null, msg.getBytes());
//            }
//        }

//        String msg = "hello world send confirm";
//        for (int i = 0; i < 5; i++) {
//            channel.basicPublish(exchangeName, routingKey, null, msg.getBytes());
//            while (!channel.waitForConfirms()){
//                channel.basicPublish(exchangeName, routingKey, null, msg.getBytes());
//            }
//        }

        String msg = "hello world send confirm";
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(msg);
        }

        for (String s : list){
            long seq = channel.getNextPublishSeqNo();
            channel.basicPublish(exchangeName, routingKey, null, s.getBytes());
            confirmMap.put(seq, msg);
        }

//        boolean f = channel.waitForConfirms();
//        System.out.println(f);
//        while (!f){
//            for (String s : list){
//                channel.basicPublish(exchangeName, routingKey, null, s.getBytes());
//            }
//        }

        Thread.sleep(5000);

        channel.close();
        connection.close();
    }
}
