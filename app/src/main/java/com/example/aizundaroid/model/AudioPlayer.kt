package com.example.aizundaroid.model
import android.content.Context
import android.media.MediaPlayer

class AudioPlayer(
    private val context: Context,
    private val resourceId: Int,
    private val mediaPlayerFactory: (Context, Int) -> MediaPlayer = { ctx, resId ->
        MediaPlayer.create(ctx, resId)
    }
) {

    private var mediaPlayer: MediaPlayer? = null

    init {
        prepareMediaPlayer()
    }

    private fun prepareMediaPlayer() {
        mediaPlayer = mediaPlayerFactory(context, resourceId).apply {
            setOnCompletionListener {
                stopAudio()
            }
        }
    }

    fun playAudio() {
        mediaPlayer?.start()
    }

    fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        prepareMediaPlayer()
    }

    fun pauseAudio() {
        mediaPlayer?.pause()
    }
}

