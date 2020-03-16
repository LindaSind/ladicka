package com.example.lsind.ladicka

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Sample that demonstrates how to record a device's microphone using [AudioRecord].
 */
class AudioRecordActivity : AppCompatActivity() {

    /**
     * Signals whether a recording is in progress (true) or not (false).
     */
    private val recordingInProgress = AtomicBoolean(false)

    private var recorder: AudioRecord? = null

    private var recordingThread: Thread? = null

    private var startButton: Button? = null

    private var stopButton: Button? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startButton = findViewById(R.id.StartButton) as Button
        startButton!!.setOnClickListener {
            startRecording()
        }

        stopButton = findViewById(R.id.StopButton) as Button
        stopButton!!.setOnClickListener {
            stopRecording()
        }
    }


    private fun startRecording() {
        recorder = AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLING_RATE_IN_HZ,
                CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE)

        recorder!!.startRecording()

        recordingInProgress.set(true)

        recordingThread = Thread(RecordingRunnable(), "Recording Thread")
        recordingThread!!.start()
    }

    private fun stopRecording() {
        if (null == recorder) {
            return
        }

        recordingInProgress.set(false)

        recorder!!.stop()

        recorder!!.release()

        recorder = null

        recordingThread = null
    }

    private inner class RecordingRunnable : Runnable {

        override fun run() {
            val file = File(Environment.getExternalStorageDirectory(), "recording.pcm")
            val buffer = ByteBuffer.allocateDirect(BUFFER_SIZE)

            try {
                FileOutputStream(file).use { outStream ->
                    while (recordingInProgress.get()) {
                        val result = recorder!!.read(buffer, BUFFER_SIZE)
                        if (result < 0) {
                            throw RuntimeException("Reading of audio buffer failed: " + getBufferReadFailureReason(result))
                        }
                        outStream.write(buffer.array(), 0, BUFFER_SIZE)
                        buffer.clear()
                    }
                }
            } catch (e: IOException) {
                throw RuntimeException("Writing of recorded audio failed", e)
            }

        }

        private fun getBufferReadFailureReason(errorCode: Int): String {
            when (errorCode) {
                AudioRecord.ERROR_INVALID_OPERATION -> return "ERROR_INVALID_OPERATION"
                AudioRecord.ERROR_BAD_VALUE -> return "ERROR_BAD_VALUE"
                AudioRecord.ERROR_DEAD_OBJECT -> return "ERROR_DEAD_OBJECT"
                AudioRecord.ERROR -> return "ERROR"
                else -> return "Unknown ($errorCode)"
            }
        }
    }

    companion object {

        private var SAMPLING_RATE_IN_HZ = 44100

        private var CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO

        private var AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

        /**
         * Factor by that the minimum buffer size is multiplied. The bigger the factor is the less
         * likely it is that samples will be dropped, but more memory will be used. The minimum buffer
         * size is determined by [AudioRecord.getMinBufferSize] and depends on the
         * recording settings.
         */
        private var BUFFER_SIZE_FACTOR = 2

        /**
         * Size of the buffer where the audio data is stored by Android
         */
        private val BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLING_RATE_IN_HZ,
                CHANNEL_CONFIG, AUDIO_FORMAT) * BUFFER_SIZE_FACTOR
    }
}
