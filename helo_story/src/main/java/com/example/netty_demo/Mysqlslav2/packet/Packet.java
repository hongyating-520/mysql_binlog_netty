package com.example.netty_demo.Mysqlslav2.packet;

import com.example.netty_demo.Mysqlslav2.dataFormat.MysqlByteArrayInputStream;
import io.netty.buffer.ByteBuf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/*
 * 读取tcp消息头信息前4个字节
 */
public abstract class Packet {
    // https://dev.mysql.com/doc/internals/en/sending-more-than-16mbyte.html
    int MAX_LENGTH = 16777215;
    public static int length;
    public static byte[] headByte =  new byte[4];
    public static void readMsgHead(ByteBuf byteBuf){
        if (byteBuf.isReadable() && byteBuf.readableBytes()>4){
             byteBuf.readBytes(headByte);
            System.out.println(headByte[0]);
        }else {
            System.out.println("byteBuf 信息未准备好");
        }
    }
    //mysql: int<1>int<2>int<3>int<4>int<6>int<8>
    public int readFixedLengthInteger(byte[] source) {
        int i1 = 0;
        int num =0;
        for (int i = 0;i<source.length;i++){
            i1 = i1 | ((source[i] & 0xFF) << 8 * num);
            num++;
        }
        return i1;
    }
    //mysql:String
    public String readLengthEncodedString(byte[] source) {
        return new String(source);
    }
    //mysql:String<null>
    public String nullTerminatedString(MysqlByteArrayInputStream buffer) throws IOException {
        //for (int i = index.vale; i < data.length; i++) {
//            byte item = data[i];
//            if (item == DataType.STRING_NULL) {
//                break;
//            }
//            num++;
//            out.write(item);
//        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int b;(b = buffer.read()) != 0;){
            out.write(b);
        }
        return new String(out.toByteArray());
    }

}
