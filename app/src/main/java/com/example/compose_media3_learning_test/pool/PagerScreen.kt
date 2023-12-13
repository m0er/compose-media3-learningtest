package com.example.compose_media3_learning_test.pool

import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
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
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.example.compose_media3_learning_test.data.Video

@kotlin.OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerScreen(videos: List<Video>) {
    val pagerState = rememberPagerState {
        videos.size
    }
    val currentIndex by remember {
        derivedStateOf { pagerState.currentPage }
    }
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color.LightGray)
    ) { page ->
        PagerItem(
            video = videos[page],
            focusedVideo = page == currentIndex,
            page = page
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun PagerItem(
    video: Video,
    focusedVideo: Boolean,
    page: Int,
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .background(if (focusedVideo) Color.Magenta else MaterialTheme.colorScheme.surface)
    ) {
        Text(text = video.subtitle, style = MaterialTheme.typography.labelLarge)

        val context = LocalContext.current
        val exoPlayer = remember { SimpleExoPlayerHolder.get(context) }
        var playerView: PlayerView? = null

        if (focusedVideo) {
            LaunchedEffect(video.url) {
                val videoUri = Uri.parse(video.url)
                val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
                    context,
                    DefaultDataSource.Factory(context)
                )
                val source = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(videoUri))
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
                    Log.d("foobar", "비디오 아이템[$page] UPDATE >> FOCUS: FALSE")
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
