package com.example.netty_demo.MysqlSlave;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MySQL Constants.<br>
 * constants that is used in mysql server.<br>
 *
 * @author fujohnwang
 */
public abstract class DataType {

    public static final int  MAX_PACKET_LENGTH                  = 8*1024*1024;
    public static final int  HEADER_PACKET_LENGTH_FIELD_LENGTH  = 3;
    public static final int  HEADER_PACKET_LENGTH_FIELD_OFFSET  = 0;
    public static final int  HEADER_PACKET_LENGTH               = 4;
    public static final int  HEADER_PACKET_NUMBER_FIELD_LENGTH  = 1;

    //
    public static final byte STRING_NULL   = 0x00;
    public static final byte DEFAULT_PROTOCOL_VERSION           = 0x0a;

    public static final int  FIELD_COUNT_FIELD_LENGTH           = 1;

    public static final int  EVENT_TYPE_OFFSET                  = 4;
    public static final int  EVENT_LEN_OFFSET                   = 9;

    public static final long DEFAULT_BINLOG_FILE_START_POSITION = 4;

    //mysql加密
    public static byte[] passwordCompatibleWithMySQL411(String password, String salt) {
        MessageDigest sha;
        try {
            sha = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] passwordHash = sha.digest(password.getBytes());
        return xor(passwordHash, sha.digest(union(salt.getBytes(), sha.digest(passwordHash))));
    }

    private static byte[] xor(byte[] a, byte[] b) {
        byte[] r = new byte[a.length];
        for (int i = 0; i < r.length; i++) {
            r[i] = (byte) (a[i] ^ b[i]);
        }
        return r;
    }

    private static byte[] union(byte[] a, byte[] b) {
        byte[] r = new byte[a.length + b.length];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }
}
