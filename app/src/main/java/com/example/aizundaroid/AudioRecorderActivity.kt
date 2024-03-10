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
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
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


        if (! Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val py = Python.getInstance()

        // 以下書き換え
        val pyinfer = py.getModule("infer") // スクリプト名



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
            AudioRecorderUI(viewModel = viewModel , model = model , pyinfer = pyinfer ,modelpath = modelPath)
        }
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    @Composable
    fun AudioRecorderUI(viewModel: AudioRecorderViewModel  , model: Module , pyinfer: com.chaquo.python.PyObject , modelpath : String) {
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
                val outputFile = application.filesDir.absolutePath + File.separator + "recorded_audio.3gp"
                val outputFile2 : String = application.filesDir.absolutePath + File.separator + "recorded_audio2.wav"
                val outputFile3 : String = application.filesDir.absolutePath + File.separator + "recorded_audio3.wav"
                var file: File = File(outputFile)
                var file3 = File(outputFile3)
                file3.delete()
                //byte
                var inputStream: InputStream = file.inputStream()
                var bytes: ByteArray = inputStream.readBytes()

//                val tensor = Tensor.fromBlob(bytes, longArrayOf(1, 1, bytes.size.toLong()))
//
                fun convert3gpToWav(inputPath: String, outputPath: String) {
                    val cmd = "-y -i $inputPath $outputPath"
                    FFmpegKit.executeAsync(cmd) { session ->
                        val returnCode = session.returnCode
                        if (ReturnCode.isSuccess(returnCode)) {
                            println("success")
                            val audio = pyinfer.callAttr("load_wav",  outputFile2 )
                            var floatArray = audio.toJava(FloatArray::class.java)
                            val tensor = Tensor.fromBlob(floatArray, longArrayOf(1, 1, floatArray.size.toLong()))
                            val outputTensor: Tensor = model.forward(IValue.from(tensor)).toTensor()

                            val data = FloatArray(floatArray.size)
                            outputTensor.dataAsFloatArray.copyInto(data, 0, 0, floatArray.size)
                            pyinfer.callAttr("save_tensor_to_wav", data , outputFile3)

                            mediaPlayer = MediaPlayer().apply {
                                try {
                                    setDataSource(outputFile3)
                                    prepare()
                                    start()
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }

                            }

                        } else if (ReturnCode.isCancel(returnCode)) {
                            println("canceled")
                        } else {
                            println("error")
                        }
                    }
                }



                convert3gpToWav(outputFile , outputFile2)

            }) {
                Text("Embdding")
            }

        }
    }
}
