package com.brainx.ble_android_manager

import android.util.Pair

/**
 * Created by pablo.biagioli on 1/31/17.
 *
 * Power
 * -----
 * The basic HM-10 module (and the two TI chips it is based on) works at 3.3V so it cannot be directly connected to a 5V microcontroller (e.g. Arduino).
 * However the HM-10 breakout boards integrate voltage (DC-DC) and logic level converters (LLC) so that their pins can be directly wired to an Arduino.
 *
 * Standard Working Mode (Serial Connection Emulation)
 * ---------------------------------------------------
 * The HM-10 abstracts and packs a Bluetooth Low Energy connection in a serial connection.
 * The original out-of-the-box firmware of the module exposes a BLE peripheral with a proprietary connectivity service (Service UUID: 0000ffe0-0000-1000-8000-00805f9b34fb) that enables bidirectional communication between the module and any other central device that connects to it.
 * The service defines a single characteristic (Characteristic UUID: 0000ffe1-0000-1000-8000-00805f9b34fb) that stores 20 bytes of unformatted data:
 * When the central device wants to send data to the module, it WRITES the charactreristic with the desired content
 * When the module wants to send data, it sends a NOTIFICATION to the central device
 * The HM-10 module implements a serial connection in pin 1 (TXD in breakout boards) and pin 2 (RXD) that is linked logically to the BLE service and connection.
 * Any data that is received through the RXD pin is sent through notifications to the central device. Any data written by the central device is output through the TXD pin.
 * This mechanism wraps the BLE connection as a standard serial connection for the connected microcontroller (Arduino, Raspberry Pi). For example, in the case of Arduino, this connection is controlled as any other serial connection using the Serial or SoftwareSerial library.
 */
class HM10Constants {
    companion object{
        val CHARACTERISTIC_USER_DESCRIPTION = "00002901-0000-1000-8000-00805f9b34fb"
        val CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"
        val HM_10_TX_RX_SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb"
        //val HM_10_CONF = "0000ffe0-0000-1000-8000-00805f9b34fb"
        val BLE_TX = "0000ffe1-0000-1000-8000-00805f9b34fb"
        val BLE_RX = "0000ffe1-0000-1000-8000-00805f9b34fb"
        //val BLE_SERVICE = "713d0000-503e-4c75-ba94-3148f18d941e"



    }
}