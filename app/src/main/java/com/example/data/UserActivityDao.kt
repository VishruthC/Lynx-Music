package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserActivityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateActivity(activity: UserActivityEntity)

    @Insert
    suspend fun insertSession(session: ListeningSession)

    @Query("SELECT * FROM user_activity ORDER BY lastPlayedTimestamp DESC LIMIT :limit")
    suspend fun getRecentActivity(limit: Int = 20): List<UserActivityEntity>

    @Query("SELECT * FROM user_activity ORDER BY playCount DESC LIMIT :limit")
    suspend fun getTopPlayed(limit: Int = 10): List<UserActivityEntity>

    @Query("SELECT * FROM user_activity WHERE artist = :artistName ORDER BY lastPlayedTimestamp DESC LIMIT :limit")
    suspend fun getActivityByArtist(artistName: String, limit: Int = 10): List<UserActivityEntity>

    @Query("SELECT artist, SUM(playCount) as total FROM user_activity GROUP BY artist ORDER BY total DESC LIMIT :limit")
    suspend fun getTopArtists(limit: Int = 5): List<TopArtistResult>

    @Query("SELECT * FROM user_activity ORDER BY lastPlayedTimestamp DESC LIMIT 1")
    suspend fun getMostRecentActivity(): UserActivityEntity?

    @Query("SELECT SUM(totalListenTimeMs) FROM user_activity")
    suspend fun getTotalListenTimeMs(): Long?

    @Query("SELECT COUNT(DISTINCT songId) FROM user_activity")
    suspend fun getUniqueSongsPlayed(): Int?

    @Query("DELETE FROM listening_sessions WHERE startTime < :cutoff")
    suspend fun pruneOldSessions(cutoff: Long)
}
