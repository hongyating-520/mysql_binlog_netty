package com.example.netty_demo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;


/*
 * @author ZZQ
 * @Date 2021/12/28 11:02 上午
 */
public class ServerDemo {
    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(2);

        try {
            ServerBootstrap b = new ServerBootstrap();
            ServerBootstrap group = b.group(bossGroup, workerGroup);
            group.channel(NioServerSocketChannel.class);
            group.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(
                            new HttpServerCodec(),//字节解码
                            new HttpObjectAggregator(65535),//消息字节最大
                            new WebSocketServerProtocolHandler("/websocket"));//处理方式websocket
                            new MsgHandler();
                }
            });
            ChannelFuture sync = group.bind(8001).sync();
            if (sync.isSuccess()){
                System.out.println("start server.......");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            workerGroup.shutdownGracefully();
//            bossGroup.shutdownGracefully();
        }

    }
}
class MsgHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("active");
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("接收消息"+msg);
    }
}
