package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlist_songs ORDER BY timestamp DESC")
    fun getAllSongs(): Flow<List<PlaylistSong>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: PlaylistSong)

    @Query("DELETE FROM playlist_songs WHERE id = :id")
    suspend fun deleteSong(id: String)
    
    @Query("SELECT EXISTS(SELECT * FROM playlist_songs WHERE id = :id)")
    fun isSongSaved(id: String): Flow<Boolean>

    @Query("SELECT * FROM custom_playlists ORDER BY createdAt DESC")
    fun getAllCustomPlaylists(): Flow<List<CustomPlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomPlaylist(playlist: CustomPlaylistEntity)

    @Query("DELETE FROM custom_playlists WHERE id = :id")
    suspend fun deleteCustomPlaylist(id: String)

    @Query("DELETE FROM custom_playlist_songs WHERE playlistId = :playlistId")
    suspend fun deleteSongsForPlaylist(playlistId: String)
    
    @Query("UPDATE custom_playlists SET imageUrl = :imageUrl WHERE id = :id AND imageUrl IS NULL")
    suspend fun updateCustomPlaylistImageIfNull(id: String, imageUrl: String)

    @Query("SELECT * FROM custom_playlist_songs WHERE playlistId = :playlistId ORDER BY timestamp DESC")
    fun getSongsForCustomPlaylist(playlistId: String): Flow<List<CustomPlaylistSong>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongToCustomPlaylist(song: CustomPlaylistSong)
    
    @Query("DELETE FROM custom_playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromCustomPlaylist(playlistId: String, songId: String)
}
