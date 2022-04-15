package com.example.netty_demo.Mysqlslav2.protocol.command;

import com.example.netty_demo.MysqlSlave.DataType;
import com.example.netty_demo.Mysqlslav2.protocol.Command;

import java.io.IOException;

/*
 * @author ZZQ
 * @Date 2022/4/13 2:57 下午
 */
public class AuthenticateNativePasswordCommand extends Command {
    public String scramble;
    public String password;

    public AuthenticateNativePasswordCommand(String scramble, String password) {
        this.scramble = scramble;
        this.password = password;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        return DataType.passwordCompatibleWithMySQL411(password, scramble);
    }

}
