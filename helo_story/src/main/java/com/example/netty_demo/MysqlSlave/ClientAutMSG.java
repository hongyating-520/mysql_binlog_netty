package com.example.netty_demo.MysqlSlave;

import io.netty.buffer.ByteBuf;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/*
 * @author ZZQ
 * @Date 2022/3/8 4:47 下午
 * 客户端初次握手连接
 */
public class ClientAutMSG {

    int  clientCapability = Capability.CLIENT_LONG_PASSWORD | Capability.CLIENT_LONG_FLAG
            | Capability.CLIENT_PROTOCOL_41 | Capability.CLIENT_INTERACTIVE
            | Capability.CLIENT_TRANSACTIONS | Capability.CLIENT_MULTI_STATEMENTS  |Capability.CLIENT_CONNECT_WITH_DB;


    public int   protocalV;
    public String   mysqlVersion;
    public int   connection_id;
    public String   capabilityFlagLow;
    public String   capabilityFlagUp;
    public int   auth_plugin_data_length;
    public byte[]   reserverd;
    public int   serverLanguage;
    public int   serverStatus;
    public int   ClientLanguage =33;
    private int   capability;
    public String username = "root";
    public String password = "942464";
    public byte   charsetNumber = 33;
    public String databaseName;
    public int    serverCapabilities;
    public byte[] auth_plugin_data_part_1;
    public byte[] auth_plugin_data_part_2;
    public String authPluginName;

    public static ClientAutMSG fromBytes(ByteBuf ByteBuf) {
        byte[] statusByte = new byte[4];
        ByteBuf.readBytes(statusByte);
        byte b = statusByte[0];
        System.out.println("----收到的消息长度："+b);
        byte[] data = new byte[ByteBuf.readableBytes()];
        ByteBuf.readBytes(data);
        ClientAutMSG clientAutMSG = new ClientAutMSG();
        PassInt index = PassInt.give(0);
        byte[] bytes = FromByte.int_string_x(data, index, 1);
        clientAutMSG.protocalV = bytes[0];

        //从3.21.0 开始 Protocol::HandshakeV10 发送，但它仍然支持 Protocol::HandshakeV9 编译时选项。
        // 1. read protocol_version
        byte[] mysqlVersion = FromByte.string_null(data, index);
        clientAutMSG.mysqlVersion = new String(mysqlVersion);
        // 2. read server_version
        String connection_id = FromByte.int_string_x(data, index, 4, false);
        clientAutMSG.connection_id = Integer.valueOf(connection_id);
        byte[] auth_plugin_data_part_1 = FromByte.int_string_x(data, index, 8);
        clientAutMSG.auth_plugin_data_part_1 = auth_plugin_data_part_1;
        index.increase();
        //第2位字节capability与高两位字节capability组合成完整capability
        String capability_flags = FromByte.int_string_x(data, index, 2, false);
        clientAutMSG.capabilityFlagLow = capability_flags;
        System.out.println("capability_flags:"+capability_flags);
        //if more data in the packet:
        if (data.length > index.vale) {
            byte[] server_language = FromByte.int_string_x(data, index, 1);
            clientAutMSG.serverLanguage = server_language[0];
            String server_status = FromByte.int_string_x(data, index, 2, false);
            clientAutMSG.serverStatus = Integer.valueOf(server_status);
            String capability_flags2 = FromByte.int_string_x(data, index, 2, false);
            clientAutMSG.capabilityFlagUp = capability_flags;
            //获取权限码：s
            int capability = clientAutMSG.setClientCapability();
            System.out.println("capability:"+capability+",capability_flags:"+capability_flags+",capability_flags2:"+capability_flags2);
            byte auth_plugin_data = 0;
            if ((capability & Capability.CLIENT_PLUGIN_AUTH) != 0){
                auth_plugin_data = FromByte.int_string_x(data, index, 1)[0];
                clientAutMSG.auth_plugin_data_length = Integer.valueOf(auth_plugin_data);
            }
            byte[] reserverd = FromByte.int_string_x(data, index, 10);
            clientAutMSG.reserverd = reserverd;
            if ((capability & Capability.CLIENT_SECURE_CONNECTION) != 0) {
                int len = Math.max(13, auth_plugin_data - 8);
                byte[] auth_plugin_data_part_2 = FromByte.int_string_x(data, index, len);
                clientAutMSG.auth_plugin_data_part_2 = auth_plugin_data_part_2;
                byte[] auth_name = FromByte.string_null(data,index);
                String authName = new String(auth_name);
                clientAutMSG.authPluginName = authName;
            }
        }
        return clientAutMSG;
    }

