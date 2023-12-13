package com.example.compose_media3_learning_test.pool

import android.content.Context
import android.view.LayoutInflater
import androidx.core.util.Pools
import androidx.media3.ui.PlayerView
import com.example.compose_media3_learning_test.R
import java.lang.ref.WeakReference

object PlayerViewPool {
    var currentPlayerViewWeakReference: WeakReference<PlayerView?> = WeakReference(null)

    private val playerViewPool = Pools.SimplePool<PlayerView>(2)

    fun setCurrentPlayerView(playerView: PlayerView?) {
        currentPlayerViewWeakReference = WeakReference(playerView)
    }

    fun get(context: Context): PlayerView {
        return playerViewPool.acquire() ?: createPlayerView(context)
    }

    fun release(player: PlayerView) {
        playerViewPool.release(player)
    }

    private fun createPlayerView(context: Context): PlayerView {
        return (LayoutInflater.from(context).inflate(R.layout.exoplayer_texture_view, null, false) as PlayerView)
    }
}
