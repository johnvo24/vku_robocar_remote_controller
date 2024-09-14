package com.example.remotecontrollerforrobocar

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.remotecontrollerforrobocar.databinding.ActivityMainBinding
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt
import android.Manifest
import android.graphics.Color
import android.speech.RecognitionListener


class MainActivity : AppCompatActivity() {
    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var connector: Connector

    private lateinit var btnConnect: Button
    private lateinit var btnMove: ImageView
    private lateinit var movementLimit0: ImageView
    private lateinit var btnArmMode: Button
    private lateinit var btnArmUp: Button
    private lateinit var btnArmLeft: Button
    private lateinit var btnArmBottom: Button
    private lateinit var btnArmRight: Button
    private lateinit var btnArmHold: Button
    private lateinit var btnArmDefault: Button
    private lateinit var btnLaze: Button
    private lateinit var btnFlag: Button
    private lateinit var seekBar: SeekBar
    private lateinit var btnAim: Button
    private lateinit var btnRaiseUp: Button
    private lateinit var seekBarSpeed: SeekBar

    private lateinit var btnAutoMode: Button
    private lateinit var btnVoice: Button
    private lateinit var speechRecognizer: SpeechRecognizer
    private val REQUEST_CODE_SPEECH_INPUT = 100
    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) {}
        override fun onResults(results: Bundle?) {
            val result = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val control = result?.get(0)?.lowercase(Locale.ROOT) ?: "stop"

            if (!control.isNullOrEmpty()) {
                connector.sendData(control + "\n")
                Helper.log(control)
            }

            startSpeechToText()
        }

        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }


    private var timer: Timer? = null
    private var mode: Int = 0
    private var armmode: Int = 0
    private var speedA_sub: Int = 0
    private var speedB_sub: Int = 0
    private var speed = 225


    @SuppressLint("ResourceAsColor", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        // Khởi tạo
        connector = Connector(applicationContext)
        btnConnect = mainBinding.btnConnect
        btnMove = mainBinding.btnMove
        movementLimit0 = mainBinding.movementLimit0
        btnArmMode = mainBinding.btnArmMode
        btnArmUp = mainBinding.btnArmUp
        btnArmLeft = mainBinding.btnArmLeft
        btnArmBottom = mainBinding.btnArmBottom
        btnArmRight = mainBinding.btnArmRight
        btnArmHold = mainBinding.btnArmHold
        btnArmDefault = mainBinding.btnArmDefault
        btnLaze = mainBinding.btnLaze
        btnFlag = mainBinding.btnFlag
        seekBar = mainBinding.seekBar
        btnAim = mainBinding.btnAim
        btnRaiseUp = mainBinding.btnRaiseUp
        seekBarSpeed = mainBinding.seekBarSpeed

        btnAutoMode = mainBinding.btnAutoMode
        btnVoice = mainBinding.btnVoice

        movementLimit0.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    putMoveButton(movementLimit0, btnMove, motionEvent)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    putMoveButton(movementLimit0, btnMove, motionEvent)
                    true
                }

                MotionEvent.ACTION_UP -> {
                    btnMove.x = view.x + view.width / 2 - btnMove.width / 2
                    btnMove.y = view.y + view.height / 2 - btnMove.height / 2

                    Helper.setTextColor(this, mainBinding.textMax, R.color.grey_300)

                    connector.sendData("STOP" + "\n")
                    true
                }

                else -> true
            }
        }
        btnConnect.setOnClickListener {
            if (!connector.isConnected) {
                if (!connector.bluetoothAdapter.isEnabled) {
                    val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBluetoothIntent, MainActivity.REQUEST_ENABLE_BLUETOOTH)
                } else {
                    connector.connect()
                    if (connector.isConnected) {
                        btnConnect.text = "CONNECTED TO HC-05"
                        Helper.setBackgroundTintColor(this, btnConnect, R.color.green_500)
                    }
                }
            } else {
                connector.disconnect()
                btnConnect.text = "NO CONNECTION"
                Helper.setBackgroundTintColor(this, btnConnect, R.color.red_700)
            }
        }
        btnArmMode.setOnClickListener {
            if (armmode == 0) {
                armmode = 1
                btnArmMode.setBackgroundColor(ContextCompat.getColor(this, R.color.red_500))
                connector.sendData("ARMON\n")
                Helper.log("ARMON")
            } else {
                armmode = 0
                btnArmMode.setBackgroundColor(ContextCompat.getColor(this, R.color.grey_700))
                connector.sendData("ARMOFF\n")
                Helper.log("ARMOFF")
            }
        }
        btnArmUp.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (mode == 0) {
                        startSendingData("UP\n")
                        Helper.log("UP")
                    } else {
                        startSendingData("ROTATERIGHT\n")
                        Helper.log("ROTATERIGHT")
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    stopSendingData()
                    connector.sendData("STOP" + "\n")
                    true
                }
                else -> false
            }
        }
        btnArmLeft.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (mode == 0) {
                        startSendingData("LEFT\n")
                        Helper.log("LEFT")
                    } else {
                        speedB_sub = 1
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    stopSendingData()
                    speedB_sub = 0
                    true
                }
                else -> false
            }
        }
        btnArmBottom.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (mode == 0) {
                        startSendingData("BOTTOM\n")
                        Helper.log("BOTTOM")
                    } else {
                        startSendingData("ROTATELEFT\n")
                        Helper.log("ROTATELEFT")
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    stopSendingData()
                    connector.sendData("STOP" + "\n")
                    true
                }
                else -> false
            }
        }
        btnArmRight.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (mode == 0) {
                        startSendingData("RIGHT\n")
                        Helper.log("RIGHT")
                    } else {
                        speedA_sub = 1
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    stopSendingData()
                    speedA_sub = 0
                    true
                }
                else -> false
            }
        }
        btnArmHold.setOnClickListener {
            connector.sendData("HOLD" + "\n")
            Helper.log("HOLD")
        }
        btnArmDefault.setOnClickListener {
            connector.sendData("DEFAULT" + "\n")
            Helper.log("DEFAULT")
        }
        btnLaze.setOnClickListener {
            connector.sendData("LAZE" + "\n")
            Helper.log("LAZE")
        }
        btnFlag.setOnClickListener {
            if (mode == 0) {
                mode = 1
                btnFlag.setBackgroundColor(ContextCompat.getColor(this, R.color.red_500))
                connector.sendData("MODEON\n")
                Helper.log("MODEON")
            } else {
                mode = 0
                btnFlag.setBackgroundColor(ContextCompat.getColor(this, R.color.grey_700))
                connector.sendData("MODEOFF\n")
                Helper.log("MODEOFF")
            }

        }
        btnAim.setOnClickListener {
            connector.sendData("AIM\n")
            Helper.log("AIM")
        }
        btnRaiseUp.setOnClickListener {
            connector.sendData("RAISEUP\n")
            Helper.log("RAISEUP")
        }
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                connector.sendData("E" + seekBar.progress.toString() + "\n");
                println(seekBar.progress.toString())
            }
        })
        seekBarSpeed?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                speed = seekBar.progress.toInt()
                println(seekBar.progress.toString())
            }
        })

        btnAutoMode.setOnClickListener {
            connector.sendData("AUTO\n")
            Helper.log("AUTO")
        }
        btnVoice.setOnClickListener {
            // Kiểm tra quyền truy cập micro
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_CODE_SPEECH_INPUT)
            }
            startSpeechToText();
        }
    }

    private fun startSpeechToText() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(recognitionListener)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Xin chào, nói gì đó...")

        try {
            speechRecognizer.startListening(intent)
        } catch (e: Exception) {
            Toast.makeText(this, " " + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun putMoveButton(viewLimit: View, target: View, motionEvent: MotionEvent) {
        val rTarget = target.width/2
        val r0 = viewLimit.width/2
        val x0 = viewLimit.x + viewLimit.width/2
        val y0 = viewLimit.y + viewLimit.height/2
        val dX = motionEvent.x - x0
        val dY = motionEvent.y - y0
        var l = sqrt(dX*dX+dY*dY)

        var maxSpeed = 215

        if (l*l <= (r0-rTarget)*(r0-rTarget)) {
            target.x = motionEvent.x - rTarget
            target.y = motionEvent.y - rTarget
        } else {
            target.x = x0 + (r0-rTarget)*dX/l - rTarget
            target.y = y0 + (r0-rTarget)*dY/l - rTarget
        }

        if (l - r0 >= 64*1.5) {
            Helper.setTextColor(this, mainBinding.textMax, R.color.yellow_700)
            maxSpeed = 255
        } else {
            Helper.setTextColor(this, mainBinding.textMax, R.color.grey_300)
            maxSpeed = speed
        }

        if (mode == 1) {
            l = abs(dY)
        }

        // ==========================> Ly thuyet thuat toan <=================================
        // speed_0 = 255 * (l/ro)^x (x, y, k dieu chinh) --> l/r0 - (0;1) => mũ x để giá trị luôn trong 0-1 nhưng biến thiên vận tốc tăng mạnh hơn khi l lớn
        // rotate_0 =  speed_0 * |cos(angle)|^y
        // speed_l = speed_0 - delta_0
        // speed_r = speed_0 - delta_1

        // Cần:
        // -(speed_0 - p*rotate_0) = speed_0 - q*rotate_0 --> Khi cos = 1 => rotate_0 = speed_0
        // <=> -speed_0 + p*rotate_0 = speed_0 - q*rotate_0
        // <=> 2*speed_0 - (p+q)*rotate_0 = 0
        // <=> 2*speed_0 - (p+q)*speed_0 = 0
        // <=> 2 - p - q = 0
        // <=> q = 2 - p => 0 < p < 2

        // => delta_0 = p*rotate_0 --> Dùng cho speed bên phía cua
        //    delta_1 = q*rotate_0 = (p-2)*rotate_0 --> Dùng cho speed bên phía khác
        // delta > 0 => p > 2

        // speed phia cua: speed_0 - p*(speed_0*|cos(angle)|^y)

        //                   = 255 * (l/r0)^x * (1 - p * |cos(angle)|^y)
        // speed phia kia: speed_0 - (2-p)*(speed_0*|cos(alpha)|^y)
        //                   = 255 * (l/r0)^x * (1 - (2-p) * |cos(alpha)|^y)

        // x > 0, y > 0, p > 2 tu dieu chinh
        // x --> bien thien toc do 2 banh, y --> bien thien goc quay, p --> bien thien toc do 2 banh khi quay (cang lon giam cang nhanh) 0 - 2

        var angle = -atan2(dY, dX)
        val x = 1.35
        val y = 2
        val p = 1.9
        val speed0 = maxSpeed  * ((if (l > r0) r0 else l).toDouble() / r0).pow(x)
        val cosAlpha = cos(angle).toDouble()
//        Helper.log(angle*180/Math.PI)
        val rotate0 = speed0 * abs(cosAlpha).pow(y) * (1-mode) // Muc dich nhan voi mode de kiem soat che do
        var speedA = speed0 - p * rotate0
        var speedB = speed0 - (2 - p) * rotate0
        if (angle < 0) {
            speedA = -speedA
            speedB = -speedB
        }
        speedA = (speedA - abs(speedA_sub)*speedA)*(1-abs(speedB_sub)) + 255*speedB_sub
        speedB = (speedB - abs(speedB_sub)*speedB)*(1-abs(speedA_sub)) + 255*speedA_sub

        Helper.log(angle.toString() + "\n")
        if (cosAlpha < 0 && mode == 0) {
            connector.sendData(speedA.toInt().toString() + "."+ speedB.toInt() + "\n")
            Helper.log(speedA.toInt().toString() + "." + speedB.toInt() + "\n")
        }
        else {
            connector.sendData(speedB.toInt().toString() + "." + speedA.toInt() + "\n")
            Helper.log(speedB.toInt().toString() + "." + speedA.toInt() + "\n")
        }
}

    companion object {
        private const val REQUEST_ENABLE_BLUETOOTH = 1
    }

    fun startSendingData(command: String) {
        timer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    connector.sendData(command)
                }
            }, 0, 25)  // Cập nhật mỗi 100ms
        }
    }

    fun stopSendingData() {
        timer?.cancel()
        timer = null
    }
}