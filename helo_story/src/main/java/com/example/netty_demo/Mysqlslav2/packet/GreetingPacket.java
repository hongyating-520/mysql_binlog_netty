package com.example.netty_demo.Mysqlslav2.packet;

import com.example.netty_demo.Mysqlslav2.dataFormat.MysqlByteArrayInputStream;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/*
 * @author ZZQ
 * @Date 2022/4/7 4:29 下午
 */
public class GreetingPacket extends Packet {
    public int protocolVersion;
    public String serverVersion;
    public long threadId;
    public String scramble;
    public int serverCapabilities;
    public int CharacterSet;
    public int serverStatus;
    public String pluginProvidedData;

    public GreetingPacket(ByteBuf msgBuffer) throws IOException {
        byte[] bytes = new byte[msgBuffer.readableBytes()];
        msgBuffer.readBytes(bytes);
        MysqlByteArrayInputStream buffer = new MysqlByteArrayInputStream(bytes);
        this.protocolVersion = this.readFixedLengthInteger(buffer.read(0, 1));
        this.serverVersion = this.nullTerminatedString(buffer);
        this.threadId = this.readFixedLengthInteger(buffer.read(0, 4));
        String scramblePrefix = this.nullTerminatedString(buffer);
        //capability与高两位字节capability组合成完整capability
        this.serverCapabilities = this.readFixedLengthInteger(buffer.read(0, 2));
        //服务端字符集
        this.CharacterSet = this.readFixedLengthInteger(buffer.read(0, 1));
        //服务端状态
        this.serverStatus = this.readFixedLengthInteger(buffer.read(0, 2));
        buffer.skip(13); // reserved
        this.scramble = scramblePrefix + this.nullTerminatedString(buffer);
        if (buffer.available() > 0) {
            this.pluginProvidedData = this.nullTerminatedString(buffer);
        }
        System.out.println(toString());
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this);
    }

}
