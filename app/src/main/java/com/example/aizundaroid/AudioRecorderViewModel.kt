package com.example.aizundaroid

import android.app.Application
import android.media.MediaRecorder
import androidx.lifecycle.AndroidViewModel
import com.example.aizundaroid.model.AudioRecorder
import java.io.File

class AudioRecorderViewModel(application: Application) : AndroidViewModel(application) {

    // Applicationインスタンスを使用して出力ファイルのパスを設定
    private val outputFile = application.filesDir.absolutePath + File.separator + "recorded_audio.3gp"


    // AudioRecorderのインスタンスを初期化
    private val audioRecorder = AudioRecorder(
        outputFile = outputFile
    )

    // 録音を開始する
    fun startRecording() {
        audioRecorder.startRecording()
    }

    // 録音を停止する
    fun stopRecording() {
        audioRecorder.stopRecording()
    }

    // 録音されたファイルを再生する
    fun playRecordedFile() {
        audioRecorder.playRecordedFile()
        println(outputFile)
    }


    override fun onCleared() {
        super.onCleared()
        audioRecorder.stopRecording()
    }


}
