package com.example.compose_media3_learning_test.share_player

import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.children
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.compose_media3_learning_test.data.Video

@OptIn(UnstableApi::class)
@kotlin.OptIn(ExperimentalFoundationApi::class)
@Composable
fun SharePlayerScreen(videos: List<Video>) {
    val lazyListState = rememberLazyListState()
    // play the video on the first visible item in the list
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
    val context = LocalContext.current
    val exoPlayer: ExoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT
            playWhenReady = true
        }
    }

    Log.d("foobar", "라이프사이클 이벤트 >> $lifecycleEvent")

    LazyColumn(modifier = Modifier.fillMaxSize(), state = lazyListState) {
        item {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .aspectRatio(16 / 9f)
                    .clipToBounds()
                    .background(Color.LightGray)
            ) { page ->
                var showPlayer by remember { mutableStateOf(false) }
                if (!showPlayer) {
                    Image(
                        painter = painterResource(id = videos[page].thumbLocal),
                        contentDescription = "Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (page == currentIndex) {
                    PlayerListener(player = exoPlayer, key = videos[page].url) { event ->
                        if (event == Player.EVENT_RENDERED_FIRST_FRAME) {
                            showPlayer = true
                        }
                    }
                    Log.d("foobar", "video![$page]")
                    SharePlayerItem(
                        video = videos[page],
                        page = page,
                        isForeground = lifecycleEvent != Lifecycle.Event.ON_STOP,
                        exoPlayer = exoPlayer
                    )
                }
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
private fun SharePlayerItem(
    video: Video,
    page: Int,
    isForeground: Boolean,
    exoPlayer: ExoPlayer
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val context = LocalContext.current

        // TODO: 플레이어 release 한 후 플래그를 변경해 보면?
        var playerReleased by remember {
            mutableStateOf(false)
        }
        var player: ExoPlayer = remember(key1 = playerReleased) {
            if (playerReleased) {
                playerReleased = false
                return@remember ExoPlayer.Builder(context).build().apply {
                    repeatMode = Player.REPEAT_MODE_ONE
                    videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT
                    playWhenReady = true
                }
            } else {
                exoPlayer
            }
        }

        LaunchedEffect(video.url) {
            val videoUri = Uri.parse(video.url)
            val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
                context,
                DefaultDataSource.Factory(context)
            )
            val source = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(videoUri))
            player.setMediaSource(source)
            player.prepare()
            Log.d("foobar", "비디오 아이템[$page] LaunchedEffect >> exoPlayer.prepare()")
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                Log.d("foobar", "비디오 아이템[$page] FACTORY")
                PlayerView(viewContext).apply {
                    hideController()
                    useController = false
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

                    this.player = player
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = {
                Log.d(
                    "foobar",
                    "비디오 아이템[$page] UPDATE >> isForeground: $isForeground"
                )

                (it.children.iterator().next() as AspectRatioFrameLayout).setAspectRatio(9 / 16f)

                it.player?.playWhenReady = isForeground
            }
        )

        Text(
            text = video.subtitle,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White
        )

        DisposableEffect(key1 = video.url) {
            onDispose {
                Log.d("foobar", "비디오 아이템[$page] onDispose")
                player.release()
                playerReleased = true
            }
        }
    }
}
