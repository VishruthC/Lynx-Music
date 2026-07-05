package com.example.data

import android.util.Log
import com.example.player.PlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserActivityTracker(
    private val userActivityDao: UserActivityDao,
    private val playerState: StateFlow<PlayerState>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var currentSessionStart: Long = 0L
    private var currentSongId: String = ""
    private var currentSongTitle: String = ""
    private var currentSongArtist: String = ""
    private var currentArtworkUrl: String = ""
    private var wasPlaying = false

    init {
        scope.launch {
            playerState.collectLatest { state ->
                handlePlayerState(state)
            }
        }
    }

    private fun handlePlayerState(state: PlayerState) {
        val song = state.currentSong
        if (song == null) {
            if (wasPlaying) {
                endSession()
                wasPlaying = false
            }
            return
        }

        if (state.isPlaying) {
            if (!wasPlaying || song.id != currentSongId) {
                // New song started or resumed
                if (wasPlaying && song.id != currentSongId) {
                    endSession() // End previous song session
                }
                startSession(song)
                wasPlaying = true
            }
        } else {
            if (wasPlaying) {
                endSession()
                wasPlaying = false
            }
        }
    }

    private fun startSession(song: com.example.data.PlaylistSong) {
        currentSessionStart = System.currentTimeMillis()
        currentSongId = song.id
        currentSongTitle = song.title
        currentSongArtist = song.artist
        currentArtworkUrl = song.artworkUrl
    }

    private fun endSession() {
        if (currentSongId.isEmpty()) return

        val endTime = System.currentTimeMillis()
        val durationListened = endTime - currentSessionStart
        val completion = (durationListened / 1000f / DEFAULT_SONG_DURATION).coerceIn(0f, 1f)

        scope.launch {
            try {
                userActivityDao.insertSession(
                    ListeningSession(
                        songId = currentSongId,
                        startTime = currentSessionStart,
                        endTime = endTime,
                        durationListenedMs = durationListened,
                        completionPercentage = completion
                    )
                )

                val existing = userActivityDao.getTopPlayed(100).find { it.songId == currentSongId }
                if (existing != null) {
                    existing.playCount += 1
                    existing.lastPlayedTimestamp = endTime
                    existing.totalListenTimeMs += durationListened
                } else {
                    userActivityDao.insertOrUpdateActivity(
                        UserActivityEntity(
                            songId = currentSongId,
                            title = currentSongTitle,
                            artist = currentSongArtist,
                            artworkUrl = currentArtworkUrl,
                            playCount = 1,
                            lastPlayedTimestamp = endTime,
                            totalListenTimeMs = durationListened,
                            averageCompletion = completion
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("UserActivityTracker", "Failed to track activity", e)
            }
        }
    }

    companion object {
        private const val DEFAULT_SONG_DURATION = 180f
    }
}
