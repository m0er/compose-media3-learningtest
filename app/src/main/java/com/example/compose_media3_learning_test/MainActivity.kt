package com.example.compose_media3_learning_test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.compose_media3_learning_test.data.verticalVideos
import com.example.compose_media3_learning_test.pool.ListScreen
import com.example.compose_media3_learning_test.data.videos
import com.example.compose_media3_learning_test.share_player.SharePlayerScreen
import com.example.compose_media3_learning_test.ui.theme.ComposeMedia3LearningTestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeMedia3LearningTestTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
//                    ListScreen(videos = videos)
                    SharePlayerScreen(videos = verticalVideos)
                }
            }
        }
    }
}
