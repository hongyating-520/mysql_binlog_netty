package com.example.netty_demo.Mysqlslav2.protocol;

import com.example.netty_demo.MysqlSlave.DataType;
import com.example.netty_demo.Mysqlslav2.dataFormat.MysqlByteArrayoutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/*
 * @author ZZQ
 * @Date 2022/4/12 4:27 下午
 */
public abstract class Command {
    public MysqlByteArrayoutputStream out = new MysqlByteArrayoutputStream();
    public abstract byte[] toByteArray() throws IOException;

    public void writeInteger(int value, int length, ByteArrayOutputStream buffer) throws IOException {
        for (int i = 0; i < length; i++) {
            buffer.write(0x000000FF & (value >>> (i << 3)));
        }
    }
}
