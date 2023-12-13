package com.example.compose_media3_learning_test.pool

import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.example.compose_media3_learning_test.data.Video

@Composable
fun ListScreen(videos: List<Video>) {
    val lazyListState = rememberLazyListState()
    // play the video on the first visible item in the list
    val focusIndex by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex }
    }
    LazyColumn(
        state = lazyListState
    ) {
        itemsIndexed(videos) { index, video ->
            ListItem(video, index == focusIndex, index)
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun ListItem(
    video: Video,
    focusedVideo: Boolean,
    page: Int
) {
    Surface(color = if (focusedVideo) Color.Magenta else MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = video.description, style = MaterialTheme.typography.titleSmall)
            Text(text = video.subtitle, style = MaterialTheme.typography.bodyMedium)

            val context = LocalContext.current
            val exoPlayer = remember { SimpleExoPlayerHolder.get(context) }
            var playerView: PlayerView? = null

            if (focusedVideo) {
                LaunchedEffect(video.url) {
                    val videoUri = Uri.parse(video.url)
                    val dataSourceFactory = DataSourceHolder.getCacheFactory(context)
                    val source = when (Util.inferContentType(videoUri)) {
                        C.CONTENT_TYPE_DASH -> DashMediaSource.Factory(
                            dataSourceFactory
                        )
                            .createMediaSource(MediaItem.fromUri(videoUri))

                        C.CONTENT_TYPE_HLS -> HlsMediaSource.Factory(
                            dataSourceFactory
                        )
                            .createMediaSource(MediaItem.fromUri(videoUri))

                        else -> ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(MediaItem.fromUri(videoUri))
                    }

                    exoPlayer.setMediaSource(source)
                    exoPlayer.prepare()
                    Log.d("foobar", "비디오 아이템[$page] LaunchedEffect >> exoPlayer.prepare()")
                }
            }

            AndroidView(
                modifier = Modifier.aspectRatio(video.width.toFloat() / video.height.toFloat()),
                factory = { viewContext ->
                    Log.d("foobar", "비디오 아이템[$page] FACTORY >> $focusedVideo")
                    val frameLayout = FrameLayout(viewContext)
                    frameLayout.setBackgroundColor(Color.Cyan.hashCode())
                    frameLayout
                },
                update = { frameLayout ->
                    frameLayout.removeAllViews()
                    if (focusedVideo) {
                        Log.d(
                            "foobar",
                            "비디오 아이템[$page] UPDATE >> FOCUS: TRUE"
                        )
                        playerView = PlayerViewPool.get(frameLayout.context)
                        PlayerView.switchTargetView(
                            exoPlayer,
                            PlayerViewPool.currentPlayerViewWeakReference.get(),
                            playerView
                        )
                        Log.d(
                            "foobar",
                            "exoPlayer: ${exoPlayer.hashCode()}, oldPlayerView: ${PlayerViewPool.currentPlayerViewWeakReference.get().hashCode()}, newPlayerView: ${playerView.hashCode()}, player: ${playerView?.player!!.hashCode()}",
                        )
                        PlayerViewPool.setCurrentPlayerView(playerView)
                        playerView!!.apply {
                            player!!.playWhenReady = true
                        }
                        frameLayout.addView(
                            playerView,
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                    } else if (playerView != null) {
                        Log.e("foobar", "비디오 아이템[$page] playerView != null")

                        playerView?.apply {
                            (parent as? ViewGroup)?.removeView(this)
                            PlayerViewPool.release(this)
                        }
                        playerView = null
                    }
                }
            )

            DisposableEffect(key1 = video.url) {
                onDispose {
                    Log.d("foobar", "비디오 아이템[$page] onDispose >> $focusedVideo")

                    if (focusedVideo) {
                        playerView?.apply {
                            (parent as? ViewGroup)?.removeView(this)
                        }
                        exoPlayer.stop()
                        playerView?.let {
                            PlayerViewPool.release(it)
                        }
                        playerView = null
                    }
                }
            }
        }
    }
}
