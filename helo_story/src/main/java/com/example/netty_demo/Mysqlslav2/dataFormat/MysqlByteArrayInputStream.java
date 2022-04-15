package com.example.netty_demo.Mysqlslav2.dataFormat;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/*
 * mysql 数据包读取
 */
public class MysqlByteArrayInputStream extends InputStream {

    private InputStream inputStream;
    private Integer peek;
    private int blockLength = -1;

    @Override
    public int read(){
        try {
            return inputStream.read();
        }catch (Exception e){
            return 0;
        }
    }
    public MysqlByteArrayInputStream(byte[] bytes) {
        this.inputStream = new java.io.ByteArrayInputStream(bytes);
    }

    public byte[] read(int offset,int length) throws IOException {
        byte[] bytes = new byte[length];
        int remaining = length;
        //保证全部读取完
        while (remaining != 0) {
            int read = inputStream.read(bytes, offset + length - remaining, remaining);
            if (read == -1) {
                throw new EOFException();
            }
            remaining -= read;
        }
        return bytes;
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

}
