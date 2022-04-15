package com.example.netty_demo.Mysqlslav2.protocol.command;

import com.example.netty_demo.MysqlSlave.Capability;
import com.example.netty_demo.MysqlSlave.DataType;
import com.example.netty_demo.Mysqlslav2.dataFormat.MysqlByteArrayoutputStream;
import com.example.netty_demo.Mysqlslav2.protocol.Command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/*
 * @author ZZQ
 * 权限认证命令：用户接收到欢迎包之后返回认证信息
 */
public class AuthCommand extends Command {
    //database :schema
    public String schema;
    public String username;
    public String password;
    //加密盐
    public String salt;
    public int clientCapabilities;
    public int charsetSet;

    public AuthCommand(String schema, String username, String password, String salt, int clientCapabilities,int charsetSet) {
        this.schema = schema;
        this.username = username;
        this.password = password;
        this.salt = salt;
        this.clientCapabilities = clientCapabilities;
        this.charsetSet = charsetSet;
    }

    //封装主体信息后再封装body信息
    @Override
    public byte[] toByteArray() throws IOException {
        MysqlByteArrayoutputStream mysqlOut = new MysqlByteArrayoutputStream();
        //1.设置响应包标识
        if (this.clientCapabilities == 0) {
            clientCapabilities = Capability.CLIENT_LONG_FLAG | Capability.CLIENT_PROTOCOL_41 | Capability.CLIENT_SECURE_CONNECTION ;
            if (schema != null) {
                clientCapabilities |= Capability.CLIENT_CONNECT_WITH_DB;
            }
        }
        mysqlOut.writeFixedLengthInteger(clientCapabilities, 4);
        // maximum packet length:设置为0？还是8M
        mysqlOut.writeFixedLengthInteger(0, 4);
        mysqlOut.writeFixedLengthInteger(charsetSet, 1);
        mysqlOut.writeByte(new byte[23]);
        mysqlOut.writeStringNull(username);
        /**安全方式返回
         * else if capabilities & CLIENT_SECURE_CONNECTION {
         * 1              length of auth-response
         * string[n]      auth-response
         */
        byte[] passwordSHA1 = "".equals(password) ? new byte[0] : DataType.passwordCompatibleWithMySQL411(password, salt);
        mysqlOut.writeFixedLengthInteger(passwordSHA1.length, 1);
        mysqlOut.writeByte(passwordSHA1);
        if (schema != null) {
            mysqlOut.writeStringNull(schema);
        }
        byte[] body = mysqlOut.toBytes();
        // packet length
        out.writeFixedLengthInteger(body.length, 3);
        //packetNumber
        out.writeFixedLengthInteger(1, 1);
        out.write(body, 0, body.length);
        return out.toByteArray();
    }

}
