package com.xck.socket;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class AIOSocketChannelTest {

    private static int port = 9975;

    public static void main(String[] args) throws Exception {
        // 声明一个线程池
        ExecutorService executorService = Executors.newFixedThreadPool(10, new ThreadFactory() {
            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("server-test-thread-" + thread.getId());
                return thread;
            }
        });
        // 绑定线程池，创建通道组
        AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(executorService);

        // 声明服务端通道，并绑定到通道组
        AsynchronousServerSocketChannel serverChannel = AsynchronousServerSocketChannel
                .open(group);
        serverChannel.bind(new InetSocketAddress("0.0.0.0", port));

        // 处理请求
        serverChannel.accept(null, new TestAcceptHandler(serverChannel));

        Thread.sleep(10000000L);
    }

    private static void handlerClient(final AsynchronousSocketChannel socketChannel) {
        System.out.println(Thread.currentThread().getName() + " 处理新连接");
        ByteBuffer headerBuffer = ByteBuffer.allocate(4);
        socketChannel.read(headerBuffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void attachment) {
                System.out.println(Thread.currentThread().getName() + " 读消息头");
                if (headerBuffer.hasRemaining()) {
                    System.out.println(Thread.currentThread().getName() + " 消息头未读取成功，继续读");
                    socketChannel.read(headerBuffer, null, this);
                    return;
                }

                System.out.println(Thread.currentThread().getName() + " 读消息头成功");
                // 消息头读取完成
                headerBuffer.flip();

                // 从消息头中解析消息体长度
                int bodyLength = headerBuffer.getInt(); // 消息头最后 4 字节为消息体长度
                if (bodyLength <= 0 || bodyLength > 1024 * 1024) { // 校验消息体长度
                    System.err.println(Thread.currentThread().getName() + " 非法消息体长度，关闭连接");
                    try {
                        socketChannel.close();
                    } catch (IOException e) {
                    }
                    return;
                }

                // 准备读取消息体
                readBody(socketChannel, bodyLength);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                System.err.println(Thread.currentThread().getName() + " 消息头读异常");
                exc.printStackTrace();
            }
        });
    }

    private static void readBody(final AsynchronousSocketChannel socketChannel, int len) {
        ByteBuffer bodyBuffer = ByteBuffer.allocate(len);

        System.out.println(Thread.currentThread().getName() + " 处理消息体");
        socketChannel.read(bodyBuffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void attachment) {

                if (bodyBuffer.hasRemaining()) {
                    System.out.println(Thread.currentThread().getName() + " 消息头未读取成功，继续读");
                    socketChannel.read(bodyBuffer, null, this);
                    return;
                }

                // 消息头读取完成
                bodyBuffer.flip();

                System.out.println(Thread.currentThread().getName() + " 读取: " + new String(bodyBuffer.array()));

                // 继续准备读
                handlerClient(socketChannel);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                System.err.println(Thread.currentThread().getName() + " 消息体读异常");
                exc.printStackTrace();
            }
        });
    }

    private static class TestAcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Void> {

        private AsynchronousServerSocketChannel serverChannel;

        public TestAcceptHandler(AsynchronousServerSocketChannel serverChannel) {
            this.serverChannel = serverChannel;
        }

        @Override
        public void completed(AsynchronousSocketChannel result, Void attachment) {
            System.out.println("accept 连接");
            // 继续接收请求
            serverChannel.accept(null, this);

            handlerClient(result);
        }

        @Override
        public void failed(Throwable exc, Void attachment) {
            System.err.println(Thread.currentThread().getName() + " 接收连接异常");
            exc.printStackTrace();
        }
    }
}
