package com.loneyang.ble2nfc.eventbeans;

/**
 * Created by user on 2017/5/19.
 */

public class Comm2GATT {
    public enum EventType{
        /** 连接*/
        CONN,

        /** 断开*/
        DISCONN,

        /** 读取系统信息*/
        READ_SYS_INFO,

        /** 读取单个block信息*/
        READ_BLOCK_INFO_SINGLE,

        /** 读取全部信息*/
        READ_INFO_ALL
    }

    private int parm;
    private EventType eventType ;

    public Comm2GATT( EventType eventType , int parm) {
        this.eventType = eventType;
        this.parm = parm;
    }

    public int getParm() {
        return parm;
    }

    public void setParm(int parm) {
        this.parm = parm;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
}
