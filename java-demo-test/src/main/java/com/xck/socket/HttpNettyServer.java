package com.xck.socket;


import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HttpNettyServer {

    public static void main(String[] args) throws Exception{
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast("httpAggregator",new HttpObjectAggregator(512*1024));
                            ch.pipeline().addLast(new MyHttpRequestHandler());
                        }
                    });

            server.bind(8888).addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    System.out.println("服务端口:" + 8888 + ",启动成功");

                }
            }).channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static class MyHttpRequestHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            FullHttpRequest request = (FullHttpRequest) msg;
            String content = request.content().toString(CharsetUtil.UTF_8);
            JSONObject json = JSON.parseObject(content);

            System.out.println(json.toJSONString());

            List<String> list = new ArrayList<>();
            list.add(content);
            FileUtil.appendLines(list, new File("D:/wzyh-report-1.txt"), CharsetUtil.UTF_8);

            JSONObject resp = new JSONObject();
            resp.put("code", 0);
            resp.put("errMsg", "OK");

            ByteBuf buf = Unpooled.copiedBuffer(resp.toJSONString(), CharsetUtil.UTF_8);
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
            HttpUtil.setContentLength(response, buf.readableBytes());
            ctx.writeAndFlush(response);
        }
    }
}
