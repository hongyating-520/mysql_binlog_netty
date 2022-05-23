package com.example.netty_demo.Mysqlslav2;

import com.example.netty_demo.MysqlSlave.TextProtoCol;
import com.example.netty_demo.Mysqlslav2.packet.ErrorPacket;
import com.example.netty_demo.Mysqlslav2.packet.GreetingPacket;
import com.example.netty_demo.Mysqlslav2.protocol.command.AuthCommand;
import com.example.netty_demo.Mysqlslav2.protocol.command.SqlCommand;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/*
 * @author ZZQ
 * @Date 2022/4/18 3:10 下午
 */
public class SocketDemo {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("47.96.133.178", 3306), (int)  TimeUnit.SECONDS.toMillis(3));
        InputStream inputStream = socket.getInputStream();
        byte[] header = new byte[4];
        inputStream.read(header,0,4);
        for (byte b : header) {
            System.out.println(b);
        }
        byte[] body = new byte[header[0]];
        inputStream.read(body, 0, header[0]);
        GreetingPacket greetingPacket = new GreetingPacket(body);
        AuthCommand authCommand = new AuthCommand(null, "root", "942464", greetingPacket.scramble, 0,greetingPacket.CharacterSet);
        byte[] bytes = authCommand.toByteArray();
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(bytes);
        outputStream.flush();
        InputStream returnBody = socket.getInputStream();
        int read = returnBody.read(header);
        for (byte b : header) {
            System.out.println(b);
        }
        byte[] reBody = new byte[header[0]];
        inputStream.read(reBody, 0, header[0]);
        System.out.println("权限认证结果："+Arrays.toString(reBody));
        SqlCommand command = new SqlCommand("show master status", TextProtoCol.QUERY.ordinal());
        byte[] sloveBody = command.toByteArray();
        outputStream.write(sloveBody);
        outputStream.flush();
        InputStream returnBody1 = socket.getInputStream();
        int read1 = returnBody1.read(header);
        for (byte b : header) {
            System.out.println(b);
        }
        byte[] reBody1 = new byte[888];

        System.out.println(inputStream.available());
        inputStream.read(reBody1, 0, 100);
        System.out.println(Arrays.toString(reBody1));
        //跳过所有eof包
        while (true){
            System.out.println("---------");
            returnBody1.read(header);
            System.out.println("eof头内容："+Arrays.toString(header));
            byte[] eof = new byte[header[0]];
            inputStream.read(eof, 0, header[0]);
            byte b = eof[0];
            System.out.println("eof头字段"+b);
            if (b == (byte) 0xFE){
                break;
            }
        }
        returnBody1.read(header);
        byte[] binlog = new byte[header[0]];
        inputStream.read(binlog, 0, header[0]);
        while (true){
            System.out.println("---------");
            returnBody1.read(header);
            byte[] eof = new byte[header[0]];
            inputStream.read(eof, 0, header[0]);
            byte b = eof[0];
            System.out.println("binlog:"+Arrays.toString(binlog));
            System.out.println(new String(binlog));

        }
    }
}
