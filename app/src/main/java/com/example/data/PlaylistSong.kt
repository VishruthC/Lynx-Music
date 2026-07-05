package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "playlist_songs")
data class PlaylistSong(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val artworkUrl: String,
    val streamUrl: String,
    val duration: Long = 0L,
    val year: String = "",
    val playCount: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_playlists")
data class CustomPlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_playlist_songs")
data class CustomPlaylistSong(
    @PrimaryKey(autoGenerate = true) val entryId: Int = 0,
    val playlistId: String,
    val songId: String,
    val title: String,
    val artist: String,
    val artworkUrl: String,
    val streamUrl: String,
    val duration: Long = 0L,
    val year: String = "",
    val playCount: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)
