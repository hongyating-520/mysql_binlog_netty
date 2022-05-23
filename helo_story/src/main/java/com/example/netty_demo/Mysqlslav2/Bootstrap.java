package com.example.netty_demo.Mysqlslav2;

import com.example.netty_demo.MysqlSlave.*;
import com.example.netty_demo.Mysqlslav2.packet.*;
import com.example.netty_demo.Mysqlslav2.protocol.command.AuthCommand;
import com.example.netty_demo.Mysqlslav2.protocol.command.SqlCommand;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/*
 * @author ZZQ
 * @Date 2022/3/7 1:55 下午
 */
public class Bootstrap {
    private static EventLoopGroup group     = new NioEventLoopGroup();                              // 非阻塞IO线程组
    private static io.netty.bootstrap.Bootstrap boot      = new io.netty.bootstrap.Bootstrap();                                      // 主
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

                .handler(new ChannelInitializer() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        MsgHandler msgHandler = new MsgHandler();
                        ch.pipeline().addLast(msgHandler);// 添加消息处理器
                    }
                });
        try {
            ByteBuf bufPool= null;
            InetSocketAddress address = new InetSocketAddress("47.96.133.178", 3306);
            ChannelFuture sync = boot.connect(address).sync();
            Channel channel = sync.channel();
            String longText = channel.id().asLongText();
            System.out.println("当前连接channel："+longText);
            PSVMDEMO.await();
            BufferMsgPool bufferMsgPool = chManager.get(longText);
            ByteBuf msgBuffer = bufferMsgPool.byteBufPool;
            Packet.readMsgHead(msgBuffer);
            //1.接收mysql欢迎包
            GreetingPacket greetingPacket = new GreetingPacket(msgBuffer);
            //2.权限认证"halodb"
            AuthCommand authCommand = new AuthCommand("halodb", "root", "942464", greetingPacket.scramble, 0,greetingPacket.CharacterSet);
            byte[] bytes = authCommand.toByteArray();
            //发送消息题
             bufPool = Bootstrap.channelWriteAndRead(channel, bufferMsgPool, bytes);
            Packet.readMsgHead(bufPool);
            byte[] authResult = new byte[bufPool.readableBytes()];
            bufPool.readBytes(authResult);
            System.out.println(Arrays.toString(authResult));
            //正常这个位置返回07 00 00 02 00 00 00 02 00 00 00
            //我的是07 00 00 02 00 00 00 00 00 00 00
//            AuthSwitchPacket authSwitchPacket = new AuthSwitchPacket(bufPool);
//            byte[] authSwitchBody = authSwitchPacket.authenticateNativePasswordCommand();
//            if (authSwitchBody !=null){
//                bufPool = Bootstrap.channelWriteAndRead(channel, bufferMsgPool, authSwitchBody);
//                //认证返回如果还有异常则直接爆错
//                Packet.readMsgHead(msgBuffer);
//                byte[] error = new byte[msgBuffer.readableBytes()];
//                msgBuffer.readBytes(error);
//                ErrorPacket errorPacket = new ErrorPacket(error);
//                return;
//            }
            //开始读取binlog日志信息：执行show master status查看主从同步信息
            SqlCommand command = new SqlCommand("show master status",TextProtoCol.QUERY.ordinal());
            //[5, 26, 0, 0, 2, 3, 100, 101, 102, 0, 0, 0, 4, 70, 105, 108, 101, 0, 12, -1,
            bufPool = Bootstrap.channelWriteAndRead(channel, bufferMsgPool, command.toByteArray());
            SqlResultSetPacket resultSetPacket = new SqlResultSetPacket(bufPool,bufferMsgPool);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static ByteBuf channelWriteAndRead(Channel channel, BufferMsgPool bufferMsgPool, byte[] bytes) {
        channel.writeAndFlush(Unpooled.copiedBuffer(bytes));
        //读取
        ByteBuf bufPool = bufferMsgPool.byteBufPool;
        //服务端返回Authentication Method Switch Request Packet切换到的身份验证方法的名称/验证方法的初始身份验证数据
        System.out.println("-----main thread wait------");
        PSVMDEMO.await();
        System.out.println("-----main thread notify------");
        while (true){
            int i = bufPool.readableBytes();
            System.out.println("bufPool字节："+i);
            if (bufPool.isReadable()){
                System.out.println("bufPool字节isReadable");
                break;
            }
        }
        return bufPool;
    }
    public static byte[] channelRead(BufferMsgPool bufferMsgPool) {
        //读取
        ByteBuf bufPool = bufferMsgPool.byteBufPool;
        //服务端返回Authentication Method Switch Request Packet切换到的身份验证方法的名称/验证方法的初始身份验证数据
        System.out.println("-----main thread wait------");
        PSVMDEMO.await();
        System.out.println("-----main thread notify------");
        while (true){
            if (bufPool.isReadable()){
                break;
            }
        }
        byte[] bytes = new byte[bufPool.readableBytes()];
        bufPool.readBytes(bytes);
        return bytes;
    }
    //官网权限认证数据包信息
    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // 1. write client_flags 4
        //writeFixedLengthInteger(1 | 4 | 512 | 8192 | 32768 | 0x00010000, out,4); // remove
        // 2. write max_packet_size 4
        //writeFixedLengthInteger(DataType.MAX_PACKET_LENGTH, out,4);
        // 3. write charset_number 1
        //out.write(this.charsetNumber);
        // 4. reserverd 23
        //out.write(new byte[23]);
        //string[NUL]    username 0x00
        //out.write(this.username.getBytes());
//        out.write(DataType.STRING_NULL);
       /* if capabilities & CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA {
            lenenc-int     length of auth-response
            string[n]      auth-response
        } else if capabilities & CLIENT_SECURE_CONNECTION {
            1              length of auth-response
            string[n]      auth-response
        } else {
            string[NUL]    auth-response
        }
        暂时不设置密码
        */
        out.write(DataType.STRING_NULL);
        //设置数据库
        out.write("halodb".getBytes());
        out.write(DataType.STRING_NULL);
        //设置加密方式如果有指定
        return out.toByteArray();
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
            int byteNum = byteBuf.readableBytes();
            System.out.println("数据字节数："+byteNum);
            bufferMsgPool.writeBuffef(byteBuf);
            chManager.put(longText,bufferMsgPool);
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
            //chManager.remove(s);
            PSVMDEMO.anotify();
            super.channelInactive(ctx);
        }
    }

    public static int readUnsignedShortLittleEndian(byte[] data, int index) {
        int result = (data[index] & 0xFF) | ((data[index + 1] & 0xFF) << 8);
        return result;
    }

}


