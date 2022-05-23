package com.example.netty_demo.MysqlSlave;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;

/*
 * @author ZZQ
 * @Date 2022/3/9 10:32 上午
 *
 ________________________________________
｜discardBytes ｜ readBytes | writeBytes |
|_________read^index__write^index_______|
writeXxx(xxx value) 这组方法将不同类型的数据写到buf里，同时将writerIndex往前移适当的距离
readXxx() 这组方法从buf里读出某种类型的数据，同时将readerIndex往前移适当的距离
skipBytes(int length) 将readerIndex往前移指定的距离
setXxx(int index, xxx value) 这组方法将不同类型的数据写到buf的指定位置
getXxx(int index) 这组方法从buf的指定位置读出一个某种类型的数据
readerIndex()/writerIndex() 访问readerIndex和writerIndex
readerIndex(int)/writerIndex(int) 设置readerIndex和writerIndex
readableBytes() 返回可读区域的字节数
writableBytes() 返回可写区域的字节数
clear() 清除buf（把readerIndex和writerIndex都设为0）
discardReadBytes() 扔掉已读数据
 */
public class BufferMsgPool {
    public Channel channel;
    public ByteBuf byteBufPool   = PooledByteBufAllocator.DEFAULT.directBuffer(1024 * 1024); // 缓存大小

    public void writeBuffef(ByteBuf msg){
        synchronized (this){
            byteBufPool.writeBytes(msg);
        }
    }

    public void clean(){
        byteBufPool.clear();
    }
}
