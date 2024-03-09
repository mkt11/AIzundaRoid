package com.example.aizundaroid


import com.example.aizundaroid.AudioRecorderViewModel
import android.Manifest
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


class AudioRecorderActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // Handle permission denial
        }
    }

    private val viewModel: AudioRecorderViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {

        //// assetファイルからパスを取得する関数
        fun assetFilePath(context: Context, assetName: String): String {
            val file = File(context.filesDir, assetName)
            if (file.exists() && file.length() > 0) {
                return file.absolutePath
            }
            context.assets.open(assetName).use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }
                    outputStream.flush()
                }
                return file.absolutePath
            }
        }

        val modelPath = assetFilePath(this, "llvc.ptl")
        println(modelPath)
        val model = LiteModuleLoader.load(modelPath)


        super.onCreate(savedInstanceState)
        requestPermissions()
        setContent {
            AudioRecorderUI(viewModel = viewModel , model = model)
        }
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    @Composable
    fun AudioRecorderUI(viewModel: AudioRecorderViewModel  , model: Module) {
        Column(modifier = Modifier.padding(PaddingValues(16.dp))) {
            Button(onClick = {
                viewModel.startRecording()
            }) {
                Text("Start Recording")
            }
            Button(onClick = { viewModel.stopRecording() }) {
                Text("Stop Recording")
            }
            Button(onClick = {
                viewModel.playRecordedFile()
            }) {
                Text("Play Recording")
            }

            Button(onClick = {
                var mediaPlayer: MediaPlayer? = null
                val outputFile = application.filesDir.absolutePath + File.separator + "recorded_audio.wav"

                var file: File = File(outputFile)
                //byte
                var inputStream: InputStream = file.inputStream()
                var bytes: ByteArray = inputStream.readBytes()
                //tensor
                var tensor: Tensor = Tensor.fromBlob(bytes, longArrayOf(1, bytes.size.toLong()))


                //embedding
                val outputTensor: Tensor = model.forward(IValue.from(tensor)).toTensor()
                //このTensorをwav形式にして再生
                val outputBytes: ByteArray = outputTensor.dataAsByteArray
                val outputWav: File = File(outputFile)
                outputWav.writeBytes(outputBytes)
                //outputWav を　pathに保存
                val path = outputFile
                outputWav.copyTo(File(path), true)
                //再生
                mediaPlayer = MediaPlayer().apply {
                    try {
                        setDataSource(path)
                        prepare()
                        start()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

            }) {
                Text("em")
            }
        }
    }
}
