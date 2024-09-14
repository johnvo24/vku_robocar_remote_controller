package com.example.remotecontrollerforrobocar

import android.content.Context
import android.content.res.ColorStateList
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat

class Helper {
    companion object {
        fun getScreenSize(context: Context): Array<Int> {
            // Lấy WindowManager
            val windowManager =
                context.getSystemService(AppCompatActivity.WINDOW_SERVICE) as WindowManager

            // Lấy DisplayMetrics
            val displayMetrics = DisplayMetrics()

            // Lấy kích thước của màn hình hiện tại
            windowManager.defaultDisplay.getMetrics(displayMetrics)

            // Chiều rộng và chiều cao của màn hình
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            Log.e(
                "JOHN",
                screenWidth.toString() + "x" + screenHeight.toString() + "-------------------------------------"
            )
            return arrayOf(screenWidth, screenHeight)
        }

        fun log(content: Any) {
            Log.d("JOHN LOG |>----------------<| ", content.toString())
        }

        fun setTextColor(context: Context, target: TextView, color: Int) {
            target.setTextColor(ContextCompat.getColor(context, color))
        }

        fun setBackgroundTintColor(context: Context, button: Button, color: Int) {
            ViewCompat.setBackgroundTintList(button, ColorStateList.valueOf(ContextCompat.getColor(context, color)))
        }
    }

    // Xử lý sự kiện
//        mainBinding.connectButton.setOnClickListener {
//            if (!connector.bluetoothAdapter.isEnabled) {
//                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//                startActivityForResult(enableBluetoothIntent, MainActivity.REQUEST_ENABLE_BLUETOOTH) //Cấp quyền Nearly App trong điện thoại
//            } else {
//                connector.connect()
//                if (connector.isConnected) mainBinding.connectButton.text = "Connected HC-05"
//            }
//        }
//        mainBinding.forwardButton.setOnTouchListener { _, motionEvent ->
//            when (motionEvent.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    connector.sendData("F")
//                    true
//                }
//                MotionEvent.ACTION_UP -> {
//                    connector.sendData("S")
//                    true
//                }
//                else -> false
//            }
//        }
//        mainBinding.backwardButton.setOnTouchListener { _, motionEvent ->
//            when (motionEvent.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    connector.sendData("G")
//                    true
//                }
//                MotionEvent.ACTION_UP -> {
//                    connector.sendData("S")
//                    true
//                }
//                else -> false
//            }
//        }
//        mainBinding.leftButton.setOnTouchListener { _, motionEvent ->
//            when (motionEvent.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    connector.sendData("Q")
//                    true
//                }
//                MotionEvent.ACTION_UP -> {
//                    connector.sendData("S")
//                    true
//                }
//                else -> false
//            }
//        }
//        mainBinding.rightButton.setOnTouchListener { _, motionEvent ->
//            when (motionEvent.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    connector.sendData("E")
//                    true
//                }
//                MotionEvent.ACTION_UP -> {
//                    connector.sendData("S")
//                    true
//                }
//                else -> false
//            }
//        }
}