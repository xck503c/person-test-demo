package com.xck.rabbitmq.message;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

/**
 * 消费者
 *
 * @author xuchengkun
 * @date 2021/07/12 13:26
 **/
public class MsgConsumer {

    public static void main(String[] args) throws Exception{

        //1. 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();

        //配置
        factory.setHost("127.0.0.1");
        factory.setPort(5672);
        factory.setVirtualHost("/");

        //2. 通过连接工厂创建连接
        Connection connection = factory.newConnection();

        //3. 通过连接创建channel
        Channel channel = connection.createChannel();

        //4. 声明（创建）一个队列，通过channel操作
        // durable: 是否持久化
        // exclusive：独占，只有一个channel去监听，实现顺序消费
        // autoDelete：如果queue和exchange没有一个绑定关系，就会自动删除queue
        String queueName = "test001";
        channel.queueDeclare(queueName, true, false, false, null);

        //5. 创建消费者，消费是建立在哪个连接之上
        QueueingConsumer queueingConsumer = new QueueingConsumer(channel);

        //6. 设置channel，消费哪个队列
        // autoAck: 是否自动签收，收到数据自动回复确认给mq。当然了也可以手动签收
        channel.basicConsume(queueName, true, queueingConsumer);

       while (true){
           //8. 获取消息
           QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery();
           String msg = new String(delivery.getBody());
           System.out.println(msg);
           System.out.println(delivery.getProperties().getHeaders());
       }
    }
}
