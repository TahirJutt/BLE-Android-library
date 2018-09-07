package com.brainx.brainxbledemoapp

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.brainx.ble_android_manager.BleManager
import kotlinx.android.synthetic.main.activity_main.*

import java.util.ArrayList

class MainActivity : AppCompatActivity(), View.OnClickListener {
   private var bluetoothDevices: MutableList<BluetoothDevice>? = null

    private val MAC_ADDRESS = "EE:27:11:F3:16:C8"
    private var driveMouseDevice: BluetoothDevice? = null
    private var bleManager: BleManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (BleManager.isDeviceSupportBle(this)) {
            bluetoothDevices = ArrayList()
            setListener()
            bleManager = BleManager.getInstance(this)
            bleManager!!.initializesBluetoothAdapter()
            BleManager.enableBluetooth(this, 1234)

        }

    }

    private fun setListener() {
        scan_button?.setOnClickListener(this)
        connection_button_view?.setOnClickListener(this)

    }


    override fun onClick(view: View) {
        if (view.id == R.id.scan_button) {
            if (BleManager.isBluetoothEnable()) {
                bleManager!!.scanLeDevice(bluetoothScanListener)
            }
        } else if (view.id == R.id.connection_button_view) {
            val intent = Intent(this, TestAcitivity::class.java)
            startActivity(intent)
        }


    }

    val bluetoothScanListener = object : BleManager.BluetoothScanListener {
        override fun onSacnStart() {
               scan_button?.setText("Scanning...")
                connection_button_view?.setVisibility(View.GONE)
           }

        override fun onScanfinish() {
        }

        override fun scanResult(bluetoothDevices: MutableList<BluetoothDevice>?) {
            for (i in bluetoothDevices!!.indices) {
                ble_device_List?.text = ble_device_List?.text.toString() + bluetoothDevices?.get(i)?.name + "- - -"
            }
            driveMouseDevice = BleManager.getDeviceMyAddress(bluetoothDevices, MAC_ADDRESS)
            if (driveMouseDevice != null) {
                runOnUiThread {
                    connection_button_view?.setVisibility(View.VISIBLE)
                    connection_button_view?.setText(driveMouseDevice?.getName())
                }
                scan_button?.setText("Scann")
            }
        }
    }
}
