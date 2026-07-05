package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlaylistSong::class,
        CustomPlaylistEntity::class,
        CustomPlaylistSong::class,
        UserActivityEntity::class,
        ListeningSession::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun userActivityDao(): UserActivityDao
}
