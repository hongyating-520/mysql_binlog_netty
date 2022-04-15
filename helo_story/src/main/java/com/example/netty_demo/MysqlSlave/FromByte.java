package com.example.netty_demo.MysqlSlave;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

/*
 * @author ZZQ
 * @Date 2022/3/8 10:43 上午
 */
public class FromByte {
    /**
     * string<lenenc> int<x>格式
     * @param data byte数据
     * @param index 当前读取的起始下标
     * @param x 读取长度
     * @return
     */
    public static byte[] int_string_x(byte[] data, PassInt index,int x) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = index.vale; i < index.vale+x; i++) {
            byte item = data[i];
            out.write(item);
        }
        index.vale=index.vale+x;
        return out.toByteArray();
    }
    public static String int_string_x(byte[] data, PassInt index,int x,Boolean simple) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = index.vale; i < index.vale+x; i++) {
            byte item = data[i];
            out.write(item);
        }
        index.vale=index.vale+x;
        byte[] bytes = out.toByteArray();
        if (simple){
            return new String(bytes);
        }else {
            long capability = 0;
            //连接数id：这么运算的
            for (int i = 0; i < bytes.length; i++) {
                capability = capability|(bytes[i] & 0xFF)<<i*8;
            }
            return String.valueOf(capability);
        }
    }


    //string<null> 格式数据
    public static byte[] string_null(byte[] data, PassInt index) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int num =1;
        for (int i = index.vale; i < data.length; i++) {
            byte item = data[i];
            if (item == DataType.STRING_NULL) {
                break;
            }
            num++;
            out.write(item);
        }
        index.vale= index.vale+num;
        return out.toByteArray();
    }


    public static int readFixedLengthInteger(byte[] source,PassInt index, int length) {
        int i1 = 0;
        int num =0;
        for (int i = index.vale;i<index.vale+length;i++){
            i1 = i1 | ((source[i] & 0xFF) << 8 * num);
            num++;
        }
        index.vale= index.vale+num;
        return i1;
    }

    public static String LengthEncodedString(byte[] source,PassInt index, int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(source, index.vale, bytes, 0, length);
        index.vale= index.vale+length;
        return new String(bytes);
    }
}
class PassInt {
    public int vale;

    private PassInt(int vale) {
        this.vale = vale;
    }
    static PassInt give(int vale){
        return new PassInt(vale);
    }
    public PassInt increase(int num){
        this.vale = vale+num;
        return this;
    }
    public PassInt increase(){
       return increase(1);
    }
    @Override
    public String toString() {
        return "PassInt{" +
                "vale=" + vale +
                '}';
    }
}
