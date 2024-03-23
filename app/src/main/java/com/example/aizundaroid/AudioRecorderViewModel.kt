package com.example.aizundaroid

import android.app.Application
import android.media.MediaRecorder
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import com.example.aizundaroid.model.AudioRecorder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

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


    @Composable
    fun IndeterminateCircularIndicator( modifier: Modifier) {
        val loading by audioRecorder.isRecording.collectAsState()
        var currentProgress by remember { mutableStateOf(0f) }
        Button(onClick = {
            audioRecorder.startRecording()
        },
            enabled = !loading

        ) {
            Text("録音ボタン")
        }


        if (!loading) return

        Text("Recording...",
            modifier = modifier.padding(0.dp,24.dp,0.dp,0.dp),)
        LinearProgressIndicator(
            color = MaterialTheme.colorScheme.secondary,
            modifier = modifier.padding(0.dp,0.dp,0.dp,24.dp),)
    }






}
