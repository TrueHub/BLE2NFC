package com.loneyang.ble2nfc.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.loneyang.ble2nfc.eventbeans.Comm2GATT;
import com.loneyang.ble2nfc.eventbeans.EventNotification;
import com.loneyang.ble2nfc.moudle.CommandPool;
import com.loneyang.ble2nfc.utils.ConstantPool;
import com.loneyang.ble2nfc.utils.DataUtils;
import com.loneyang.ble2nfc.utils.EventUtil;

import java.util.List;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.loneyang.ble2nfc.eventbeans.Comm2GATT.EventType.READ_BLOCK_INFO_SINGLE;
import static com.loneyang.ble2nfc.eventbeans.Comm2GATT.EventType.READ_INFO_ALL;

/**
 * Created by Dell on 2017-4-16.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class GATTService extends Service {

    public static final String DEVICE_ID = ConstantPool.DEVICEID_1;
    private BluetoothAdapter mBluetoothAdapter;
    private LeScanCallback_LOLLIPOP mScanCallBack_lollipop;//5.0以上
    private LeScanCallback_JELLY_BEAN mScanCallBack_jelly;//4.3以上
    private BluetoothLeScanner mBluetoothScanner;
    private BluetoothDevice mTarget;
    private CommandPool commandPool;
    private BLEGATTCallBack mGattCallback;
    private Handler handler;
    private boolean mScanning;
    private BluetoothGattCharacteristic vibrationChar;
    private boolean isConnected = false;
    private int cameCount = -1;
    private BluetoothGatt mGatt;

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventUtil.unregister(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventUtil.register(this);
        handler = new Handler();
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
        } else {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        searchDevice();
    }

    private void searchDevice() {
        Log.i("MSL", "searchDevice: method running");

        //打开蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
            return;
        }
        if (mGattCallback == null) mGattCallback = new BLEGATTCallBack();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0以上
            if (mScanCallBack_lollipop == null)
                mScanCallBack_lollipop = new LeScanCallback_LOLLIPOP();
            mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();
            mBluetoothScanner.startScan(mScanCallBack_lollipop);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {//4.3 ~ 5.0
            if (mScanCallBack_lollipop == null)
                mScanCallBack_jelly = new LeScanCallback_JELLY_BEAN();
            mBluetoothAdapter.startLeScan(mScanCallBack_jelly);

        }

        mScanning = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mScanning) {
                    Log.d("MSL", "Stop Scan， Time Out");
                    mScanning = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mBluetoothScanner.stopScan(mScanCallBack_lollipop);
                    }
                }
            }
        }, 1000 * 10);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class LeScanCallback_LOLLIPOP extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (result == null) {
                return;
            }
            Log.i("MSL", "onScanResult: 扫描到设备：" + result.getDevice().getName() + "\n" + result.getDevice().getAddress());

            EventUtil.post(result.getDevice());

            if (result.getDevice().getName() != null && DEVICE_ID.equals(result.getDevice().getName())) {
                mTarget = result.getDevice();
                if (!isConnected) {
                    mTarget.connectGatt(GATTService.this, false, mGattCallback);
                    isConnected = true;
                }
                mBluetoothScanner.stopScan(mScanCallBack_lollipop);

                if (mBluetoothAdapter.getBondedDevices().contains(mTarget)) {
                    EventUtil.post("目标设备已配对");
                }
            }
        }
    }

    private class LeScanCallback_JELLY_BEAN implements BluetoothAdapter.LeScanCallback {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device.getName() != null && device.getName().equals(DEVICE_ID)) {
                mTarget = device;
                if (!isConnected) {
                    mTarget.connectGatt(GATTService.this, false, mGattCallback);
                    isConnected = true;
                }
                mBluetoothAdapter.stopLeScan(mScanCallBack_jelly);

                if (mBluetoothAdapter.getBondedDevices().contains(mTarget)) {
                    EventUtil.post("目标设备已配对");
                }
            }
        }
    }

    private class BLEGATTCallBack extends BluetoothGattCallback {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            mGatt = gatt;
            if (status == 0) {
                mGatt.discoverServices();
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //连上了新设备
                commandPool = new CommandPool(GATTService.this, gatt);
                new Thread(commandPool).start();
                EventUtil.post(new EventNotification(DEVICE_ID, true));
                Log.i("MSL", "Connected to GATT server 连接成功");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //设备断开
                EventUtil.post(new EventNotification(DEVICE_ID, false));
                Log.i("MSL", "Disconnected from GATT server");
                mGatt.close();
                stopSelf();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> serviceList;
            //发现新的设备
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("MSL", "onServicesDiscovered: 发现新的设备");
            }
            serviceList = gatt.getServices();
            if (serviceList != null) {
                Log.i("MSL", "onServicesDiscovered: " + serviceList);
                Log.i("MSL", "serviceList NUM ： " + serviceList.size());
                for (BluetoothGattService bleService : serviceList) {
                    List<BluetoothGattCharacteristic> characteristicList = bleService.getCharacteristics();
                    Log.i("MSL", "扫描到Service: " + bleService.getUuid());
                    for (BluetoothGattCharacteristic characteristic :
                            characteristicList) {
                        Log.i("MSL", "characteristic: " + characteristic.getUuid() + "\n" + characteristic.getProperties());
                        if (characteristic.getUuid().equals(ConstantPool.UUID_NOTIFY)) {

                            Log.i("MSL", "onServicesDiscovered: " + characteristic.getUuid());
                            gatt.setCharacteristicNotification(characteristic, true);
                            List<BluetoothGattDescriptor> descriptorList = characteristic.getDescriptors();
                            for (BluetoothGattDescriptor descriptor :
                                    descriptorList) {
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                gatt.writeDescriptor(descriptor);
                            }
                            descriptorList.get(0).setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptorList.get(0));
                        }

                        if (characteristic.getUuid().equals(ConstantPool.UUID_WRITE)) {
                            vibrationChar = characteristic;
                        }
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d("MSL", "onCharacteristicRead: " + characteristic.getUuid());
            commandPool.onCommandCallbackComplete();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d("MSL", "onCharacteristicWrite: " + status + "," + characteristic.getUuid());
            byte[] data = characteristic.getValue();
            Log.d("MSL", "onCharacteristicWrite: " + DataUtils.bytes2hex(data));
            readData(data);
            commandPool.onCommandCallbackComplete();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d("MSL", "onCharacteristicChanged: " + characteristic.getUuid());
            if (characteristic.getUuid().equals(ConstantPool.UUID_NOTIFY)) {
                commandPool.onCommandCallbackComplete();
                byte[] data = characteristic.getValue();
                Log.d("MSL", "onCharacteristicChanged: " + DataUtils.bytes2hex(data));
                readData(data);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

            Log.d("MSL", "onDescriptorRead: " );
            commandPool.onCommandCallbackComplete();
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            byte[] data = descriptor.getValue();
            Log.d("MSL", "onDescriptorWrite: " + DataUtils.bytes2hex(data));
            commandPool.onCommandCallbackComplete();
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            commandPool.onCommandCallbackComplete();
            Log.d("MSL", "onReliableWriteCompleted: ");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            commandPool.onCommandCallbackComplete();
            Log.d("MSL", "onReadRemoteRssi: ");
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            commandPool.onCommandCallbackComplete();
            Log.d("MSL", "onMtuChanged: ");
        }

    }


    //MainActivity控制这里
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void btnClick(Comm2GATT comm2GATT) {
        switch (comm2GATT.getEventType()) {
            case READ_SYS_INFO:
                Log.d("MSL", "btnClick: sys info");
                commandPool.addCommand(CommandPool.Type.write,ConstantPool.SYS_INFO,vibrationChar);
                break;
            case READ_BLOCK_INFO_SINGLE:
                commandPool.addCommand(CommandPool.Type.write,ConstantPool.BLOCK_INFO_SINGLE,vibrationChar);
                break;
            case READ_INFO_ALL:
                commandPool.addCommand(CommandPool.Type.write,ConstantPool.INFO_ALL,vibrationChar);
                break;
            case CONN:
                searchDevice();
                break;
            case DISCONN:
                EventUtil.post("断开GATT连接");
                Log.i("MSL", "Disconnected from GATT server by user");
//                mGatt.disconnect();
//                EventUtil.post(new EventNotification(DEVICE_ID, false));
                break;
        }
    }

/*    @Subscribe(threadMode = ThreadMode.MAIN)
    public void btnClick(String str) {
        switch (str){
            case "READ_SYS_INFO":
                Log.d("MSL", "btnClick: sys info");
                commandPool.addCommand(CommandPool.Type.write,ConstantPool.SYS_INFO,vibrationChar);
                break;
            case "READ_BLOCK_INFO_SINGLE":
                commandPool.addCommand(CommandPool.Type.write,ConstantPool.BLOCK_INFO_SINGLE,vibrationChar);
                break;
            case "READ_INFO_ALL":
                commandPool.addCommand(CommandPool.Type.write,ConstantPool.INFO_ALL,vibrationChar);
                break;
        }
    }*/


    private void readData(byte[] data) {

    }

}
