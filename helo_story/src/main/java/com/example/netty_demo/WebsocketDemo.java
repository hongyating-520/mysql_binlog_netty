package com.example.netty_demo;

import com.google.protobuf.GeneratedMessageV3;
import com.protobuf.GameMsgProtocol;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.time.LocalDateTime;

/*
 * @author ZZQ
 * @Date 2021/12/28 2:22 下午
 *
 */
public class WebsocketDemo {
    public static void main(String[] args) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup,workGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(
                        new HttpServerCodec(), //字节解码
                        new HttpObjectAggregator(55555),//消息字节最大
                        new WebSocketServerProtocolHandler("/websocket"),//处理方式websocket
                        new GameMsgDecoder(),//消息msg消息对象解码转换
                        new GameMsgHandler(),//解码之后的消息会到Handler
                        new GameMsgEecoder()//消息编码器：用户服务端到客户端消息发送时候消息编码
                );
            }
        });
        try {
            ChannelFuture sync = bootstrap.bind(8088).sync();//port
            if (sync.isSuccess()){
                System.out.println("demo is sucess");
            }
            //所有管道关闭
            sync.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}


class GameMsgHandler extends SimpleChannelInboundHandler<Object> {

    //创建全局的消息通道：由于initChannel每次都是newGameMsgHandler，所以要是用static修饰通道，保证全局唯一
    public static final ChannelGroup _channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    //消息处理
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        System.out.println("接收到消息——————："+msg);
        System.out.println(channelHandlerContext.channel().id().asLongText());
        com.google.protobuf.GeneratedMessageV3 msgBoty = null;
        if (msg instanceof GameMsgProtocol.UserAttkCmd ){
            msgBoty  = (GameMsgProtocol.UserAttkCmd) msg;
        }
        if (msg instanceof GameMsgProtocol.UserEntryCmd){
            //如果有客户端入场时可接收到消息命令，同时将该用户的形象群发至所有用户
            GameMsgProtocol.UserEntryCmd entryCmd = (GameMsgProtocol.UserEntryCmd) msg;
            GameMsgProtocol.UserEntryResult.Builder builder = GameMsgProtocol.UserEntryResult.newBuilder();
            builder.setUserId(entryCmd.getUserId());
            builder.setHeroAvatar(entryCmd.getHeroAvatar());
            GameMsgProtocol.UserEntryResult build = builder.build();
            //解码成对应二进制消息，再进行广播
            _channelGroup.writeAndFlush(build);
        }
        System.out.println(msgBoty);
    }

    //新用户链接时候触发
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        //添加到通道:进行广播的时候会存在后进入的用户没法获取先进来的用户的广播消息
        _channelGroup.add(ctx.channel());
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

}


//自定义解码器，将接收到的消息转对象
class GameMsgDecoder extends ChannelInboundHandlerAdapter{
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("解码器接收到消息——————："+msg);
        //websocket 二进制消息会从httpservercoder将消息转为BinaryWebSocketFrame
        BinaryWebSocketFrame binaryWebSocketFram = (BinaryWebSocketFrame)msg;
        ByteBuf content = binaryWebSocketFram.content();
        /**
         * demo中使用protobuf发送消息时候前2个字节存储消息长度，第3，4字节存储定义的msgCode
         * @see com.protobuf.GameMsgProtocol.MsgCode
         * */
        content.readShort();
        short msgType = content.readShort();
        int i = content.readableBytes();//get real msg length
        byte[] bytes = new byte[i];
        content.readBytes(bytes);//read remaining msg
        System.out.println(bytes);
        //将bytes转成protocol对应的消息体
        com.google.protobuf.GeneratedMessageV3 msgBody = null;
        try {
            //根据读取出来的msg_type转换成对应对象
            GameMsgProtocol.MsgCode msgCode = GameMsgProtocol.MsgCode.forNumber(msgType);
            switch (msgCode){
                case USER_ATTK_CMD:
                    msgBody = GameMsgProtocol.UserAttkCmd.parseFrom(bytes);
                    break;
                case USER_ENTRY_CMD:
                    msgBody = GameMsgProtocol.UserEntryCmd.parseFrom(bytes);
                    break;
                case USER_ATTK_RESULT:
            }
            ctx.fireChannelRead(msgBody);
        }catch (Exception ex){
            ctx.fireChannelRead(msg);
            System.out.println(ex.getMessage());
        }
    }
}
//前后端统一使用protobuf消息加密，对应服务端发挥给客户段也需要做消息加密

/**
 * 1.获取msg对应字节数组
 * 通过ByteBuff写入字节
 * 通过BinaryWebSocketFrame写入到channel
 *
 */
class GameMsgEecoder extends ChannelOutboundHandlerAdapter{
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        assert msg!=null;
        if (!(msg instanceof com.google.protobuf.GeneratedMessageV3)){
            super.write(ctx, msg, promise);
            return;
        }
        //信道广播后到客户端时会走加密
        if (msg instanceof  com.google.protobuf.GeneratedMessageV3){
            ByteBuf buffer = ctx.alloc().buffer();
            //写入字节
            buffer.writeShort(0);
            buffer.writeShort(GameMsgProtocol.UserEntryCmd.HEROAVATAR_FIELD_NUMBER);
            //websocket 二进制消息会从httpservercoder将消息转为BinaryWebSocketFrame
            //BinaryWebSocketFrame binaryWebSocketFram = (BinaryWebSocketFrame)msg;
            //所以写消息的时候也需要通过这个对象写出去
            byte[] bytes = ((GeneratedMessageV3) msg).toByteArray();
            buffer.writeBytes(bytes);
            BinaryWebSocketFrame socketFrame = new BinaryWebSocketFrame(buffer);
            super.write(ctx,socketFrame,promise);
        }
    }
}
