package com.zzx.locations;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Created by wanderFly on 2017/4/17.
 */

public class ByteUtil {
    /**
     * 求字节数组的所有字节的和(即校验和)是否为零
     *
     * @param bytes 需要求和的字节数组
     * @return 校验和是否为0
     */
    public static boolean bytesCheckSum(byte[] bytes) {
        int value = 0;
       /* int length = bytes.length;
        for (int i = 0; i < length; i++) {
            value += bytes[i];
        }*/
        for (byte b: bytes) {
            value+=b;
        }
        return value == 0;
    }

    /**
     * 将字节数组转换为表示十六进制的字符串 每个字节表示的十六进制值之间--有--空格
     *
     * @param bytes 需要转换的字节数组
     * @return 与之对应的十六进制字符串
     */
    public static String bytes2hex(byte[] bytes) {
        final String HEX = "0123456789ABCDEF";
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (byte b : bytes) {
            //取出字节的高4位，然后与 0x0f与运算，得到0~15的数据，通过HEX.charAt(0~15)，即为十六进制数.
            sb.append(HEX.charAt((b >> 4) & 0x0f));
            //取出字节的低4位，然后与 0x0f与运算，得到0~15的数据，通过HEX.charAt(0~15)，即为十六进制数.
            sb.append(HEX.charAt(b & 0x0f));
            sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * 将字节数组转换为表示十六进制的字符串 每个字节表示的十六进制值之间--每十六个字符后换行
     *
     * @param bytes 需要转换的字节数组
     * @return 与之对应的十六进制字符串
     */
    public static String bytes2hex16(byte[] bytes) {
        final String HEX = "0123456789ABCDEF";
        int len = bytes.length;
        int multiple = (len % 16 == 0) ? (len / 16) : (len / 16 + 1);
        StringBuilder sb = new StringBuilder(bytes.length * 3 + multiple * 2);
        for (int i = 0; i < len; i++) {
            //取出字节的高4位，然后与 0x0f与运算，得到0~15的数据，通过HEX.charAt(0~15)，即为十六进制数.
            sb.append(HEX.charAt((bytes[i] >> 4) & 0x0f));
            //取出字节的低4位，然后与 0x0f与运算，得到0~15的数据，通过HEX.charAt(0~15)，即为十六进制数.
            sb.append(HEX.charAt(bytes[i] & 0x0f));
            sb.append(" ");
            if ((i + 1) % 16 == 0) {
                sb.append("\n");
                if ((i + 1) % 32 == 0) {
                    sb.append("\r");
                }
            }
        }
        return sb.toString();
    }

    /**
     * 将字节数组转换为表示十六进制的字符串  每个字节表示的十六进制值之间--没有--空格
     *
     * @param bytes 需要转换的字节数组
     * @return 与之对应的十六进制字符串
     */
    public static String bytes2hexNo(byte[] bytes) {
        final String HEX = "0123456789ABCDEF";
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            //取出字节的高4位，然后与 0x0f与运算，得到0~15的数据，通过HEX.charAt(0~15)，即为十六进制数.
            sb.append(HEX.charAt((b >> 4) & 0x0f));
            //取出字节的低4位，然后与 0x0f与运算，得到0~15的数据，通过HEX.charAt(0~15)，即为十六进制数.
            sb.append(HEX.charAt(b & 0x0f));
        }
        return sb.toString();
    }


    /**
     * java中将4字节的byte数组转成一个int值的工具方法如下：
     * (注:长度不足为4字节的会在高位补零)
     *
     * @param b 需要转换的字节数组
     * @return int
     */
    public static int byteArrayToInt(byte[] b) {
        byte[] a = new byte[4];
        int i = a.length - 1, j = b.length - 1;
        for (; i >= 0; i--, j--) {//从b的尾部(即int值的低位)开始copy数据
            if (j >= 0)
                a[i] = b[j];
            else
                a[i] = 0;//如果b.length不足4,则将高位补0
        }
        int v0 = (a[0] & 0xff) << 24;//&0xff将byte值无差异转成int,避免Java自动类型提升后,会保留高位的符号位
        int v1 = (a[1] & 0xff) << 16;
        int v2 = (a[2] & 0xff) << 8;
        int v3 = (a[3] & 0xff);
        return v0 + v1 + v2 + v3;
    }

    /**
     * int到byte[] 大端方式
     *
     * @param i 需要转换的int值
     * @return 转换后的int数组
     */
    public static byte[] intToByteArrayBig(int i) {
        byte[] result = new byte[4];
        //由高位到低位
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    /**
     * int到byte[] 小端方式
     *
     * @param i 需要转换的int值
     * @return 转换后的int数组
     */
    public static byte[] intToByteArraySmall(int i) {
        byte[] result = new byte[4];
        //由高位到低位
        result[3] = (byte) ((i >> 24) & 0xFF);
        result[2] = (byte) ((i >> 16) & 0xFF);
        result[1] = (byte) ((i >> 8) & 0xFF);
        result[0] = (byte) (i & 0xFF);
        return result;
    }

    /**
     * Convert hex string to byte[]
     *
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    public static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
    // char转byte

    public static byte[] getBytes(char[] chars) {
        Charset cs = Charset.forName("UTF-8");
        CharBuffer cb = CharBuffer.allocate(chars.length);
        cb.put(chars);
        cb.flip();
        ByteBuffer bb = cs.encode(cb);

        return bb.array();

    }

    // byte转char

    public static char[] getChars(byte[] bytes) {
        Charset cs = Charset.forName("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        bb.flip();
        CharBuffer cb = cs.decode(bb);

        return cb.array();
    }


}
