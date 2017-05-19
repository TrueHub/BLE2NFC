package com.loneyang.ble2nfc.utils;

/**
 * Created by user on 2017/4/6.
 * 有关设备的常量池
 */

public class ConstantPool {

    //BluetoothProfile
    public static final String DEVICEID_1 = "USR-BLE101";
    public static final String DEVICEID_2 = "KUWO_K1";
    public static final java.util.UUID UUID_NOTIFY = java.util.UUID.fromString("0003cdd1-0000-1000-8000-00805f9b0131");
    public static final java.util.UUID UUID_WRITE = java.util.UUID.fromString("0003cdd2-0000-1000-8000-00805f9b0131");

    private static final byte HEAD = (byte) 0x5b;

    /**
     * [0] :包头       5b
     * [1] :数据长度   03
     * [2] :标志       02
     * [3] :命令       2b
     * [4] :参数       00
     */
    public static final byte[] SYS_INFO = new byte[]{HEAD, (byte) 0x03, (byte) 0x02, (byte) 0x2b, (byte) 0x00};

    /**
     * [0] :包头           5b
     * [1] :数据长度       03
     * [2] :标志           02
     * [3] :命令           20
     * [4] :参数 (block号) 00       共有43个block
     */
    public static final byte[] BLOCK_INFO_SINGLE = new byte[]{HEAD, (byte) 0x03, (byte) 0x02, (byte) 0x20, (byte) 0x00};

    /**
     * [0] :包头       5b
     * [1] :数据长度   03
     * [2] :标志       ea
     * [3] :命令       eb
     * [4] :参数       ec
     */
    public static final byte[] INFO_ALL = new byte[]{HEAD, (byte) 0x03, (byte) 0xea, (byte) 0xeb, (byte) 0xec};
}
