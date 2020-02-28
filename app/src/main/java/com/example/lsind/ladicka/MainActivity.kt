package com.example.lsind.ladicka

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;
import java.io.File

class MainActivity : AppCompatActivity() {

    var mediaRecorder: MediaRecorder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaRecorder = MediaRecorder()


        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 0);


        }

        val outputFile = File.createTempFile("prefix", ".ogg")
        mediaRecorder?.setOutputFile(outputFile)
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

    }

    fun startRecording(v: View){
        mediaRecorder?.prepare()
        mediaRecorder?.start()
        textView.setText("recording started!")
    }

    fun stopRecording(v: View){
        mediaRecorder?.stop()
        mediaRecorder?.release()
        textView.setText("recording ended!")
    }
}
 