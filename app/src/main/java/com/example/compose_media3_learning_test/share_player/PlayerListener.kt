package com.example.compose_media3_learning_test.share_player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player

@Composable
fun PlayerListener(
    player: Player,
    key: String,
    onEvent: (Int) -> Unit
) {
    DisposableEffect(key1 = key) {
        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() {
                super.onRenderedFirstFrame()
                onEvent(Player.EVENT_RENDERED_FIRST_FRAME)
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                onEvent(Player.EVENT_PLAYER_ERROR)
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
        }
    }
}
