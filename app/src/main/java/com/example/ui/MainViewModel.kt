package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MusicRepository
import com.example.data.PlaylistSong
import com.example.player.PlayerController
import com.example.player.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class MainUiState(
    val searchResults: List<PlaylistSong> = emptyList(),
    val artistResults: List<OnyxPlaceholder> = emptyList(),
    val albumResults: List<OnyxPlaceholder> = emptyList(),
    val playlistResults: List<OnyxPlaceholder> = emptyList(),
    val quickResume: List<OnyxPlaceholder> = emptyList(),
    val topHits: List<OnyxPlaceholder> = emptyList(),
    val topAlbums: List<OnyxPlaceholder> = emptyList(),
    val topArtists: List<OnyxPlaceholder> = emptyList(),
    val madeForYou: List<OnyxPlaceholder> = emptyList(),
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val activeTab: Int = 0, // 0: Home/Search, 1: Playlists
    val homeState: HomeUiState = HomeUiState.Loading
)

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val heroCarousel: List<OnyxPlaceholder>,
        val trendingNow: List<OnyxPlaceholder>,
        val recentlyPlayed: List<OnyxPlaceholder>,
        val newReleases: List<OnyxPlaceholder>,
        val topCharts: List<OnyxPlaceholder>,
        val recommendedForYou: List<OnyxPlaceholder>,
        val popularArtists: List<OnyxPlaceholder>,
        val quickResume: List<OnyxPlaceholder>,
        val topHits: List<OnyxPlaceholder>,
        val topAlbums: List<OnyxPlaceholder>,
        val topArtists: List<OnyxPlaceholder>,
        val madeForYou: List<OnyxPlaceholder>,
        val dailyMix: List<OnyxPlaceholder>,
        val workoutMusic: List<OnyxPlaceholder>,
        val focusMusic: List<OnyxPlaceholder>,
        val personalizedArtistName: String = ""
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

sealed class ImportState {
    object Idle : ImportState()
    data class Loading(val progress: Int, val total: Int) : ImportState()
    data class Preview(val playlist: com.example.ui.OnyxPlaceholder, val songs: List<PlaylistSong>) : ImportState()
    data class Success(val playlist: com.example.ui.OnyxPlaceholder, val songs: List<PlaylistSong>) : ImportState()
    data class Error(val message: String) : ImportState()
    data class SpotifyAuth(val url: String) : ImportState()
}

