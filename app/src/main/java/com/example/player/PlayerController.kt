package com.example.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.data.PlaylistSong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PlayerState(
    val currentSong: PlaylistSong? = null,
    val isPlaying: Boolean = false,
    val playbackPosition: Long = 0L,
    val duration: Long = 0L
)

class PlayerController(private val context: Context) {
    private var browser: MediaController? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var progressJob: Job? = null

    fun connect() {
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        future.addListener({
            browser = future.get()
            browser?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _playerState.update { it.copy(isPlaying = isPlaying) }
                    if (isPlaying) {
                        startTrackingProgress()
                    } else {
                        stopTrackingProgress()
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    updateCurrentMediaItem(mediaItem)
                }
            })
            // Initialize state
            updateCurrentMediaItem(browser?.currentMediaItem)
        }, androidx.core.content.ContextCompat.getMainExecutor(context))
    }

    private fun updateCurrentMediaItem(mediaItem: MediaItem?) {
        val songId = mediaItem?.mediaId
        if (songId != null) {
            val title = mediaItem.mediaMetadata.title?.toString() ?: ""
            val artist = mediaItem.mediaMetadata.artist?.toString() ?: ""
            val artworkUrl = mediaItem.mediaMetadata.artworkUri?.toString() ?: ""
            val streamUrl = mediaItem.mediaMetadata.extras?.getString("streamUrl") ?: ""
            
            _playerState.update { 
                it.copy(
                    currentSong = PlaylistSong(
                        id = songId, 
                        title = title, 
                        artist = artist, 
                        artworkUrl = artworkUrl, 
                        streamUrl = streamUrl
                    ),
                    duration = browser?.duration?.coerceAtLeast(0) ?: 0L,
                    playbackPosition = browser?.currentPosition?.coerceAtLeast(0) ?: 0L
                ) 
            }
        } else {
            _playerState.update { it.copy(currentSong = null) }
        }
    }

    private fun startTrackingProgress() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                _playerState.update { 
                    it.copy(
                        playbackPosition = browser?.currentPosition?.coerceAtLeast(0) ?: 0L,
                        duration = browser?.duration?.coerceAtLeast(0) ?: 0L
                    ) 
                }
                delay(50)
            }
        }
    }

    private fun stopTrackingProgress() {
        progressJob?.cancel()
    }

    fun playSongs(songs: List<PlaylistSong>, startIndex: Int = 0) {
        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id)
                .setUri(song.streamUrl)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setArtworkUri(android.net.Uri.parse(song.artworkUrl))
                        .setExtras(android.os.Bundle().apply { putString("streamUrl", song.streamUrl) })
                        .build()
                )
                .build()
        }
        browser?.setMediaItems(mediaItems, startIndex, 0)
        browser?.prepare()
        browser?.play()
    }

    fun togglePlayPause() {
        browser?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun skipToNext() {
        browser?.seekToNext()
    }

    fun skipToPrevious() {
        browser?.seekToPrevious()
    }

    fun seekTo(position: Float) {
        browser?.duration?.let { dur ->
            val seekPos = (dur * position).toLong()
            browser?.seekTo(seekPos)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        browser?.setPlaybackSpeed(speed)
    }

    fun release() {
        progressJob?.cancel()
        browser?.release()
    }
}
