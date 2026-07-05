package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_activity")
data class UserActivityEntity(
    @PrimaryKey val songId: String,
    val title: String = "",
    val artist: String = "",
    val artworkUrl: String = "",
    var playCount: Int = 0,
    var lastPlayedTimestamp: Long = 0L,
    var totalListenTimeMs: Long = 0L,
    var averageCompletion: Float = 0f
) {
    fun toPlaceholder(): com.example.ui.OnyxPlaceholder {
        return com.example.ui.OnyxPlaceholder(
            id = songId,
            title = title,
            subtitle = artist,
            imageUrl = artworkUrl,
            type = "song"
        )
    }
}

@Entity(tableName = "listening_sessions")
data class ListeningSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: String = "",
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val durationListenedMs: Long = 0L,
    val completionPercentage: Float = 0f
)
