package com.example.compose_media3_learning_test.pool

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache

@UnstableApi
object CacheHolder {
    private var cache: SimpleCache? = null
    val lock = Object()

    fun get(context: Context): SimpleCache {
        synchronized(lock) {
            if (cache == null) {
                val cacheSize = 20L * 1024 * 1024
                val exoDatabaseProvider = StandaloneDatabaseProvider(context)

                cache = SimpleCache(
                    context.cacheDir,
                    LeastRecentlyUsedCacheEvictor(cacheSize),
                    exoDatabaseProvider
                )
            }
        }
        return cache!!
    }
}
