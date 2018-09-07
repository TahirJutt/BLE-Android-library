package com.brainx.ble_android_manager;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class BleManager {
    private static BleManager bleManager;
    private int REQUEST_ENABLE_BT = 1234;
    private BluetoothAdapter mBluetoothAdapter;
    BluetoothManager bluetoothManager;
    static Context context;
    boolean mScanning = false;
    private List<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    BleServiceConnectionListener mServiceConnection;

    private long SCAN_PERIOD = 10000;
    private Handler mHandler;


    public static BleManager getInstance(Context context1) {
        context = context1;
        if (bleManager == null) {
            bleManager = new BleManager();

        }
        return bleManager;
    }

    public static boolean isDeviceSupportBle(Context context) {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(context, "ble Not Support", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public BleManager initializesBluetoothAdapter() {

        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }
        return bleManager;
    }

    public static boolean isBluetoothEnable() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    public static void enableBluetooth(Context context, int REQUEST_ENABLE_BT) {
        if (!isBluetoothEnable()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity) context).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public List<BluetoothDevice> getUniqueList(List<BluetoothDevice> bluetoothDevices) {
        if (bluetoothDevices != null) {
            List<BluetoothDevice> bluetoothDevices1 = new ArrayList<>();
            List<String> stringList = new ArrayList<>();
            for (BluetoothDevice i : bluetoothDevices) {
                if (!stringList.contains(i.getAddress())) {
                    stringList.add(i.getAddress());
                    bluetoothDevices1.add(i);
                }
            }
            return bluetoothDevices1;
        }
        return null;
    }

    public static BluetoothDevice getDeviceMyAddress(List<BluetoothDevice> bluetoothDevices, String address) {

        for (BluetoothDevice i : bluetoothDevices) {
            if (address.contains(i.getAddress())) {
                return i;
            }
        }
        return null;
    }

    public static BluetoothDevice getDeviceMyName(List<BluetoothDevice> bluetoothDevices, String name) {

        for (BluetoothDevice i : bluetoothDevices) {
            if (name.contains(i.getName())) {
                return i;
            }
        }
        return null;
    }

    public BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            bluetoothDevices.add(bluetoothDevice);
        }
    };

    public void scanLeDevice(final BluetoothScanListener bluetoothScanListener) {
        if (!mScanning) {
            if (mHandler == null) {
                mHandler = new Handler();
            }
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    bluetoothScanListener.onScanfinish();
                    bluetoothDevices = getUniqueList(bluetoothDevices);
                    bluetoothScanListener.scanResult(bluetoothDevices);
                }
            }, SCAN_PERIOD);

            bluetoothDevices.clear();
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            bluetoothScanListener.onSacnStart();
        }
    }

    public void bindBleService(BleServiceConnectionListener serviceConnection) {
        mServiceConnection = serviceConnection;
        Intent gattServiceIntent = new Intent(context, BLEConsoleServiceTemp.class);
        context.bindService(gattServiceIntent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mServiceConnection.onBleServiceConnected(((BLEConsoleServiceTemp.LocalBinder) iBinder).getService());
                mServiceConnection.onServiceConnected(componentName, iBinder);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mServiceConnection.onServiceDisconnected(componentName);
            }
        }, Context.BIND_AUTO_CREATE);

    }

    public void unbindBleService() {
        if (mServiceConnection != null) {
            context.unbindService(mServiceConnection);
            mServiceConnection = null;
        }
    }

    public static IntentFilter makeGattUpdateIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEConsoleServiceTemp.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEConsoleServiceTemp.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEConsoleServiceTemp.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEConsoleServiceTemp.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BLEConsoleServiceTemp.ACTION_DATA_CHANGED);
        intentFilter.addAction(BLEConsoleServiceTemp.ACTION_DATA_WRITTEN);
        return intentFilter;
    }

    public void registerReceiver(BroadcastReceiver broadcastReceiver,IntentFilter intentFilter) {
        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    public void unregisterReceiver(BroadcastReceiver broadcastReceiver) {
        context.unregisterReceiver(broadcastReceiver);
    }


    public interface BluetoothScanListener {
        void onSacnStart();

        void onScanfinish();

        void scanResult(List<BluetoothDevice> bluetoothDevices);
    }

    public interface BleServiceConnectionListener extends ServiceConnection {
        void onBleServiceConnected(BLEConsoleServiceTemp bleConsoleServiceTemp);
    }
}
