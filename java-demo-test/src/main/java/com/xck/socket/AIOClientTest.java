package com.xck.socket;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;

public class AIOClientTest {

    private static int port = 9975;

    public static void main(String[] args) throws Exception {

        SocketChannel clientChannel = SocketChannel.open();
        clientChannel.configureBlocking(false);
        clientChannel.connect(new InetSocketAddress("127.0.0.1", port));
        Selector selector = Selector.open();
        clientChannel.register(selector, SelectionKey.OP_CONNECT);
        while (true) {
            selector.select();
            for (SelectionKey key : selector.selectedKeys()) {
                if (key.isConnectable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    if (channel.finishConnect()) {
                        channel.register(
                                selector, SelectionKey.OP_WRITE,
                                null
                        );
                    }
                }
                if (key.isWritable()) {
                    try {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        byte[] b = "hello world".getBytes();
                        ByteBuffer buffer = ByteBuffer.allocate(4 + b.length);
                        buffer.putInt(b.length);
                        buffer.put(b);
                        // 写之前flip
                        buffer.flip();
                        socketChannel.write(buffer);
                        System.out.println("写成功");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // ======================================
        // 客户端测试
        // 声明一个线程池
//        ExecutorService executorService = Executors.newFixedThreadPool(10, new ThreadFactory() {
//            @Override
//            public Thread newThread(@NotNull Runnable r) {
//                Thread thread = new Thread(r);
//                thread.setName("server-test-thread-" + thread.getId());
//                return thread;
//            }
//        });
//        // 绑定线程池，创建通道组
//        AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(executorService);
//        try (AsynchronousSocketChannel clientChannel = AsynchronousSocketChannel.open(group)) {
//            clientChannel.connect(new InetSocketAddress(port), null, new CompletionHandler<Void, Object>() {
//                @Override
//                public void completed(Void result, Object attachment) {
//                    System.out.println("连接成功");
//
//                    while (true) {
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                        }
//                        byte[] b = "hello world".getBytes();
//                        ByteBuffer buffer = ByteBuffer.allocate(4 + b.length);
//                        buffer.putInt(b.length);
//                        buffer.put(b);
//                        buffer.flip();
//                        clientChannel.write(buffer, null, new CompletionHandler<Integer, Void>() {
//                            @Override
//                            public void completed(Integer result, Void attachment) {
//                                System.out.println(Thread.currentThread().getName() + " 写成功");
//                            }
//
//                            @Override
//                            public void failed(Throwable exc, Void attachment) {
//                                System.err.println(Thread.currentThread().getName() + " 客户端写异常");
//                                exc.printStackTrace();
//                            }
//                        });
//                    }
//                }
//
//                @Override
//                public void failed(Throwable exc, Object attachment) {
//                    System.err.println(Thread.currentThread().getName() + " 客户端连接异常");
//                    exc.printStackTrace();
//                }
//            });
//        }
    }
}
