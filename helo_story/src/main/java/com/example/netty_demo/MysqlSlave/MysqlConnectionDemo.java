package com.example.netty_demo.MysqlSlave;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/*
 * @author ZZQ
 * @Date 2022/3/7 1:55 下午
 */
public class MysqlConnectionDemo {
    private static EventLoopGroup group     = new NioEventLoopGroup();                              // 非阻塞IO线程组
    private static Bootstrap boot      = new Bootstrap();                                      // 主
    private static Map<String, BufferMsgPool> chManager = new ConcurrentHashMap<>();
    public static final CountDownLatch      runningLatch         = new CountDownLatch(1);


    //handler()跟ChildHandler的区别：一个是在连接之前就处理一个是客户端连接之后
    public static void main(String[] args) {
        boot.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                // 如果是延时敏感型应用，建议关闭Nagle算法
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                //
                .handler(new ChannelInitializer() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        MsgHandler msgHandler = new MsgHandler();
                        ch.pipeline().addLast(msgHandler);// 添加消息处理器
                    }
                });
        try {
            InetSocketAddress address = new InetSocketAddress("47.96.133.178", 3306);
            ChannelFuture sync = boot.connect(address).sync();
            Channel channel = sync.channel();
            String longText = channel.id().asLongText();
            System.out.println("当前连接channel："+longText);
            runningLatch.await();
            BufferMsgPool bufferMsgPool = chManager.get(longText);
            ClientAutMSG autMSG = ClientAutMSG.fromBytes(bufferMsgPool.byteBufPool);
            autMSG.toString();
            byte[] bytes = autMSG.toBytes();
            Thread.sleep(2000);
            channel.writeAndFlush(Unpooled.copiedBuffer(bytes));
            ByteBuf bufPool = bufferMsgPool.byteBufPool;
            System.out.println("-----main thread wait------");
            PSVMDEMO.await();
            System.out.println("-----main thread notify------");
            while (true){
                if (bufPool.isReadable()){
                      break;
                }
            }
            int i = bufPool.readableBytes();
            byte[] msgHandler = new byte[4];
            bufPool.readBytes(msgHandler);
            for (byte b : msgHandler) {
                System.out.println("msgHandler:"+b);
            }
            byte[] retuenOk = new byte[i-4];
            bufPool.readBytes(retuenOk);
            //如果第一个字节是1111 1111 ff则表示是ERR_Packet
            /**
             * Type	            Name	             Description
             * int<1>	       header	    [ff] header of the ERR packet
             * int<2>	      error_code	          error-code
             * if capabilities & CLIENT_PROTOCOL_41 {
             * string[1]	sql_state_marker	# marker of the SQL State
             * string[5]	sql_state	                SQL State
             * }
             * string<EOF>	error_message	human readable error message
             */
            if (retuenOk[0]==-1){
                System.out.println("-----receive EOF_Package--------");
                PassInt index = PassInt.give(1);
                int errorCode = FromByte.readFixedLengthInteger(retuenOk, index, 2);
                String sql_state_marker = FromByte.LengthEncodedString(retuenOk, index, 1);
                String sql_state = FromByte.LengthEncodedString(retuenOk, index, 5);
                String error_message = FromByte.LengthEncodedString(retuenOk, index, retuenOk.length-index.vale);
                //new ErrorPacket(errorCode,sql_state_marker,sql_state,error_message).toString();
            }
            System.out.println("-----retuenOk--------");
            for (byte b : retuenOk) {
                System.out.println(b);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //无法确定mysql tcp协议报文内容 msg类型字段：按照canal方式：添加buffer池存储消息进行消费
    static class MsgHandler extends SimpleChannelInboundHandler<ByteBuf> {
        private BufferMsgPool bufferMsgPool;
        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
            Channel channel = channelHandlerContext.channel();
            String longText = channel.id().asLongText();
            System.out.println("接收到消息——————："+longText);
            //接收数据包
            bufferMsgPool.channel=channel;
            bufferMsgPool.writeBuffef(byteBuf);
            chManager.put(longText,bufferMsgPool);
            runningLatch.countDown();
            PSVMDEMO.anotify();
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("-----连接上了----");
            String s = ctx.channel().id().asLongText();
            System.out.println(s);
            //每次新连接都创建一个新的
            bufferMsgPool = new BufferMsgPool();
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("-----连接移除----");
            String s = ctx.channel().id().asLongText();
            System.out.println(s);
            chManager.remove(s);
            super.channelInactive(ctx);
        }
    }

    public static int readUnsignedShortLittleEndian(byte[] data, int index) {
        int result = (data[index] & 0xFF) | ((data[index + 1] & 0xFF) << 8);
        return result;
    }

}


