package com.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;

/*
 * @author ZZQ
 * @Date 2022/2/6 4:42 下午
 */
public class MsgDecoderEncoder {
    public static void main(String[] args) throws InvalidProtocolBufferException {
        GameMsgProtocol.UserAttkCmd userAttkCmd = GameMsgProtocol.UserAttkCmd.parseFrom(new byte[1]);
    }
}
