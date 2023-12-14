package com.example.compose_media3_learning_test.share_player

import android.util.Log
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
                onEvent(Player.EVENT_RENDERED_FIRST_FRAME)
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.d("foobar", "onPlayerError >> $error")

                onEvent(Player.EVENT_PLAYER_ERROR)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                onEvent(playbackState)

                val playbackStateStr = when (playbackState) {

                    Player.STATE_BUFFERING -> {
                        "STATE_BUFFERING"
                    }

                    Player.STATE_ENDED -> {
                        "STATE_ENDED"
                    }

                    Player.STATE_IDLE -> {
                        "STATE_IDLE"
                    }

                    Player.STATE_READY -> {
                        "STATE_READY"
                    }

                    else -> ""
                }

                Log.d("foobar", "onPlaybackStateChanged >> $playbackStateStr")
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                onEvent(Player.EVENT_IS_PLAYING_CHANGED)

                Log.d("foobar", "onIsPlayingChanged >> $isPlaying")
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                val reasonStr = when(reason) {
                    Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_BECOMING_NOISY -> {
                        "PLAY_WHEN_READY_CHANGE_REASON_AUDIO_BECOMING_NOISY"
                    }

                    Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS -> {
                        "PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS"
                    }

                    Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM -> {
                        "PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM"
                    }

                    Player.PLAY_WHEN_READY_CHANGE_REASON_REMOTE -> {
                        "PLAY_WHEN_READY_CHANGE_REASON_REMOTE"
                    }

                    Player.PLAY_WHEN_READY_CHANGE_REASON_SUPPRESSED_TOO_LONG -> {
                        "PLAY_WHEN_READY_CHANGE_REASON_SUPPRESSED_TOO_LONG"
                    }

                    Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST -> {
                        "PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST"
                    }

                    else -> ""
                }

                Log.d("foobar", "onPlayWhenReadyChanged >> playWhenReady: $playWhenReady, reason: $reasonStr")
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
        }
    }
}
