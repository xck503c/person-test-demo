package com.xck.socket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class NIOClient {

    public static void main(String[] args) throws Exception{

        Selector selector = Selector.open();
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.socket().setSoTimeout(60000);
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 9999));

        while (true) {
            if (selector.isOpen()) {
                int selectSize = selector.select(100);
                if (selectSize > 0) {
                    Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        SocketChannel channel = (SocketChannel) key.channel();
                        if (key.isConnectable()) {
                            if(channel.finishConnect()){
                                channel.register(selector, SelectionKey.OP_WRITE);
                                System.out.println("连接服务端成功");
                                continue;
                            }
                        }
                        if (key.isWritable()) {
                            byte[] sendMsg = "你好".getBytes(Charset.forName("UTF-8"));
                            ByteBuffer byteBuffer = ByteBuffer.allocate(sendMsg.length);
                            byteBuffer.put(sendMsg);
                            byteBuffer.flip();
                            channel.write(byteBuffer);
                        }

//                        if (key.isReadable()) {
//                            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
//                            int len = channel.read(byteBuffer);
//                            byteBuffer.flip();
//                            System.out.println("收到服务端信息: " + new String(byteBuffer.array(), Charset.forName("UTF-8")));
//                        }
                    }
                }
            }
        }
    }
}
