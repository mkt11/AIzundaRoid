package com.example.aizundaroid


import android.Manifest
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

import com.example.aizundaroid.model.AudioRecorder
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
import androidx.compose.runtime.setValue





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
            AudioRecorderUI(viewModel = viewModel , model = model , pyinfer = pyinfer )
        }
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    @Composable
    fun AudioRecorderUI(viewModel: AudioRecorderViewModel  , model: Module , pyinfer: com.chaquo.python.PyObject ) {

        var zundaflag  = remember { mutableStateOf(0) }
        val infiniteTransition = rememberInfiniteTransition()
        val offsetY by infiniteTransition.animateFloat(
            initialValue = -7f,
            targetValue = 7f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
        Box(modifier = Modifier
            .background(color = Color(red = 207, green = 247, blue = 232))
            .fillMaxSize(),
            contentAlignment = Alignment.Center

        ) {

            //真ん中に表示
            Column(
                modifier = Modifier
                    .size(600.dp)
                    .padding(8.dp)
                    .background(color = Color.White, shape = RoundedCornerShape(50.dp))
                ,
                horizontalAlignment = Alignment.CenterHorizontally, // 横方向
                verticalArrangement = Arrangement.Center // 縦方向,


                //カラーを決める


            ) {

            if (zundaflag.value == 0) {
                Image(
                    painter = painterResource(R.drawable.zunda), contentDescription = "zunda",
                    modifier = Modifier
                        .width(200.dp)
                        .offset(y = offsetY.dp)
                )
            }else if (zundaflag.value == 1){
                Image(
                    painter = painterResource(R.drawable.zunda_recording), contentDescription = "zunda_recording",
                    modifier = Modifier
                        .width(200.dp)
                        .offset(y = offsetY.dp)
                )
            }else if (zundaflag.value == 2){
                Text("考え中...",
                    )

                LinearProgressIndicator(
                    color = MaterialTheme.colorScheme.secondary,
              )

                Image(
                    painter = painterResource(R.drawable.zunda_kangae), contentDescription = "zunda_recording",
                    modifier = Modifier
                        .width(200.dp)
                        .offset(y = offsetY.dp)
                )
            }else if (zundaflag.value == 3){
                Image(
                    painter = painterResource(R.drawable.zunda_server), contentDescription = "zunda_recording",
                    modifier = Modifier
                        .width(200.dp)
                        .offset(y = offsetY.dp)
                )
            }

                viewModel.RecordStart(Modifier ,zundaflag)

                viewModel.RecordStop(Modifier ,zundaflag)

                viewModel.RecordPlay(Modifier , zundaflag)

                Button(onClick = {

                    var mediaPlayer: MediaPlayer? = null
                    val outputFile =
                        application.filesDir.absolutePath + File.separator + "recorded_audio.3gp"
                    val outputFile2: String =
                        application.filesDir.absolutePath + File.separator + "recorded_audio2.wav"
                    val outputFile3: String =
                        application.filesDir.absolutePath + File.separator + "recorded_audio3.wav"
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
                                val audio = pyinfer.callAttr("load_wav", outputFile2)
                                var floatArray = audio.toJava(FloatArray::class.java)
                                val tensor = Tensor.fromBlob(
                                    floatArray,
                                    longArrayOf(1, 1, floatArray.size.toLong())
                                )
                                val outputTensor: Tensor =
                                    model.forward(IValue.from(tensor)).toTensor()

                                val data = FloatArray(floatArray.size)
                                outputTensor.dataAsFloatArray.copyInto(data, 0, 0, floatArray.size)
                                pyinfer.callAttr("save_tensor_to_wav", data, outputFile3)

                                mediaPlayer = MediaPlayer().apply {
                                    try {
                                        zundaflag.value = 3
                                        setDataSource(outputFile3)
                                        prepare()
                                        start()
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }

                                }//再生が終わった際
                                mediaPlayer?.setOnCompletionListener {
                                    zundaflag.value = 0
                                }

                            } else if (ReturnCode.isCancel(returnCode)) {
                                println("canceled")
                            } else {
                                println("error")
                            }
                        }
                    }
                    zundaflag.value = 2
                    convert3gpToWav(outputFile, outputFile2)


                }) {
                    Text("AIでずんだもん")
                }

            }
        }
    }

}
