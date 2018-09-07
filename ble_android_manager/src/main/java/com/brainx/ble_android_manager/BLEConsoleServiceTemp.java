package com.brainx.ble_android_manager;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


import java.util.UUID;

import static android.bluetooth.BluetoothProfile.STATE_CONNECTING;

public class BLEConsoleServiceTemp extends Service {
    public static final String ACTION_GATT_CONNECTED = "org.pampanet.bluetooth.le.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = "org.pampanet.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = "org.pampanet.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_DATA_AVAILABLE = "org.pampanet.bluetooth.le.ACTION_DATA_AVAILABLE";
    public static final String ACTION_DATA_WRITTEN = "org.pampanet.bluetooth.le.ACTION_DATA_WRITTEN";
    public static final String ACTION_DATA_CHANGED = "org.pampanet.bluetooth.le.ACTION_DATA_CHANGED";
    public static final String ACTION_DATA_SERVICE_ID = "org.pampanet.bluetooth.le.ACTION_DATA_SERVICE_ID";
    public static final String ACTION_DATA_CHAR_ID = "org.pampanet.bluetooth.le.ACTION_DATA_CHAR_ID";
    public static final String EXTRA_DATA = "org.pampanet.bluetooth.le.EXTRA_DATA";
    public static String TAG = "BLEConsoleService1";
    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private String mBluetoothDeviceAddress = null;
    private BluetoothGatt mBluetoothGatt = null;
    private int mConnectionState = BluetoothProfile.STATE_DISCONNECTED;

    public UUID dataGattCharacteristicUUID = UUID.randomUUID();
    public UUID dataGattServiceUUID = UUID.randomUUID();
    public BluetoothGattCharacteristic dataGattCharacteristic = null;

    public class LocalBinder extends Binder {
        public BLEConsoleServiceTemp getService() {
            return BLEConsoleServiceTemp.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *                *
     *                *
     * @return Return true if the connection is initiated successfully. The connection result
     * *         is reported asynchronously through the
     * *         `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * *         callback.
     */
    public boolean connect(String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }
public void setCharacteristicUUID( UUID gattCharacteristicUUID){
    dataGattCharacteristicUUID=gattCharacteristicUUID;
}
    public void setGattServiceUUID( UUID gattServiceUUID){
        dataGattServiceUUID=gattServiceUUID;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            String intentAction;
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED: {
                    intentAction = ACTION_GATT_CONNECTED;
                    mConnectionState = BluetoothProfile.STATE_CONNECTED;
                    broadcastUpdate(intentAction);
                    Log.i(TAG, "Connected to GATT server.");
                    // Attempts to discover services after successful connection.
                    Log.i(TAG, "Attempting to start discoveryService discovery:" + mBluetoothGatt.discoverServices());

                }
                break;
                case BluetoothProfile.STATE_DISCONNECTED: {
                    intentAction = ACTION_GATT_DISCONNECTED;
                    mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
                    Log.i(TAG, "Disconnected from GATT server.");
                    broadcastUpdate(intentAction);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            switch (status) {
                case BluetoothGatt.GATT_SUCCESS: {
                    dataGattCharacteristic = gatt.getService(dataGattServiceUUID).getCharacteristic(dataGattCharacteristicUUID);
                    if (dataGattCharacteristic != null) {
                        gatt.readCharacteristic(dataGattCharacteristic);
                        setCharacteristicNotification(dataGattCharacteristic, true);
                    } else
                        Log.i(TAG, "characteristic is null, can't read it");
                    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "data read successfully");
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);

            } else {
                Log.i(TAG, "data couldn't be read, status= " + status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //Toast.makeText(this@BLEDiscoveryService,"data written successfully",Toast.LENGTH_SHORT).show()
                Log.i(TAG, "data written successfully");
                broadcastUpdate(ACTION_DATA_WRITTEN, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            //Log.i(this@BLEConsoleService.javaClass.simpleName,"Characteristic Changed")
            //Toast.makeText(this@BLEDiscoveryService,"data changed successfully",Toast.LENGTH_SHORT).show()
            Log.i(TAG, "data changed successfully");
            broadcastUpdate(ACTION_DATA_CHANGED, characteristic);

        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.i(TAG, "descriptor written successfully");
        }


    };

    private void broadcastUpdate(String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(String action,
                                 BluetoothGattCharacteristic characteristic) {
        Intent intent = new Intent(action);
        byte[] data = characteristic.getValue();
        if (data != null && data.length != 0) {
            //val stringBuilder = StringBuilder(data.size)
            for (byte byteChar : data) {
                //stringBuilder.append(String.format("%02X ", byteChar))
                intent.putExtra(EXTRA_DATA, new String(data) + "\n");//+ stringBuilder.toString())
                intent.putExtra(ACTION_DATA_SERVICE_ID, characteristic.getService().getUuid().toString());
                intent.putExtra(ACTION_DATA_CHAR_ID, characteristic.getUuid().toString());
            }
            sendBroadcast(intent);
        }
    }


    public Boolean sendData(String msg, BluetoothGattCharacteristic characteristic) {
        characteristic.setValue(msg);
        Log.i(TAG, "Writing message to characteristic");
        return mBluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     *                       *
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              Boolean enabled) {
        UUID notifyUUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(notifyUUID);
        if (descriptor != null) {
            if (enabled) {
                //descriptor = BluetoothGattDescriptor(notifyUUID, BluetoothGattDescriptor.PERMISSION_WRITE)
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                Log.i(TAG, "point 1");
            } else {
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                Log.i(TAG, "point 2");
            }
        } else {
            descriptor = new BluetoothGattDescriptor(notifyUUID, BluetoothGattDescriptor.PERMISSION_WRITE);
            Log.i(TAG, "point 3");
        }
        mBluetoothGatt.writeDescriptor(descriptor);
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        //mBluetoothGatt!!.writeCharacteristic(characteristic)
    }


    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }


    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

}
