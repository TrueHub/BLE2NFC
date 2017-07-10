package com.loneyang.ble2nfc.utils;

/**
 * Created by user on 2017/4/7.
 */

public class DataUtils {
    public static byte[] int2Bytes(int num) {
        byte[] byteNum = new byte[4];
        for (int ix = 0; ix < 4; ++ix) {
            int offset = 32 - (ix + 1) * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xff);
        }
        return byteNum;
    }

    public static int byte2Int(byte byteNum) {
        int num = 0;
            num <<= 8;
            num |= (byteNum & 0xff);
        return num;
    }

    public static int bytes2IntUnsigned(byte[] byteNum) {
        int num = 0;
        for (int ix = 0; ix < byteNum.length; ++ix) {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);
        }
        return num;
    }

    public static int bytes2IntSigned(byte[] byteNum) {
        int num = 0;
        for (int ix = 0; ix < byteNum.length; ++ix) {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);
        }
        if (num > 32767) num -= 65536;
        return num;
    }

    public static byte int2OneByte(int num) {
        return (byte) (num & 0x000000ff);
    }

    public static int Byte2Int(byte byteNum) {
        return byteNum > 0 ? byteNum : (128 + (128 + byteNum));
    }

    public static byte[] long2Bytes(long num) {
        byte[] byteNum = new byte[8];
        for (int ix = 0; ix < 8; ++ix) {
            int offset = 64 - (ix + 1) * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xff);
        }
        return byteNum;
    }

    public static long bytes2Long(byte[] byteNum) {
        long num = 0;
        for (int ix = 0; ix < byteNum.length; ++ix) {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);
        }
        return num;
    }

    public static String byte2hex(byte b) {
        String result = Integer.toHexString(b & 0xFF);
        if (result.length() == 1) {
            result = '0' + result;
        }
        return result;
    }
    /**
     * 将byte[]转化成16进制的String命令
     */
    public static String bytes2hex(byte[] b) {
        // String Buffer can be used instead
        String hs = "0x";
        String stmp = "";

        for (int n = 0; n < b.length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF)) ;

            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }

            if (n < b.length - 1) {
                hs = hs + ",0x";
            }
        }
        return hs;
    }

    public static void main(String[] args) {

        String ss = "0x00,0xc8,0xc4,0x9b,0x80,0x38,0x05,0xc8,0x54,0xe0";
        ss = ss.substring(5 , ss.length() - 5);
        System.out.println(ss);

        /*long time = System.currentTimeMillis();
        System.out.println(time);
        byte[] long2Bytes1 = long2Bytes(time);
        for (int ix = 0; ix < long2Bytes1.length; ++ix) {
            System.out.print(long2Bytes1[ix] + " ");
        }

        int num = 129;
        System.out.println("测试的int值为:" + num);

        byte[] int2bytes = DataUtils.int2Bytes(num);
        System.out.printf("int转成bytes: ");
        for (int i = 0; i < 4; ++i) {
            System.out.print(int2bytes[i] + " ");
        }
        System.out.println();

        int bytes2int = DataUtils.bytes2IntUnsigned(int2bytes);
        System.out.println("bytes转行成int: " + bytes2int);

        byte int2OneByte = DataUtils.int2OneByte(num);
        System.out.println("int转行成one byte: " + int2OneByte);
        System.out.println("int转行成one byte: " + (byte)0x127);

        int oneByte2Int = DataUtils.Byte2Int(int2OneByte);
        System.out.println("one byte转行成int: " + oneByte2Int);
        System.out.println("one byte转行成int 2 : " + byte2Int(int2OneByte));
        System.out.println();

        long longNum = 286331153;
        System.out.println("测试的long值为：" + longNum);

        byte[] long2Bytes = DataUtils.long2Bytes(longNum);
        System.out.printf("long转行成bytes: ");
        for (int ix = 0; ix < long2Bytes.length; ++ix) {
            System.out.print(long2Bytes[ix] + " ");
        }
        System.out.println();


        byte[] newByte = new byte[] {4,6,23,6};
        long bytes2Long = DataUtils.bytes2Long(newByte);
        System.out.println("bytes转行成long: " + bytes2Long);

        byte byteNum = (byte)0x45;
        System.out.print("byte为："+byteNum);
        int byte2Int = byte2Int(byteNum);
        System.out.println("byte转行成int: " + byte2Int);*/
    }

}
