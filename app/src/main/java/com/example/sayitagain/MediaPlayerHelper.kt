package com.example.sayitagain

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_BUFFERING
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Player.STATE_IDLE
import androidx.media3.common.Player.STATE_READY
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.google.firebase.storage.FirebaseStorage
import java.io.File

@UnstableApi
class MediaPlayerHelper(context: Context) : Player.Listener {
    private val storage = FirebaseStorage.getInstance()
    var onPlaybackEnded: (() -> Unit)? = null

    private val databaseProvider = StandaloneDatabaseProvider(context)
    private val cache = SimpleCache(
        File(context.cacheDir, "mediaCache"),
        LeastRecentlyUsedCacheEvictor(50 * 1024 * 1024),
        databaseProvider
    )
    private val cacheDataSourceFactory = CacheDataSource.Factory()
        .setCache(cache)
        .setUpstreamDataSourceFactory(DefaultDataSource.Factory(context))

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
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
                    STATE_ENDED -> onPlaybackEnded?.invoke()
                    STATE_BUFFERING -> Log.d("PLAYER", "Buffering...")
                    STATE_IDLE -> Log.d("PLAYER", "Idle")
                    STATE_READY -> Log.d("PLAYER", "Ready")
                }
            }
        })
    }
    fun playFromFirebase(path: String) {
        val reference = storage.getReferenceFromUrl(path)

        reference.downloadUrl.addOnSuccessListener { uri ->
            exoPlayer.stop()
            exoPlayer.setMediaItem(MediaItem.fromUri(uri))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }.addOnFailureListener { e ->
            Log.e("FirebaseStorage", "Błąd pobierania pliku", e)
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }

    fun play(url: String) {
        exoPlayer.stop()
        exoPlayer.setMediaItem(MediaItem.fromUri(url))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    fun pause() {
        exoPlayer.playWhenReady = false
    }

    fun stop() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
    }

    fun release() {
        exoPlayer.stop()
        exoPlayer.release()
        exoPlayer.removeListener(this)
        cache.release()
    }

    fun resume() {
        exoPlayer.playWhenReady = true
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            STATE_READY -> Unit // Media is ready
            STATE_ENDED -> stop()
            STATE_BUFFERING -> Unit // Buffering
            STATE_IDLE -> Unit // Initial state
        }
    }
}