class MainViewModel(
    private val repository: MusicRepository,
    private val localAudioHelper: com.example.data.LocalAudioHelper,
    val playerController: PlayerController,
    private val playlistDownloader: com.example.player.PlaylistDownloader,
    private val networkMonitor: com.example.data.NetworkMonitor,
    private val playlistImporter: com.example.data.PlaylistImporter
) : ViewModel() {

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    fun handleSpotifyAuthCode(code: String) {
        viewModelScope.launch {
            try {
                playlistImporter.handleSpotifyAuthCode(code)
                // Resume import
                if (playlistImporter.currentPendingUrl.isNotEmpty()) {
                    previewPlaylistFromLink(playlistImporter.currentPendingUrl)
                }
            } catch (e: Exception) {
                _importState.value = ImportState.Error(e.message ?: "Spotify Authentication Failed")
            }
        }
    }

    fun previewPlaylistFromLink(url: String) {
        if (isOfflineMode.value) return
        viewModelScope.launch {
            _importState.value = ImportState.Loading(0, 100)
            try {
                val result = playlistImporter.importFromLink(url) { current, total ->
                    _importState.value = ImportState.Loading(current, total)
                }
                if (result != null) {
                    val (playlist, songs) = result
                    _importState.value = ImportState.Preview(playlist, songs)
                } else {
                    _importState.value = ImportState.Error("Failed to fetch or unsupported link.")
                }
            } catch (e: com.example.data.SpotifyAuthRequiredException) {
                _importState.value = ImportState.SpotifyAuth(e.authUrl)
            } catch (e: Exception) {
                _importState.value = ImportState.Error(e.message ?: "Unknown error occurred.")
            }
        }
    }

    fun saveImportedPlaylist() {
        if (isOfflineMode.value) return
        val current = _importState.value
        if (current is ImportState.Preview) {
            viewModelScope.launch {
                try {
                    val generatedId = repository.createCustomPlaylist(current.playlist.title, current.playlist.imageUrl)
                    
                    current.songs.forEach { song ->
                        repository.addSongToCustomPlaylist(generatedId, song)
                    }
                    _importState.value = ImportState.Success(current.playlist, current.songs)
                } catch(e: Exception) {
                    _importState.value = ImportState.Error(e.message ?: "Unknown error saving playlist.")
                }
            }
        }
    }
    
    fun resetImportState() {
        _importState.value = ImportState.Idle
    }

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    fun setOfflineMode(offline: Boolean) {
        _isOfflineMode.value = offline
    }

    val downloadProgress = playlistDownloader.downloadProgress
    val downloadingPlaylists = playlistDownloader.downloadingPlaylists

    fun downloadPlaylist(playlistId: String, songs: List<PlaylistSong>) {
        viewModelScope.launch {
            playlistDownloader.downloadPlaylist(playlistId, songs)
        }
    }

    fun isPlaylistDownloaded(songs: List<PlaylistSong>): Boolean {
        return playlistDownloader.isPlaylistDownloaded(songs)
    }

    private val _uiState = MutableStateFlow(MainUiState(homeState = HomeUiState.Loading))
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val customPlaylists: StateFlow<List<OnyxPlaceholder>> = repository.customPlaylists
        .map { entities ->
            entities.map { entity ->
                OnyxPlaceholder(
                    id = entity.id,
                    title = entity.name,
                    type = "custom",
                    subtitle = "Custom Playlist",
                    imageUrl = entity.imageUrl ?: "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17"
                )
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _localAudio = MutableStateFlow<List<PlaylistSong>>(emptyList())
    val localAudio: StateFlow<List<PlaylistSong>> = _localAudio.asStateFlow()

    fun loadLocalAudio() {
        viewModelScope.launch {
            val audio = localAudioHelper.getLocalAudioFiles()
            _localAudio.value = audio
        }
    }

    fun createCustomPlaylist(name: String) {
        viewModelScope.launch {
            repository.createCustomPlaylist(name)
        }
    }

    fun deleteCustomPlaylist(playlistId: String) {
        viewModelScope.launch {
            repository.deleteCustomPlaylist(playlistId)
        }
    }

    fun addSongToCustomPlaylist(playlistId: String, song: PlaylistSong) {
        viewModelScope.launch {
            repository.addSongToCustomPlaylist(playlistId, song)
        }
    }

    val savedSongs: StateFlow<List<PlaylistSong>> = repository.savedSongs
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val playerState: StateFlow<PlayerState> = playerController.playerState

    private val _lyrics = MutableStateFlow<String?>(null)
    val lyrics: StateFlow<String?> = _lyrics.asStateFlow()

    private val _isLoadingLyrics = MutableStateFlow(false)
    val isLoadingLyrics: StateFlow<Boolean> = _isLoadingLyrics.asStateFlow()

    private var lastFetchedSongId: String? = null

    private val _isRefreshingHomeData = MutableStateFlow(false)
    val isRefreshingHomeData: StateFlow<Boolean> = _isRefreshingHomeData.asStateFlow()

    fun refreshHomeData() {
        if (isOfflineMode.value) return
        fetchHomeData()
    }

    private fun fetchHomeData() {
        viewModelScope.launch {
            _isRefreshingHomeData.value = true
            try {
                // Get user's top artist for personalization
                val topArtistsResult = repository.getTopArtists(1)
                val personalArtist = topArtistsResult.firstOrNull()?.artist ?: ""

                // Fetch all data in parallel for maximum performance
                val heroDeferred = async { repository.searchPlaylists("Trending") }
                val trendingDeferred = async { repository.searchPlaylists("Trending") }
                val recentDeferred = async { repository.getRecentActivity(10).map { it.toPlaceholder() } }
                val newReleasesDeferred = async { repository.getNewReleases() }
                val topChartsDeferred = async { repository.getTopCharts() }
                val recommendedDeferred = async { repository.getRecommendedForYou(personalArtist) }
                val popularArtistsDeferred = async { repository.getPopularArtists() }
                val quickResumeDeferred = async { repository.searchPlaylists("Pop") }
                val topHitsDeferred = async { repository.searchPlaylists("Top Hits") }
                val topAlbumsDeferred = async { repository.searchAlbums("New") }
                val topArtistsListDeferred = async { repository.searchArtists("Top") }
                val madeForYouDeferred = async { repository.getMadeForYou(personalArtist) }
                val dailyMixDeferred = async { repository.getDailyMix() }
                val workoutDeferred = async { repository.getWorkoutMusic() }
                val focusDeferred = async { repository.getFocusMusic() }

                // Await all
                val hero = heroDeferred.await().take(5)
                val trending = trendingDeferred.await().take(6)
                val recent = recentDeferred.await()
                val newReleases = newReleasesDeferred.await().take(6)
                val topCharts = topChartsDeferred.await().take(6)
                val recommended = recommendedDeferred.await().take(6)
                val popularArtists = popularArtistsDeferred.await().take(6)
                val quickResume = quickResumeDeferred.await().take(4)
                val topHits = topHitsDeferred.await().take(6)
                val topAlbums = topAlbumsDeferred.await().take(6)
                val topArtists = topArtistsListDeferred.await().take(6)
                val madeForYou = madeForYouDeferred.await().take(6)
                val dailyMix = dailyMixDeferred.await().take(6)
                val workout = workoutDeferred.await().take(6)
                val focus = focusDeferred.await().take(6)

                _uiState.update {
                    it.copy(
                        quickResume = quickResume,
                        topHits = topHits,
                        topAlbums = topAlbums,
                        topArtists = topArtists,
                        madeForYou = madeForYou,
                        homeState = HomeUiState.Success(
                            heroCarousel = hero,
                            trendingNow = trending,
                            recentlyPlayed = recent,
                            newReleases = newReleases,
                            topCharts = topCharts,
                            recommendedForYou = recommended,
                            popularArtists = popularArtists,
                            quickResume = quickResume,
                            topHits = topHits,
                            topAlbums = topAlbums,
                            topArtists = topArtists,
                            madeForYou = madeForYou,
                            dailyMix = dailyMix,
                            workoutMusic = workout,
                            focusMusic = focus,
                            personalizedArtistName = personalArtist
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(homeState = HomeUiState.Error(e.message ?: "Failed to load home data"))
                }
            } finally {
                _isRefreshingHomeData.value = false
            }
        }
    }

    init {
        viewModelScope.launch {
            playerState.collect { state ->
                val song = state.currentSong
                if (song != null && song.id != lastFetchedSongId) {
                    lastFetchedSongId = song.id
                    fetchLyrics(song)
                }
            }
        }
        
        // Fetch home screen data in parallel
        fetchHomeData()
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (isOfflineMode.value) return
        if (query.length > 2) {
            performSearch(query)
        } else if (query.isEmpty()) {
            _uiState.update { 
                it.copy(
                    searchResults = emptyList(),
                    artistResults = emptyList(),
                    albumResults = emptyList(),
                    playlistResults = emptyList()
                ) 
            }
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            val songResults = repository.searchSongs(query)
            val artistResults = repository.searchArtists(query)
            val albumResults = repository.searchAlbums(query)
            val playlistResults = repository.searchPlaylists(query)
            _uiState.update { 
                it.copy(
                    searchResults = songResults, 
                    artistResults = artistResults,
                    albumResults = albumResults,
                    playlistResults = playlistResults,
                    isSearching = false
                ) 
            }
        }
    }

    suspend fun getPlaylistDetails(playlist: OnyxPlaceholder): List<PlaylistSong> {
        return if (playlist.type == "liked") {
            savedSongs.value
        } else if (playlist.type == "local") {
            if (localAudio.value.isEmpty()) {
                val files = localAudioHelper.getLocalAudioFiles()
                _localAudio.value = files
                files
            } else {
                localAudio.value
            }
        } else if (playlist.type == "custom" && playlist.id.isNotEmpty()) {
            val customSongs = repository.getSongsForCustomPlaylist(playlist.id).first()
            customSongs.map { custom -> 
                PlaylistSong(
                    id = custom.songId,
                    title = custom.title,
                    artist = custom.artist,
                    artworkUrl = custom.artworkUrl,
                    streamUrl = custom.streamUrl
                )
            }
        } else if (playlist.id.isNotEmpty() && (playlist.type == "playlist" || playlist.type == "album")) {
            repository.getCollectionDetails(playlist.id, isAlbum = playlist.type == "album")
        } else {
            repository.searchSongs(playlist.title)
        }
    }

    fun setActiveTab(index: Int) {
        _uiState.update { it.copy(activeTab = index) }
    }

    fun playSong(song: PlaylistSong) {
        viewModelScope.launch {
            val songToPlay = if (song.streamUrl.isBlank()) {
                val results = repository.searchSongs("${song.title} ${song.artist}")
                val url = results.firstOrNull()?.streamUrl ?: ""
                song.copy(streamUrl = url)
            } else {
                song
            }
            playerController.playSongs(listOf(songToPlay), 0)
        }
    }
    
    fun playPlaylist(songs: List<PlaylistSong>, startIndex: Int) {
        viewModelScope.launch {
            val updatedSongs = songs.map { song ->
                if (song.streamUrl.isBlank()) {
                    val results = repository.searchSongs("${song.title} ${song.artist}")
                    val url = results.firstOrNull()?.streamUrl ?: ""
                    song.copy(streamUrl = url)
                } else {
                    song
                }
            }
            playerController.playSongs(updatedSongs, startIndex)
        }
    }

    private fun fetchLyrics(song: PlaylistSong) {
        if (isOfflineMode.value) return
        viewModelScope.launch {
            _isLoadingLyrics.value = true
            _lyrics.value = repository.getLyrics(song.title, song.artist)
            _isLoadingLyrics.value = false
        }
    }
    
    fun fetchLyricsForCurrent() {
        val current = playerState.value.currentSong
        if (current != null) {
            fetchLyrics(current)
        }
    }

    fun toggleSaveCurrentSong() {
        viewModelScope.launch {
            val current = playerState.value.currentSong
            if (current != null) {
                // Check if it's already saved
                val isSaved = savedSongs.value.any { it.id == current.id }
                repository.toggleSaveSong(current, isSaved)
            }
        }
    }
    
    fun toggleSaveSong(song: PlaylistSong) {
        viewModelScope.launch {
            val isSaved = savedSongs.value.any { it.id == song.id }
            repository.toggleSaveSong(song, isSaved)
        }
    }

    private val _suggestedCurationSongs = MutableStateFlow<List<PlaylistSong>>(emptyList())
    val suggestedCurationSongs = _suggestedCurationSongs.asStateFlow()

    private val _isCurating = MutableStateFlow(false)
    val isCurating = _isCurating.asStateFlow()

    fun fetchSuggestionsForContext(context: String) {
        if (isOfflineMode.value) return
        viewModelScope.launch {
            _isCurating.value = true
            _suggestedCurationSongs.value = emptyList()
            try {
                // Try searching for a playlist matching the context
                val playlists = repository.searchPlaylists(context)
                if (playlists.isNotEmpty()) {
                    val firstPlaylistId = playlists.first().id
                    val songs = repository.getCollectionDetails(firstPlaylistId, isAlbum = false)
                    _suggestedCurationSongs.value = songs
                } else {
                    // Fallback to searching songs
                    _suggestedCurationSongs.value = repository.searchSongs(context)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isCurating.value = false
            }
        }
    }

    fun clearCuration() {
        _suggestedCurationSongs.value = emptyList()
        _isCurating.value = false
    }

    override fun onCleared() {
        super.onCleared()
        playerController.release()
    }
}
