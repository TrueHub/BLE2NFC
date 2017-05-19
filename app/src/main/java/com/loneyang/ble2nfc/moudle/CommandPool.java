package com.loneyang.ble2nfc.moudle;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.os.Build;
import android.util.Log;


import com.loneyang.ble2nfc.utils.ConstantPool;

import java.util.LinkedList;

/**
 * Created by user on 2017/4/17.
 */

public class CommandPool implements Runnable{

    public enum Type{
        setNotification, read, write
    }
    private Context context;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic characteristic;
    private int index = 0;
    private LinkedList<Command> pool;
    private Command commandToExc;
    private boolean isCompleted = false;
    private boolean isDone = false;

    public CommandPool(Context context, BluetoothGatt gatt) {
        this.context = context;
        this.gatt = gatt;
        pool = new LinkedList<>();
    }

    public void addCommand(Type type, byte[] value, BluetoothGattCharacteristic target) {
        Command command = new Command(type, value, target);
        pool.offer(command);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (pool.peek() == null) {
                commandToExc = null;
                continue;
            } else if (!isDone) {
                commandToExc = pool.peek();
                isDone = execute(commandToExc.getType(), commandToExc.getValue(), commandToExc.getTarget());
                Log.i("MSL", commandToExc.getId() + ",命令结果:" + isDone);
            } else if (isCompleted && isDone) {
                Log.i("MSL", commandToExc.getId() + "命令执行完成");
                pool.poll();
                isCompleted = false;
                isDone = false;
            }
        }
    }

    private boolean execute(Type type, byte[] value, BluetoothGattCharacteristic target) {
        boolean result = false;
        switch (type) {
            case setNotification:
                result = enableNotification(true, target);
                break;
            case read:
                result = readCharacteristic(target);
                break;
            case write:
                result = writeCharacteristic(target, value);
                break;
        }
        return result;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean enableNotification(boolean enable, BluetoothGattCharacteristic characteristic) {

        if (gatt == null || characteristic == null)
            return false;
        if (!gatt.setCharacteristicNotification(characteristic, enable))
            return false;

        BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(ConstantPool.UUID_NOTIFY);

        if (clientConfig == null)
            return false;
        if (enable) {
            clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            clientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        return gatt.writeDescriptor(clientConfig);
    }

    public void onCommandCallbackComplete() {
        isCompleted = true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean readCharacteristic(BluetoothGattCharacteristic characteristic) {
        boolean result = gatt.readCharacteristic(characteristic);
        return result;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] command) {
        characteristic.setValue(command);
        boolean result = gatt.writeCharacteristic(characteristic);
        return result;
    }

    private class Command {
        private int id;
        private boolean state = false;
        private byte[] value;
        private Type type;
        private BluetoothGattCharacteristic target;


        Command(Type type, byte[] value, BluetoothGattCharacteristic target) {
            this.value = value;
            this.target = target;
            this.type = type;
            id = index;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
//                Log.i("MSL",index + "命令创建，UUID: " + target.getUuid().toString());
            index++;

        }

        int getId() {
            return id;
        }

        void setSsate(boolean state) {
            this.state = state;
        }

        boolean getState() {
            return state;
        }

        BluetoothGattCharacteristic getTarget() {
            return target;
        }

        byte[] getValue() {
            return value;
        }

        Type getType() {
            return type;
        }
    }
}
