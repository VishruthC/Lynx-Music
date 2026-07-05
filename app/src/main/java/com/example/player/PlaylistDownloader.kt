package com.example.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheWriter
import com.example.data.PlaylistSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

@UnstableApi
class PlaylistDownloader(private val context: Context) {

    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Float>> = _downloadProgress.asStateFlow()

    private val _downloadingPlaylists = MutableStateFlow<Set<String>>(emptySet())
    val downloadingPlaylists: StateFlow<Set<String>> = _downloadingPlaylists.asStateFlow()

    fun isPlaylistDownloaded(songs: List<PlaylistSong>): Boolean {
        if (songs.isEmpty()) return false
        val cache = CacheModule.getCache(context)
        return songs.all { song ->
            val key = song.streamUrl
            // check if fully cached (we can't just check keys easily so we look for cached bytes)
            val cachedBytes = cache.getCachedBytes(key, 0, -1)
            cachedBytes > 0 // A simplified check
        }
    }
    
    suspend fun downloadPlaylist(playlistId: String, songs: List<PlaylistSong>) = withContext(Dispatchers.IO) {
        if (_downloadingPlaylists.value.contains(playlistId)) return@withContext
        
        _downloadingPlaylists.value = _downloadingPlaylists.value + playlistId
        val cache = CacheModule.getCache(context)
        val httpDataSource = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .createDataSource()
        val cacheDataSource = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory { httpDataSource }
            .createDataSource()

        var totalProgress = 0f
        
        songs.forEachIndexed { index, song ->
            try {
                val dataSpec = DataSpec.Builder().setUri(song.streamUrl).setKey(song.streamUrl).build()
                val cacheWriter = CacheWriter(cacheDataSource, dataSpec, null) { requestLength, bytesCached, newBytesCached ->
                    val songProgress = if (requestLength > 0) bytesCached.toFloat() / requestLength.toFloat() else 0f
                    val currentOverallProgress = (index + songProgress) / songs.size.toFloat()
                    _downloadProgress.value = _downloadProgress.value + (playlistId to currentOverallProgress)
                }
                cacheWriter.cache()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        _downloadProgress.value = _downloadProgress.value + (playlistId to 1.0f)
        _downloadingPlaylists.value = _downloadingPlaylists.value - playlistId
    }
}
