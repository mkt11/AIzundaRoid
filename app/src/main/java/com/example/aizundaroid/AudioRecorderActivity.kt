package com.example.aizundaroid


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
import java.nio.ByteBuffer
import java.nio.ByteOrder


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


                val tensor = Tensor.fromBlob(bytes, longArrayOf(1, 1, bytes.size.toLong()))

                //tensor
//                val inTensorBuffer = Tensor.allocateFloatBuffer(bytes.size)
//                for (i in bytes.indices) {
//                    inTensorBuffer.put(i, bytes[i].toFloat())
//                }
//                val tensor: Tensor = Tensor.fromBlob(inTensorBuffer, longArrayOf(1, 1, bytes.size.toLong()))

                //embedding
                println("embedding")
                val outputTensor: Tensor = model.forward(IValue.from(tensor)).toTensor()
                println("embedded")
                println(outputTensor)

                val data = FloatArray(bytes.size)
                outputTensor.dataAsFloatArray.copyInto(data, 0, 0, bytes.size)

// 新しい形状 [1, 5280] でTensorを再生成します
                val newTensorShape = longArrayOf(1, bytes.size.toLong()) // 新しい形状
                val newTensorBuffer = Tensor.allocateFloatBuffer(bytes.size)
                data.forEach { value ->
                    newTensorBuffer.put(value)
                }
                val newTensor = Tensor.fromBlob(newTensorBuffer, newTensorShape)

                println(newTensor)


            }) {
                Text("embdding")
            }
        }
    }
}
