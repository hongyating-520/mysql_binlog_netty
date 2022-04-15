package com.example.netty_demo.Mysqlslav2.packet;

import com.example.netty_demo.Mysqlslav2.dataFormat.MysqlByteArrayInputStream;
import com.example.netty_demo.Mysqlslav2.protocol.Command;
import com.example.netty_demo.Mysqlslav2.protocol.command.AuthenticateNativePasswordCommand;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.Arrays;

/*
 * @author ZZQ
 * //接收auth权限校验之后返回的权限相关
 * 状态 （一）—— 0xfe
auth_method_name ( string.NUL ) -- 要切换到的身份验证方法的名称
auth_method_data ( string.EOF ) -- 该身份验证方法的初始身份验证数据
 */
public class AuthSwitchPacket extends Packet{
    public int sign = 0x00;
    public String authName;
    //认证加密盐信息
    public String scramble;

    public AuthSwitchPacket(ByteBuf msgBuffer) throws IOException {
        byte[] bytes = new byte[msgBuffer.readableBytes()];
        //如果返回00标识认证成功不需要额外认证
        if (bytes[0] != (byte) 0x00 /* ok */) {
            //第一个字节如果是-1的话标识异常
            if (bytes[0] == (byte) 0xFF /* error */) {
                ErrorPacket errorPacket = new ErrorPacket(bytes);
                System.exit(1);
            } else if (bytes[0] == (byte) 0xFE) {
                MysqlByteArrayInputStream buffer = new MysqlByteArrayInputStream(bytes);
                //读取掉第一个标识
                sign = this.readFixedLengthInteger(buffer.read(0, 1));
                authName = this.nullTerminatedString(buffer);
                //如果客户端发送了mysql_native_password 响应，
                // 但服务器有 mysql_old_password该用户的响应，它将要求客户端切换到mysql_old_password
                //如果是相反的情况（mysql --default-auth=mysql_old_password针对 mysql_native_password用户），客户端将响应 mysql_native_password插件的回复：
                if ("mysql_native_password".equals(authName)){
                    //返回认证信息
                    scramble = this.nullTerminatedString(buffer);
                }else {
                    throw new RuntimeException("Unexpected authentication result (" + bytes[0] + ")");
                }
            } else {
                throw new RuntimeException("Unexpected authentication result (" + bytes[0] + ")");
            }
        }else {
            System.out.println("ok packet:"+Arrays.toString(bytes));
        }
    }
    public byte[] authenticateNativePasswordCommand() throws IOException {
        if (sign == 0xFE){
            AuthenticateNativePasswordCommand command = new AuthenticateNativePasswordCommand(scramble, "942464");
            return command.toByteArray();
        }
        return null;
    }
}
