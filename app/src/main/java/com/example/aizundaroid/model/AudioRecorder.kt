package com.example.aizundaroid.model

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Handler
import androidx.compose.foundation.isSystemInDarkTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.pytorch.IValue
import org.pytorch.Tensor
import java.io.File
import java.io.IOException
import java.io.InputStream
import org.pytorch.LiteModuleLoader



class AudioRecorder(private val outputFile: String) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private val _isRecording = MutableStateFlow(false) // Kotlin Flowを使用
    val isRecording: StateFlow<Boolean> = _isRecording
    private var flag = false



    fun startRecording() {
        if (_isRecording.value) return

        // MediaRecorderのインスタンスを直接生成
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB)
            setAudioSamplingRate(16000)
            setOutputFile(outputFile)

            try {
                prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            start()
            _isRecording.value = true

            Handler().postDelayed({
                if(!_isRecording.value) return@postDelayed
                mediaRecorder?.apply {
                stop()
                release()
                }
                println("stop recording by timeout")
                _isRecording.value= false

            }, 10000)

        }
    }

    fun stopRecording() {
        if (!_isRecording.value) return

        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        _isRecording.value = false
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

