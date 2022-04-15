package com.example.netty_demo.Mysqlslav2.packet;

import com.example.netty_demo.MysqlSlave.BufferMsgPool;
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
        Packet.readMsgHead(bufPool);
        byte[] bytes = new byte[bufPool.readableBytes()];
        bufPool.readBytes(bytes);
        byte sign = bytes[0];
        if (sign == 0XFF){
            new ErrorPacket(bytes);
        }
        switch (sign){
            case (byte) 0XFF:new ErrorPacket(bytes);break;
            case (byte)0XFE :case 0X00:
                System.out.println("ok packet recive~~~~~~!");
                break;
            default://TextResultset包含：columnType;ColumnDefinition;TextResultRow
                System.out.println(Arrays.toString(bytes));
                byte[] secendBody = Bootstrap.channelRead(bufferMsgPool);
                System.out.println(Arrays.toString(secendBody));
        }
//        MysqlByteArrayInputStream buffer = new MysqlByteArrayInputStream(bytes);
//        List<String> values = new LinkedList<String>();
//        while (buffer.available() > 0) {
//            values.add(buffer.readLengthEncodedString());
//        }
//        this.values = values.toArray(new String[values.size()]);
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
