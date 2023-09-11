package com.xck.socket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class NIOServer {

    public static void main(String[] args) throws Exception{

        Selector selector = Selector.open();

        ServerSocketChannel listenerChannel = ServerSocketChannel.open();
        listenerChannel.socket().bind(new InetSocketAddress(9999));
        listenerChannel.configureBlocking(false);
        listenerChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            int selectSize = selector.select(10);
            if (selectSize <= 0) {
                Thread.sleep(5);
                continue;
            }

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
//                it.remove();

                if (key.isValid()) {
                    if (key.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.socket().setSoTimeout(60000);
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        System.out.println("收到客户端连接");
                    } else {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        if (key.isWritable()) {
//                            byte[] sendMsg = "你好".getBytes(Charset.forName("UTF-8"));
//                            ByteBuffer byteBuffer = ByteBuffer.allocate(sendMsg.length);
//                            byteBuffer.put(sendMsg);
//                            byteBuffer.flip();
//                            socketChannel.write(byteBuffer);
                        }

                        if (key.isReadable()) {
                            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                            int len = socketChannel.read(byteBuffer);
                            byteBuffer.flip();
                            System.out.println("收到客户端信息: " + new String(byteBuffer.array(), Charset.forName("UTF-8")));
                        }
                    }
                }
            }
        }


    }
}
