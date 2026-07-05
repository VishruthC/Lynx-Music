package com.example.data

import kotlinx.coroutines.flow.Flow

class MusicRepository(
    private val playlistDao: PlaylistDao,
    private val userActivityDao: UserActivityDao? = null
) {
    
    val savedSongs: Flow<List<PlaylistSong>> = playlistDao.getAllSongs()

    val customPlaylists: Flow<List<CustomPlaylistEntity>> = playlistDao.getAllCustomPlaylists()

    fun getSongsForCustomPlaylist(playlistId: String): Flow<List<CustomPlaylistSong>> {
        return playlistDao.getSongsForCustomPlaylist(playlistId)
    }

    suspend fun createCustomPlaylist(name: String, imageUrl: String? = null): String {
        val id = java.util.UUID.randomUUID().toString()
        playlistDao.insertCustomPlaylist(CustomPlaylistEntity(id = id, name = name, imageUrl = imageUrl))
        return id
    }

    suspend fun deleteCustomPlaylist(playlistId: String) {
        playlistDao.deleteSongsForPlaylist(playlistId)
        playlistDao.deleteCustomPlaylist(playlistId)
    }

    suspend fun addSongToCustomPlaylist(playlistId: String, song: PlaylistSong) {
        playlistDao.insertSongToCustomPlaylist(
            CustomPlaylistSong(
                playlistId = playlistId,
                songId = song.id,
                title = song.title,
                artist = song.artist,
                artworkUrl = song.artworkUrl,
                streamUrl = song.streamUrl,
                duration = song.duration,
                year = song.year,
                playCount = song.playCount
            )
        )
        playlistDao.updateCustomPlaylistImageIfNull(playlistId, song.artworkUrl)
    }

    suspend fun removeSongFromCustomPlaylist(playlistId: String, songId: String) {
        playlistDao.removeSongFromCustomPlaylist(playlistId, songId)
    }

    suspend fun searchSongs(query: String): List<PlaylistSong> {
        return try {
            val response = RetrofitClient.api.searchSongs(query)
            if (response.success == true && response.data?.results != null) {
                response.data.results.mapNotNull { dto ->
                    val id = dto.id ?: return@mapNotNull null
                    val title = dto.name?.replace("&quot;", "\"") ?: "Unknown"
                    val artist = dto.artists?.primary?.firstOrNull()?.name?.replace("&quot;", "\"") ?: "Unknown"
                    val artworkUrl = dto.image?.lastOrNull()?.url?.replace("http:", "https:") ?: ""
                    // Prefer 320kbps or the highest available quality
                    val streamUrl = dto.downloadUrl?.find { it.quality == "320kbps" }?.url
                        ?: dto.downloadUrl?.find { it.quality == "160kbps" }?.url
                        ?: dto.downloadUrl?.lastOrNull()?.url ?: return@mapNotNull null
                    
                    PlaylistSong(
                        id = id,
                        title = title,
                        artist = artist,
                        artworkUrl = artworkUrl,
                        streamUrl = streamUrl,
                        duration = dto.duration ?: 0L,
                        year = dto.year ?: "",
                        playCount = dto.playCount ?: 0L
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun searchArtists(query: String): List<com.example.ui.OnyxPlaceholder> {
        return try {
            val response = RetrofitClient.api.searchArtists(query)
            if (response.success == true && response.data?.results != null) {
                response.data.results.mapNotNull { dto ->
                    val title = dto.title ?: dto.name ?: return@mapNotNull null
                    val imageUrl = dto.image?.lastOrNull()?.url?.replace("http:", "https:") ?: ""
                    com.example.ui.OnyxPlaceholder(
                        id = dto.id ?: "",
                        title = title,
                        subtitle = "Artist",
                        imageUrl = imageUrl,
                        type = "artist"
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun searchAlbums(query: String): List<com.example.ui.OnyxPlaceholder> {
        return try {
            val response = RetrofitClient.api.searchAlbums(query)
            if (response.success == true && response.data?.results != null) {
                response.data.results.mapNotNull { dto ->
                    val title = dto.title ?: dto.name ?: return@mapNotNull null
                    val imageUrl = dto.image?.lastOrNull()?.url?.replace("http:", "https:") ?: ""
                    com.example.ui.OnyxPlaceholder(
                        id = dto.id ?: "",
                        title = title,
                        subtitle = "Album",
                        imageUrl = imageUrl,
                        type = "album"
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun searchPlaylists(query: String): List<com.example.ui.OnyxPlaceholder> {
        return try {
            val response = RetrofitClient.api.searchPlaylists(query)
            if (response.success == true && response.data?.results != null) {
                response.data.results.mapNotNull { dto ->
                    val title = dto.title ?: dto.name ?: return@mapNotNull null
                    val imageUrl = dto.image?.lastOrNull()?.url?.replace("http:", "https:") ?: ""
                    com.example.ui.OnyxPlaceholder(
                        id = dto.id ?: "",
                        title = title,
                        subtitle = "Playlist",
                        imageUrl = imageUrl,
                        type = "playlist"
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getCollectionDetails(id: String, isAlbum: Boolean): List<PlaylistSong> {
        return try {
            val response = if (isAlbum) RetrofitClient.api.getAlbumDetails(id) else RetrofitClient.api.getPlaylistDetails(id = id, limit = 10000)
            if (response.success == true && response.data?.songs != null) {
                response.data.songs.mapNotNull { dto ->
                    val songId = dto.id ?: return@mapNotNull null
                    val title = dto.name?.replace("&quot;", "\"") ?: "Unknown"
                    val artist = dto.artists?.primary?.firstOrNull()?.name?.replace("&quot;", "\"") ?: "Unknown"
                    val artworkUrl = dto.image?.lastOrNull()?.url?.replace("http:", "https:") ?: ""
                    val streamUrl = dto.downloadUrl?.find { it.quality == "320kbps" }?.url
                        ?: dto.downloadUrl?.find { it.quality == "160kbps" }?.url
                        ?: dto.downloadUrl?.lastOrNull()?.url ?: return@mapNotNull null
                    
                    PlaylistSong(
                        id = songId,
                        title = title,
                        artist = artist,
                        artworkUrl = artworkUrl,
                        streamUrl = streamUrl,
                        duration = dto.duration ?: 0L,
                        year = dto.year ?: "",
                        playCount = dto.playCount ?: 0L
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getLyrics(trackName: String, artistName: String): String? {
        return try {
            // Take just the primary artist (before any commas)
            val primaryArtist = artistName.split(",").first().trim()
            val query = "$trackName $primaryArtist"
            val responseList = RetrofitClient.api.searchLyrics(query)
            
            val response = responseList.firstOrNull { !it.syncedLyrics.isNullOrBlank() } 
                ?: responseList.firstOrNull { !it.plainLyrics.isNullOrBlank() }
                
            if (response != null) {
                if (!response.syncedLyrics.isNullOrBlank()) {
                    response.syncedLyrics
                } else {
                    response.plainLyrics
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun toggleSaveSong(song: PlaylistSong, isSaved: Boolean) {
        if (isSaved) {
            playlistDao.deleteSong(song.id)
        } else {
            playlistDao.insertSong(song)
        }
    }

    fun isSongSaved(id: String): Flow<Boolean> = playlistDao.isSongSaved(id)

    // --- User Activity ---

    suspend fun getRecentActivity(limit: Int = 20): List<UserActivityEntity> {
        return userActivityDao?.getRecentActivity(limit) ?: emptyList()
    }

    suspend fun getTopPlayed(limit: Int = 10): List<UserActivityEntity> {
        return userActivityDao?.getTopPlayed(limit) ?: emptyList()
    }

    suspend fun getTopArtists(limit: Int = 5): List<TopArtistResult> {
        return userActivityDao?.getTopArtists(limit) ?: emptyList()
    }

    suspend fun getMostRecentActivity(): UserActivityEntity? {
        return userActivityDao?.getMostRecentActivity()
    }

    // --- Trending & Personalized Content ---

    suspend fun getTrendingPlaylists(): List<com.example.ui.OnyxPlaceholder> {
        return searchPlaylists("Trending")
    }

    suspend fun getNewReleases(): List<com.example.ui.OnyxPlaceholder> {
        return searchAlbums("New Release")
    }

    suspend fun getTopCharts(): List<com.example.ui.OnyxPlaceholder> {
        return searchPlaylists("Top 50")
    }

    suspend fun getMadeForYou(topArtist: String? = null): List<com.example.ui.OnyxPlaceholder> {
        if (!topArtist.isNullOrBlank()) {
            // Search broader: artist + "mix" for personalized but still discovery-oriented
            return searchPlaylists("$topArtist Mix")
        }
        return searchPlaylists("Mix")
    }

    suspend fun getRecommendedForYou(topArtist: String?): List<com.example.ui.OnyxPlaceholder> {
        if (topArtist.isNullOrBlank()) return emptyList()
        return searchPlaylists(topArtist)
    }

    suspend fun getPopularArtists(): List<com.example.ui.OnyxPlaceholder> {
        return searchArtists("Popular")
    }

    suspend fun getDailyMix(): List<com.example.ui.OnyxPlaceholder> {
        return searchPlaylists("Daily Mix")
    }

    suspend fun getWorkoutMusic(): List<com.example.ui.OnyxPlaceholder> {
        return searchPlaylists("Workout")
    }

    suspend fun getFocusMusic(): List<com.example.ui.OnyxPlaceholder> {
        return searchPlaylists("Focus")
    }
}
