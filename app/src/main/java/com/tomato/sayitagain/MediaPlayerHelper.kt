package com.tomato.sayitagain

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.google.firebase.storage.FirebaseStorage
import java.io.File

@UnstableApi
class MediaPlayerHelper(context: Context) : Player.Listener {

    private val appContext = context.applicationContext
    private val storage = FirebaseStorage.getInstance()
    private val handler = Handler(Looper.getMainLooper())
    private val databaseProvider = StandaloneDatabaseProvider(appContext)

    companion object {
        // Jedna instancja cache na cały proces
        @Volatile private var sSimpleCache: SimpleCache? = null
        private const val CACHE_DIR = "media_cache"
        private const val CACHE_SIZE: Long = 200L * 1024 * 1024 // 200 MB

        private fun getOrCreateCache(context: Context, dbProvider: StandaloneDatabaseProvider): SimpleCache {
            return sSimpleCache ?: synchronized(this) {
                sSimpleCache ?: SimpleCache(
                    File(context.cacheDir, CACHE_DIR),
                    LeastRecentlyUsedCacheEvictor(CACHE_SIZE),
                    dbProvider
                ).also { sSimpleCache = it }
            }
        }
    }

    // Konfiguracja LoadControl dla buforowania
    private val loadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(
            5_000,   // minBufferMs
            30_000,  // maxBufferMs
            1_500,   // bufferForPlaybackMs
            3_000    // bufferForPlaybackAfterRebufferMs
        )
        .build()



    private val cacheDataSourceFactory: DataSource.Factory = CacheDataSource.Factory()
        .setCache(getOrCreateCache(appContext, databaseProvider))
        .setUpstreamDataSourceFactory(
            DefaultHttpDataSource.Factory()
                .setConnectTimeoutMs(15_000)
                .setReadTimeoutMs(15_000)
                .setDefaultRequestProperties(
                    mapOf(
                        "Cache-Control" to "public, max-age=2592000",
                        "Pragma" to "cache"
                    )
                )
        )
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(appContext)
        .setLoadControl(loadControl)
        .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
        .build()
        .apply {
            addListener(this@MediaPlayerHelper)
            repeatMode = Player.REPEAT_MODE_OFF
        }

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> Log.d("PLAYER", "Buffering...")
                    Player.STATE_READY -> Log.d("PLAYER", "Ready to play")
                    Player.STATE_ENDED -> stop()
                    Player.STATE_IDLE -> { /* no-op */ }
                }
            }
        })
    }

    /**
     * Odtwarzaj dźwięk pobierając link z Firebase Storage,
     * a samo odtwarzanie zawsze przez cacheDataSourceFactory.
     */
    fun playFromFirebaseFile(path: String) {
        Log.d("MediaPlayer", "Próba odczytu ścieżki: $path")

        var isTimeout = false
        // timeout na pobranie URL
        val timeoutRunnable = Runnable {
            isTimeout = true
            handler.post {
                Log.w("MediaPlayerHelper", "Download timeout")
                stop()
            }
        }
        handler.postDelayed(timeoutRunnable, 10_000)

        storage.reference.child(path)
            .downloadUrl
            .addOnSuccessListener { uri ->
                Log.d("MediaPlayer", "Pobrano URL: $uri")
                prepareAndPlay(uri, path)
                if (isTimeout) return@addOnSuccessListener
                handler.removeCallbacks(timeoutRunnable)
                prepareAndPlay(uri, path)
            }
            .addOnFailureListener { e ->
                Log.e("MediaPlayer", "Błąd pobierania URL", e)
                handler.removeCallbacks(timeoutRunnable)
                handler.post {
                    Log.e("MediaPlayerHelper", "Download failed", e)
                    stop()
                }
            }
    }

    fun play(url: String) {
        prepareAndPlay(url.toUri(), url)
    }

    private fun prepareAndPlay(uri: Uri, cacheKey: String) {
        Log.d("MediaPlayer", "Przygotowanie odtwarzania: $uri")
        handler.post {
            try {
                if (exoPlayer.playbackState == Player.STATE_IDLE) {
                    val mediaItem = MediaItem.Builder()
                        .setUri(uri)
                        .setCustomCacheKey(cacheKey)
                        .build()

                    exoPlayer.stop()
                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                    exoPlayer.playWhenReady = true
                } else {
                    Log.w("MediaPlayerHelper", "Player is not in idle state")
                }
            } catch (e: Exception) {
                Log.e("MediaPlayerHelper", "Playback error", e)
                stop()
            }
        }
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun resume() {
        exoPlayer.play()
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }

    fun stop() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
    }

    fun release() {
        exoPlayer.removeListener(this)
        exoPlayer.release()
        handler.removeCallbacksAndMessages(null)

        sSimpleCache?.let {
            it.release()
            sSimpleCache = null
        }
        databaseProvider.close()
    }


    override fun onPlaybackStateChanged(playbackState: Int) {
        // dodatkowa obsługa jeżeli potrzebujesz
    }
}
