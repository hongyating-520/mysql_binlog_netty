package com.example.netty_demo.Mysqlslav2.dataFormat;

import com.example.netty_demo.MysqlSlave.DataType;

import java.io.*;

/*
 * mysql 数据包读取
 */
public class MysqlByteArrayoutputStream extends OutputStream {

    private ByteArrayOutputStream outputStream;
    private Integer peek;
    private int blockLength = -1;


    public MysqlByteArrayoutputStream() {
        this.outputStream = new java.io.ByteArrayOutputStream();
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
    }

    public byte[] toByteArray() {
        // todo: whole approach feels wrong
        if (outputStream instanceof java.io.ByteArrayOutputStream) {
            return outputStream.toByteArray();
        }
        return new byte[0];
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }
    public void writeFixedLengthInteger(int data, int length) throws IOException {
        for (int i = 0; i < length; i++) {
            int by = (data >>> i * 8) & 0xff;
            outputStream.write(by);
        }
    }
    public void writeByte(byte[] bytes) throws IOException {
        outputStream.write(bytes);
    }
    public void writeStringNull(String code) throws IOException {
        outputStream.write(code.getBytes());
        outputStream.write(DataType.STRING_NULL);
    }
    public byte[] toBytes(){
        return outputStream.toByteArray();
    }

}
