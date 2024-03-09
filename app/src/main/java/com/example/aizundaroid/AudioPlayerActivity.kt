package com.example.aizundaroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class AudioPlayerActivity : ComponentActivity() {

    private val viewModel: AudioPlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AudioPlayerScreen(viewModel)
        }
    }
}

@Composable
fun AudioPlayerScreen(viewModel: AudioPlayerViewModel) {
    Column(modifier = Modifier.padding(PaddingValues(16.dp))) {
        Button(onClick = { viewModel.playAudio() }) {
            Text(text = "Play Audio")
        }
        Button(onClick = { viewModel.pauseAudio()}) {
            Text(text = "Pause Audio")
        }
    }
}
