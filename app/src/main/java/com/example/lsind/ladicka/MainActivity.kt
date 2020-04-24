package com.example.lsind.ladicka

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.Switch
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread
import kotlin.math.round


class MainActivity : AppCompatActivity() {
    var button = 0
    var buttons = arrayOf<Button>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonE2.setOnClickListener {
            button = 1
        }
        buttonA.setOnClickListener {
            button = 2
        }
        buttonD.setOnClickListener {
            button = 3
        }
        buttonG.setOnClickListener {
            button = 4
        }
        buttonH.setOnClickListener {
            button = 5
        }
        buttonE4.setOnClickListener {
            button = 6
        }
        buttons = arrayOf(buttonE2, buttonA, buttonD, buttonG, buttonH, buttonE4)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun record(switch: View) {
        switch as Switch
        if (switch.isChecked) {

            if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO)
                ActivityCompat.requestPermissions(this, permissions, 0)
            }
            thread {
                val recorder = AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLING_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE)
                assert(recorder.state != 0)

                recorder.startRecording()
                val buffer = ByteArray(BUFFER_SIZE)
                while (switch.isChecked) {
                    recorder.read(buffer, 0, BUFFER_SIZE)
                    val wave = unpack(buffer)
                    Fft.autocorrelation(wave)
                    render(wave)
                }
                recorder.release()
                runOnUiThread {
                    buttons.forEach { it.setBackgroundResource(R.drawable.custom_button) }
                    arrowRight.setBackgroundColor(Color.TRANSPARENT)
                    arrowLeft.setBackgroundColor(Color.TRANSPARENT)
                    textView2.setTextColor(Color.TRANSPARENT)
                    textView.text = "f = 0 Hz"
                    textView3.text = "difference = 0 Hz"
                    textView4.text = ""
                    button = 0
                }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun render(corr: DoubleArray) {
        runOnUiThread {
            val peak = findPeak(corr)
            var frequency = SAMPLING_RATE_IN_HZ / peak
            frequency = round(10 * frequency) / 10
            textView.text = "f = ${frequency} Hz"
            toneDifference(button, frequency)
        }
    }

    fun toneDifference(button: Int, frequency: Double) {
        var difference = 0.0

        textView4.text = "Press a button."
        if (button != 0) {
            buttons.forEach { it.setBackgroundResource(R.drawable.custom_button) }
            buttons[button-1].setBackgroundResource(R.drawable.pressed_button)
            textView4.text = ""
        }

        if (button == 1){
            difference = frequency - 82.0
        }
        if (button == 2){
            difference = frequency - 110.8
        }
        if (button == 3){
            difference = frequency - 146.3
        }
        if (button == 4){
            difference = frequency - 196.0

        }
        if (button == 5){
            difference = frequency - 246.9
        }
        if (button == 6){
            difference = frequency - 329.6
        }
        difference = round(10*difference) /10

        textView3.text = "difference: ${difference} Hz"

        if (difference > 0.5) {
            arrowRight.setBackgroundColor(Color.RED)
            arrowLeft.setBackgroundColor(Color.TRANSPARENT)
            textView2.setTextColor(Color.TRANSPARENT)
        }
        else if (difference < -0.5) {
            arrowLeft.setBackgroundColor(Color.RED)
            arrowRight.setBackgroundColor(Color.TRANSPARENT)
            textView2.setTextColor(Color.TRANSPARENT)
        }
        else {
            textView2.setTextColor(Color.GREEN)
            arrowRight.setBackgroundColor(Color.TRANSPARENT)
            arrowLeft.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    companion object {
        fun unpack(bytes: ByteArray): DoubleArray {
            val samples = DoubleArray(bytes.size / 2)
            for (i in samples.indices) {
                samples[i] = bytes[2 * i + 1] + bytes[2 * i] / 256.0 - 127.5
            }
            return samples
        }

        fun findPeak(wave: DoubleArray): Double {
            var i = 0
            while (wave[i + 1] < wave[i]) i++
            while (wave[i + 1] > wave[i]) i++
            return i.toDouble() // todo: interpolate
        }

        fun nearestPowerOfTwo(x: Int): Int {
            val result = Integer.highestOneBit(x)
            return if (x == result) result else result shl 1
        }

        private var SAMPLING_RATE_IN_HZ = 44100
        private var CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private var AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private var BUFFER_SIZE_FACTOR = 5
        private val BUFFER_SIZE = nearestPowerOfTwo(AudioRecord.getMinBufferSize(SAMPLING_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT) * BUFFER_SIZE_FACTOR)
    }
}

