package com.example.netty_demo.Mysqlslav2.protocol.command;

import com.example.netty_demo.Mysqlslav2.dataFormat.MysqlByteArrayoutputStream;
import com.example.netty_demo.Mysqlslav2.protocol.Command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/*
* mysql文本协议：例子：
* COM_QUERY：
ACOM_QUERY用于向服务器发送立即执行的基于文本的查询。
服务器使用 ResponseCOM_QUERY回复 数据包 。 COM_QUERY
查询字符串的长度取自数据包长度 - 1。
* 数据包内容
1  [03] COM_QUERY
string[EOF]    the query the server shall execute
* 14.6.2 COM_QUIT
COM_QUIT：
响应：连接关闭或 OK_Packet
01 00 00 00 01
*
* */
public class SqlCommand extends Command {
    public String sql;
    public int textProtocol;
    public SqlCommand(String sql,int textProtocol) {
        this.sql = sql;
        this.textProtocol = textProtocol;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        MysqlByteArrayoutputStream mysqlOut = new MysqlByteArrayoutputStream();
        mysqlOut.writeFixedLengthInteger(this.textProtocol, 1);
        mysqlOut.writeString(this.sql);
        byte[] body = mysqlOut.toByteArray();
        MysqlByteArrayoutputStream buffer = new MysqlByteArrayoutputStream();
        buffer.writeFixedLengthInteger(body.length, 3); // packet length
        buffer.writeFixedLengthInteger(0, 1);
        buffer.write(body, 0, body.length);
        return buffer.toByteArray();
    }
}
