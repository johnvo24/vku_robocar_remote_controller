package com.example.remotecontrollerforrobocar

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.util.Log
import java.io.DataOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.UUID

class Connector(context: Context) {
    var isConnected: Boolean = false
    var socket: BluetoothSocket? = null
    var curDevice: BluetoothDevice? = null
    val bluetoothAdapter: BluetoothAdapter = context.getSystemService(BluetoothManager::class.java).adapter

    private val TAG = "BluetoothConnection"
    private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // UUID của SPP

    fun connect() {
        if (!isConnected) {
            // Hiển thị các thiết bị có thể kết nối
//            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
//            pairedDevices?.forEach { device ->
//                println(device.name + " : " + device.address)
//            }

            val hc05Device: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice("00:22:12:01:91:06")

            if (hc05Device == null) {
                Log.e(TAG, "Không tìm thấy HC05")
            } else {
                connectToHC05(hc05Device)
            }
        }
    }

    fun disconnect() {
        socket?.close()
        isConnected = false
        socket = null
        curDevice = null
    }

    private fun connectToHC05(device: BluetoothDevice) {
        // Tạo BluetoothSocket
        socket = try {
            device.createRfcommSocketToServiceRecord(MY_UUID)
        } catch (e: IOException) {
            Log.e(TAG, "Socket's create() method failed", e)
            return
        }
        // Kết nối BluetoothSocket
        try {
            socket?.connect()
            Log.d(TAG, "Kết nối Bluetooth thành công")
            // Update isconnected & device
            isConnected = true
            curDevice = device
        } catch (connectionException: IOException) {
            try {
                socket?.close()
            } catch (closeException: IOException) {
                Log.e(TAG, "Không thể đóng client socket")
            }
            Log.e(TAG, "Không thể kết nối tới HC05")
        }
    }

    fun sendData(data: String) {
        if (isConnected) {
            try {
                // Lấy luồng xuất từ BluetoothSocket để gửi dữ liệu
                val outputStream: OutputStream? = socket?.outputStream
                // Chuyển đổi dữ liệu từ String sang mảng byte
                val byteData: ByteArray = data.toByteArray(StandardCharsets.UTF_8)
                // Gửi dữ liệu qua BluetoothSocket
                outputStream?.write(byteData)
                outputStream?.flush() // Đảm bảo dữ liệu đã được gửi đi hoàn toàn
            } catch (e: IOException) {
                // Xử lý lỗi khi gửi dữ liệu
                e.printStackTrace()
            }
        }
    }

    fun sendData(data: Int) {
        if (isConnected) {
            try {
                val outputStream: OutputStream? = socket?.outputStream
                val dataOutputStream = DataOutputStream(outputStream)
                dataOutputStream.writeInt(data)
                dataOutputStream.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}