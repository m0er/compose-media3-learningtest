package com.example.compose_media3_learning_test.pool

import android.R
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.example.compose_media3_learning_test.data.Video

@kotlin.OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListPagerScreen(videos: List<Video>) {
    val lazyListState = rememberLazyListState()
    // play the video on the first visible item in the list
    val focusIndex by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex }
    }
    val pagerState = rememberPagerState {
        videos.size
    }
    val currentIndex by remember {
        derivedStateOf { pagerState.settledPage }
    }
    var lifecycleEvent by remember {
        mutableStateOf(Lifecycle.Event.ON_ANY)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            lifecycleEvent = event
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Log.d("foobar", "라이프사이클 이벤트 >> $lifecycleEvent")

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.LightGray)
            ) { page ->
                ListPagerItem(
                    video = videos[page],
                    focusedVideo = page == currentIndex,
                    page = page,
                    isBackground = lifecycleEvent == Lifecycle.Event.ON_STOP
                )
            }
        }

        items(count = 10) { index ->
            Text(
                text = "[index: $index] Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum placerat congue accumsan. Sed tortor urna, luctus eu lorem at, feugiat feugiat leo. Aliquam scelerisque quam orci, a ultricies leo posuere in. Mauris id ligula in nisl euismod tincidunt. Donec porttitor viverra eleifend. Donec vel lorem et quam ultrices maximus malesuada sit amet ante. Aliquam egestas velit quam, vitae fermentum eros bibendum ut. Fusce ex orci, tristique eu facilisis at, molestie eget dui. Vestibulum vel tristique enim. Etiam ullamcorper consequat tincidunt. Phasellus gravida dolor at dolor pellentesque, sit amet facilisis sem tempor. Phasellus eget tortor eget dolor vestibulum mollis. Proin non quam justo. Sed sit amet nulla diam. Sed faucibus lobortis rutrum\nNam pharetra, arcu quis lobortis porttitor, arcu ligula faucibus lorem, vitae lobortis sapien lorem ac lorem. Integer gravida et eros in viverra. Cras aliquam lacinia lacus, id rutrum est sagittis eu. Maecenas non odio elementum, elementum orci ut, posuere magna. Ut interdum turpis nec gravida semper. Proin elementum quam non porttitor eleifend. Fusce scelerisque justo vitae dolor auctor, at suscipit elit pulvinar."
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun ListPagerItem(
    video: Video,
    focusedVideo: Boolean,
    page: Int,
    isBackground: Boolean
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
//                val dataSourceFactory = DataSourceHolder.getCacheFactory(context)
                val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
                    context,
                    DefaultDataSource.Factory(context)
                )
//                val source = when (Util.inferContentType(videoUri)) {
//                    C.CONTENT_TYPE_DASH -> DashMediaSource.Factory(dataSourceFactory)
//                        .createMediaSource(MediaItem.fromUri(videoUri))
//
//                    C.CONTENT_TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory)
//                        .createMediaSource(MediaItem.fromUri(videoUri))
//
//                    else -> ProgressiveMediaSource.Factory(dataSourceFactory)
//                        .createMediaSource(MediaItem.fromUri(videoUri))
//                }
                val source = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(videoUri))
                exoPlayer.setMediaSource(source)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
                Log.d("foobar", "비디오 아이템[$page] LaunchedEffect >> exoPlayer.prepare()")
            }
        }

        AndroidView(
            modifier = Modifier.aspectRatio(video.width.toFloat() / video.height.toFloat()),
            factory = { viewContext ->
                Log.d("foobar", "비디오 아이템[$page] FACTORY >> $focusedVideo")
                val frameLayout = FrameLayout(viewContext)
                frameLayout.setBackgroundColor(viewContext.getColor(R.color.holo_blue_bright))
                frameLayout
            },
            update = { frameLayout ->
                frameLayout.removeAllViews()
                if (focusedVideo) {
                    Log.d(
                        "foobar",
                        "비디오 아이템[$page] UPDATE >> FOCUS: TRUE, isBackground: $isBackground"
                    )
                    playerView = PlayerViewPool.get(frameLayout.context)
                    PlayerView.switchTargetView(
                        exoPlayer,
                        PlayerViewPool.currentPlayerViewWeakReference.get(),
                        playerView
                    )
                    PlayerViewPool.setCurrentPlayerView(playerView)
                    playerView!!.apply {
                        player!!.playWhenReady = !isBackground
                    }
//                        playerView?.apply {
//                            Log.d("foobar", "parent? >> ${parent as? ViewGroup}")
//                            (parent as? ViewGroup)?.removeView(this)
//                        }
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