    public int getClientCapability() {
        return capability;
    }

    public int setClientCapability() {
        this.capability  = Integer.valueOf(capabilityFlagUp) << 16 | Integer.valueOf(capabilityFlagLow);
        return capability;
    }

    @Override
    public String toString() {
        String content = "ClientAutMSG{" +
                "protocalV=" + protocalV +
                ", mysqlVersion='" + mysqlVersion + '\'' +
                ", connection_id=" + connection_id +
                ", capabilityFlagLow='" + capabilityFlagLow + '\'' +
                ", capabilityFlagUp='" + capabilityFlagUp + '\'' +
                ", auth_plugin_data_length=" + auth_plugin_data_length +
                ", reserverd=" + Arrays.toString(reserverd) +
                ", serverLanguage=" + serverLanguage +
                ", serverStatus=" + serverStatus +
                ", ClientLanguage=" + ClientLanguage +
                ", capability=" + capability +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", charsetNumber=" + charsetNumber +
                ", databaseName='" + databaseName + '\'' +
                ", serverCapabilities=" + serverCapabilities +
                ", auth_plugin_data_part_1=" + Arrays.toString(auth_plugin_data_part_1) +
                ", auth_plugin_data_part_2=" + Arrays.toString(auth_plugin_data_part_2) +
                ", authPluginName='" + authPluginName + '\'' +
                '}';
        System.out.println(content);
        return content;
    }


    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // 1. write client_flags 4
        writeFixedLengthInteger(1 | 4 | 512 | 8192 | 32768 | 0x00010000, out,4); // remove
        // 2. write max_packet_size 4
        writeFixedLengthInteger(DataType.MAX_PACKET_LENGTH, out,4);
        // 3. write charset_number 1
        out.write(this.charsetNumber);
        // 4. reserverd 23
        out.write(new byte[23]);
        //string[NUL]    username 0x00
        out.write(this.username.getBytes());
//        out.write(DataType.STRING_NULL);
       /* if capabilities & CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA {
            lenenc-int     length of auth-response
            string[n]      auth-response
        } else if capabilities & CLIENT_SECURE_CONNECTION {
            1              length of auth-response
            string[n]      auth-response
        } else {
            string[NUL]    auth-response
        }
        暂时不设置密码
        */
        out.write(DataType.STRING_NULL);
        //设置数据库
        out.write("halodb".getBytes());
        out.write(DataType.STRING_NULL);
        //设置加密方式如果有指定
        return out.toByteArray();
    }

    public static void writeFixedLengthInteger(int data, ByteArrayOutputStream out,int length) {
        for (int i = 0;i<length;i++){
            int by = (data >>> i * 8) & 0xff;
            out.write(by);
        }
    }




    public static void main(String[] args) {
        //- 80 3 0 0   944
        System.out.println(Integer.toBinaryString(-80));
        System.out.println(Integer.toBinaryString(3));

        int result = (-80 & 0xFF) | ((3 & 0xFF) << 8);
        System.out.println(result);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int a = 1024*1024*1024;
        writeFixedLengthInteger(a,out,4);
        for (byte b : out.toByteArray()) {
            System.out.println(b);
        }
    }

}
