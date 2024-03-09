package com.example.aizundaroid.model

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import org.pytorch.IValue
import org.pytorch.Tensor
import java.io.File
import java.io.IOException
import java.io.InputStream
import org.pytorch.LiteModuleLoader



class AudioRecorder(private val outputFile: String) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isRecording = false



    fun startRecording() {
        if (isRecording) return

        // MediaRecorderのインスタンスを直接生成
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
            setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
            setOutputFile(outputFile)

            try {
                prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            start()
            isRecording = true
        }
    }

    fun stopRecording() {
        if (!isRecording) return

        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        isRecording = false
    }

    fun playRecordedFile() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }

        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(outputFile)
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun embedding() {
        println("Embedding")
        try {



        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

