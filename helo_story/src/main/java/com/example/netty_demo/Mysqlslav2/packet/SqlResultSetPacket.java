package com.example.netty_demo.Mysqlslav2.packet;

import com.example.netty_demo.MysqlSlave.BufferMsgPool;
import com.example.netty_demo.MysqlSlave.PSVMDEMO;
import com.example.netty_demo.Mysqlslav2.Bootstrap;
import com.example.netty_demo.Mysqlslav2.dataFormat.MysqlByteArrayInputStream;
import io.netty.buffer.ByteBuf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

/*
 * @author ZZQ
 * @Date 2022/4/13 4:16 下午
 * https://dev.mysql.com/doc/internals/en/com-query-response.html#packet-ProtocolText::Resultset
 * 响应数据包是一个元数据包，它可以是以下之一
ERR_Packet,OK_Packet,Protocol::LOCAL_INFILE_Request,ProtocolText::Resultset
 * packet-ProtocolText::Resultset：
 * 包含列计数协议：：LengthCodedInteger的数据包；column_count * Protocol::ColumnDefinition packets
 * 如果未设置客户端功能标志CLIENT_DEPRECATE_EOF，则EOF数据包
One or more ProtocolText::ResultsetRow packets, each containing column_count values
ERR_Packet in case of error. Otherwise: If the CLIENT_DEPRECATE_EOF client capability flag is set, OK_Packet; else EOF_Packet.
If the SERVER_MORE_RESULTS_EXISTS flag is set in the last EOF_Packet or (if the CLIENT_DEPRECATE_EOF capability flag is set) OK_Packet, another ProtocolText::Resultset will follow (see Multi-resultset).
 *
 *     column_count>0
 *       👇     👆
 *  column_def 👆
 *      👇     👆
 *     EOF    👆
 *     👇     👆
 *    row    👆
 *  👇   👇  👆
 * ERR   E O F
 */
public class SqlResultSetPacket extends Packet{
    private String[] values;

    public SqlResultSetPacket(ByteBuf bufPool, BufferMsgPool bufferMsgPool) throws IOException {
        bufPool.readBytes(Packet.headByte);
        byte sign = Packet.headByte[0];
        System.out.println("sign："+sign);
        byte[] yizhix = new byte[275];
        bufPool.readBytes(yizhix);
        System.out.println("第一次回调：1000，5，26，0，0/："+Arrays.toString(yizhix));
        System.out.println(new String(yizhix));
        //过滤eof
        PSVMDEMO.anotify();
        while (true){
            ByteBuf byteBufPool = bufferMsgPool.byteBufPool;
            while (byteBufPool.isReadable()){
                byteBufPool.readBytes(Packet.headByte);
                System.out.println("eof:"+Arrays.toString(Packet.headByte));
                short i = byteBufPool.readShort();
                System.out.println("eof第一个字段:"+i);
                if (i == (byte) 0xFE){
                    break;
                }
            }

        }
    }

    public String[] getValues() {
        return values;
    }

    public String getValue(int index) {
        return values[index];
    }

    public int size() {
        return values.length;
    }

}
