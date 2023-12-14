package com.example.compose_media3_learning_test.share_player

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

object ExoPlayerHolder {
    private var exoplayer: ExoPlayer? = null
    private var isReleased: Boolean = false

    fun get(context: Context): ExoPlayer {
        Log.d("foobar", "PlayerHolder.get() >> isReleased: $isReleased, $exoplayer")
        if (exoplayer == null) {
            exoplayer = createExoPlayer(context)
            isReleased = false
        }
        return exoplayer!!
    }

    fun release() {
        Log.d("foobar", "PlayerHolder.release()")
        exoplayer?.release()
        exoplayer = null
        isReleased = true
    }

    fun isReleased() = isReleased

    @OptIn(UnstableApi::class)
    private fun createExoPlayer(context: Context): ExoPlayer {
        return ExoPlayer.Builder(context)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ONE
                videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT
                playWhenReady = true
            }
    }
}
