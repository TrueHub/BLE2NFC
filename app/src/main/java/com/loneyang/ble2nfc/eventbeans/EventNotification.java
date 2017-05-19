package com.loneyang.ble2nfc.eventbeans;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by user on 2017/4/21.
 */

public class EventNotification implements Parcelable{
    private String type;
    private boolean getOver;

    public EventNotification() {
    }

    public EventNotification(String type, boolean getOver) {
        this.type = type;
        this.getOver = getOver;
    }

    protected EventNotification(Parcel in) {
        type = in.readString();
        getOver = in.readByte() != 0;
    }

    public static final Creator<EventNotification> CREATOR = new Creator<EventNotification>() {
        @Override
        public EventNotification createFromParcel(Parcel in) {
            return new EventNotification(in);
        }

        @Override
        public EventNotification[] newArray(int size) {
            return new EventNotification[size];
        }
    };

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isGetOver() {
        return getOver;
    }

    public void setGetOver(boolean getOver) {
        this.getOver = getOver;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeByte((byte) (getOver ? 1 : 0));
    }
}
