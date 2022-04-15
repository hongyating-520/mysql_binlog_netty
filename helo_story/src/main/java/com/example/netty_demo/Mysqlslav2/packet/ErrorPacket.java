package com.example.netty_demo.Mysqlslav2.packet;

import com.example.netty_demo.Mysqlslav2.dataFormat.MysqlByteArrayInputStream;
import io.netty.buffer.ByteBuf;

import java.io.IOException;

/**
 * 异常包
 */
public class ErrorPacket extends Packet {
    public int errorCode;
    public String sqlStateMarker;
    public String sqlState;
    public String errorMessage;


    public ErrorPacket(byte[] bytes) throws IOException {
        MysqlByteArrayInputStream buffer = new MysqlByteArrayInputStream(bytes);
        int first = this.readFixedLengthInteger(buffer.read(0, 1));
        if (first == -1){
            this.errorCode = this.readFixedLengthInteger(buffer.read(0, 2));
            this.sqlStateMarker =this.readLengthEncodedString(buffer.read(0, 1));
            this.sqlState =this.readLengthEncodedString(buffer.read(0, 5));
            this.errorMessage = this.readLengthEncodedString(buffer.read(0, buffer.available()));
        }
        System.out.println(toString());
    }


    @Override
    public String toString() {
        System.out.println("EOFPackage{" +
                "errorCode=" + errorCode +
                ", sqlStateMarker='" + sqlStateMarker + '\'' +
                ", sqlState='" + sqlState + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}');
        return null;
    }
}
