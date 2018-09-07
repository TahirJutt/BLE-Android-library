package com.brainx.brainxbledemoapp

import android.bluetooth.BluetoothGattCharacteristic
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.brainx.ble_android_manager.BLEConsoleServiceTemp
import com.brainx.ble_android_manager.BleManager
import kotlinx.android.synthetic.main.activity_serial_console.*
import java.util.*

class TestAcitivity : AppCompatActivity() {
    var bluetoothConsoleService: BLEConsoleServiceTemp? = null
    var TAG: String = "TestAcitivity"
    var bleManager: BleManager? = null
    var mDeviceName: String = ""
    var mDeviceAddress: String = ""
    var serviceUUID: UUID? = null
    var characteristicUUID: UUID? = null
    var characteristic: BluetoothGattCharacteristic? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_serial_console)


        mDeviceName = "Drive Mouse"//intent.getStringExtra("device-name")
        mDeviceAddress = "EE:27:11:F3:16:C8"//intent.getStringExtra("device-address")
        serviceUUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")//intent.getStringExtra("service-uuid"))
        characteristicUUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")//intent.getStringExtra("characteristic-uuid"))

        bleManager = BleManager.getInstance(this)
        bleManager!!.bindBleService(serviceConnection)


        bt_console_notify_check.setOnCheckedChangeListener { _, _ ->
            //bt_console_notify_check.isChecked = !bt_console_notify_check.isChecked
            if (bluetoothConsoleService != null && characteristic != null) {
                bluetoothConsoleService?.setCharacteristicNotification(characteristic!!, bt_console_notify_check.isChecked)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        bleManager!!.registerReceiver(dataBReceiver, BleManager.makeGattUpdateIntentFilter())

    }

    override fun onPause() {
        super.onPause()
        bleManager!!.unregisterReceiver(dataBReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        bleManager!!.unbindBleService()
    }

    val serviceConnection = object : BleManager.BleServiceConnectionListener {
        override fun onServiceDisconnected(p0: ComponentName?) {
            bluetoothConsoleService = null
        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        }

        override fun onBleServiceConnected(bleConsoleServiceTemp: BLEConsoleServiceTemp?) {
            bluetoothConsoleService = bleConsoleServiceTemp
            if (!bluetoothConsoleService!!.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth")
                finish()
            }
            bluetoothConsoleService!!.connect(mDeviceAddress)
            bluetoothConsoleService!!.setCharacteristicUUID(characteristicUUID)
            bluetoothConsoleService!!.setGattServiceUUID(serviceUUID)

        }
    }
    val dataBReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent!!.action
            when (action) {
                BLEConsoleServiceTemp.ACTION_GATT_CONNECTED -> {
                    val data = "Connected to Gatt Server"
                    bt_console_output.text = "${bt_console_output.text} \n $data"
                }
                BLEConsoleServiceTemp.ACTION_GATT_DISCONNECTED -> {
                    val data = "Disconnected from Gatt Server"
                    bt_console_output.text = "${bt_console_output.text} \n $data"
                }
                BLEConsoleServiceTemp.ACTION_DATA_AVAILABLE -> {
                    val data = "data read: ${intent.getStringExtra(BLEConsoleServiceTemp.EXTRA_DATA)}"
                    bt_console_output.text = "${bt_console_output.text}\n$data"
                }
                BLEConsoleServiceTemp.ACTION_DATA_WRITTEN -> {
                    val data = ">>># ${intent.getStringExtra(BLEConsoleServiceTemp.EXTRA_DATA)}"
                    bt_console_output.text = "${bt_console_output.text}$data"
                }
                BLEConsoleServiceTemp.ACTION_DATA_CHANGED -> {
                    val data = "<<<# ${intent.getStringExtra(BLEConsoleServiceTemp.EXTRA_DATA)}"
                    bt_console_output.text = "${bt_console_output.text}$data"
                }
                BLEConsoleServiceTemp.ACTION_GATT_SERVICES_DISCOVERED -> {
                    val data = "Services Discovered, Reading Gatt Characteristic ..."
                    characteristic = bluetoothConsoleService?.dataGattCharacteristic
                    bt_console_output.text = "${bt_console_output.text}\n$data\n"
                }
            }
        }
    }
}
