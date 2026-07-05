package com.example.data

import com.example.BuildConfig
import com.example.ui.OnyxPlaceholder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

interface SpotifyApi {
    @FormUrlEncoded
    @POST("api/token")
    suspend fun getToken(
        @Header("Authorization") authHeader: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): SpotifyTokenResponse

    @FormUrlEncoded
    @POST("api/token")
    suspend fun getTokenPkce(
        @Field("client_id") clientId: String,
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("code_verifier") codeVerifier: String
    ): SpotifyTokenResponse

    @GET("v1/playlists/{playlist_id}")
    suspend fun getPlaylist(
        @Header("Authorization") authHeader: String,
        @Path("playlist_id") playlistId: String
    ): SpotifyPlaylistResponse
}

data class SpotifyTokenResponse(val access_token: String)
data class SpotifyPlaylistResponse(
    val name: String, 
    val images: List<SpotifyImage>?,
    val tracks: SpotifyTracks
)
data class SpotifyImage(val url: String)
data class SpotifyTracks(val items: List<SpotifyTrackItem>)
data class SpotifyTrackItem(val track: SpotifyTrack?)
data class SpotifyTrack(val name: String, val artists: List<SpotifyArtist>)
data class SpotifyArtist(val name: String)


interface InvidiousApi {
    @GET("api/v1/playlists/{playlistId}")
    suspend fun getPlaylist(
        @Path("playlistId") playlistId: String
    ): InvidiousPlaylistResponse
}

data class InvidiousPlaylistResponse(
    val title: String?,
    val playlistThumbnail: String?,
    val videos: List<InvidiousVideo>?
)

data class InvidiousVideo(
    val title: String?,
    val author: String?
)

interface PipedApi {
    @GET("playlists/{playlistId}")
    suspend fun getPlaylist(
        @Path("playlistId") playlistId: String
    ): PipedPlaylistResponse
}

data class PipedPlaylistResponse(
    val name: String?,
    val thumbnailUrl: String?,
    val relatedStreams: List<PipedStream>?
)

data class PipedStream(
    val title: String?,
    val uploaderName: String?
)


object ExternalApiClient {
    private val moshi = com.squareup.moshi.Moshi.Builder()
        .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
        .build()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val spotifyApi: SpotifyApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://accounts.spotify.com/")
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SpotifyApi::class.java)
    }

    val spotifyDataApi: SpotifyApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SpotifyApi::class.java)
    }

    fun getInvidiousApi(baseUrl: String): InvidiousApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(InvidiousApi::class.java)
    }

    fun getPipedApi(baseUrl: String): PipedApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PipedApi::class.java)
    }
}

class SpotifyAuthRequiredException(val authUrl: String) : Exception("Spotify Login Required")

class PlaylistImporter(private val repository: MusicRepository) {

    var currentCodeVerifier: String = ""
    var currentPendingUrl: String = ""
    var spotifyAccessToken: String? = null

    private val INVIDIOUS_INSTANCES = listOf(
        "https://invidious.projectsegfau.lt/",
        "https://inv.tux.im/",
        "https://invidious.flokinet.to/",
        "https://inv.thepixora.com/",
        "https://invidious.io.lol/"
    )

    private val PIPED_INSTANCES = listOf(
        "https://pipedapi.kavin.rocks/",
        "https://pipedapi.tokyo.lewd.icu/",
        "https://pipedapi.moomoo.me/"
    )

    private fun generateCodeVerifier(): String {
        val secureRandom = SecureRandom()
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    private fun generateCodeChallenge(verifier: String): String {
        val bytes = verifier.toByteArray(Charsets.US_ASCII)
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(bytes, 0, bytes.size)
        val digest = messageDigest.digest()
        return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    suspend fun handleSpotifyAuthCode(code: String) {
        val clientId = BuildConfig.SPOTIFY_CLIENT_ID
        if (clientId == "PLACEHOLDER") throw Exception("Missing Spotify Client ID in Secrets")

        val tokenResponse = withContext(Dispatchers.IO) {
            ExternalApiClient.spotifyApi.getTokenPkce(
                clientId = clientId,
                code = code,
                redirectUri = "onyx://spotify-auth",
                codeVerifier = currentCodeVerifier
            )
        }
        spotifyAccessToken = tokenResponse.access_token
    }

    suspend fun importFromLink(url: String, progressCallback: (Int, Int) -> Unit): Pair<OnyxPlaceholder, List<PlaylistSong>>? {
        return withContext(Dispatchers.IO) {
            try {
                if (url.contains("spotify.com/playlist/")) {
                    importFromSpotify(url, progressCallback)
                } else if (url.contains("youtube.com/playlist") || url.contains("music.youtube.com/playlist") || url.contains("youtu.be")) {
                    importFromYouTube(url, progressCallback)
                } else {
                    throw Exception("Unsupported link format.")
                }
            } catch (e: SpotifyAuthRequiredException) {
                throw e
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private suspend fun importFromSpotify(url: String, progressCallback: (Int, Int) -> Unit): Pair<OnyxPlaceholder, List<PlaylistSong>>? {
        val clientId = BuildConfig.SPOTIFY_CLIENT_ID
        if (clientId == "PLACEHOLDER") throw Exception("Missing Spotify credentials in Secrets")

        if (spotifyAccessToken == null) {
            currentPendingUrl = url
            currentCodeVerifier = generateCodeVerifier()
            val codeChallenge = generateCodeChallenge(currentCodeVerifier)
            val encodedRedirectUri = java.net.URLEncoder.encode("onyx://spotify-auth", "UTF-8")
            val scopes = java.net.URLEncoder.encode("playlist-read-private playlist-read-collaborative", "UTF-8")
            val authUrl = "https://accounts.spotify.com/authorize?client_id=$clientId&response_type=code&redirect_uri=$encodedRedirectUri&scope=$scopes&code_challenge_method=S256&code_challenge=$codeChallenge"
            throw SpotifyAuthRequiredException(authUrl)
        }

        val playlistId = url.substringAfter("playlist/").substringBefore("?")
        
        val playlistResponse = ExternalApiClient.spotifyDataApi.getPlaylist("Bearer $spotifyAccessToken", playlistId)

        val imageUrl = playlistResponse.images?.firstOrNull()?.url ?: ""
        val title = playlistResponse.name
        val playlistInfo = OnyxPlaceholder(id = playlistId, title = title, subtitle = "Imported Spotify Playlist", imageUrl = imageUrl, type = "custom")

        val videoData = playlistResponse.tracks.items.mapNotNull {
            val track = it.track ?: return@mapNotNull null
            Pair(track.name, track.artists.firstOrNull()?.name ?: "")
        }
        
        val mappedSongs = resolveSongsFromJioSaavn(videoData, progressCallback)
        return Pair(playlistInfo, mappedSongs)
    }

    private suspend fun importFromYouTube(url: String, progressCallback: (Int, Int) -> Unit): Pair<OnyxPlaceholder, List<PlaylistSong>>? {
        val playlistId = if (url.contains("list=")) url.substringAfter("list=").substringBefore("&") else ""
        if (playlistId.isEmpty()) throw Exception("Could not find playlist ID in URL")

        var playlistTitle = "Imported YouTube Playlist"
        var playlistImage = ""
        var videos: List<Pair<String, String>> = emptyList()

        // 1. Try Invidious Instances
        for (instance in INVIDIOUS_INSTANCES) {
            try {
                val api = ExternalApiClient.getInvidiousApi(instance)
                val response = api.getPlaylist(playlistId)
                if (response.videos != null && response.videos.isNotEmpty()) {
                    playlistTitle = response.title ?: playlistTitle
                    playlistImage = response.playlistThumbnail ?: ""
                    videos = response.videos.mapNotNull { 
                        if (it.title != null) Pair(it.title, it.author ?: "") else null 
                    }
                    break 
                }
            } catch (e: Exception) {
                continue
            }
        }

        // 2. Fallback to Piped
        if (videos.isEmpty()) {
            for (instance in PIPED_INSTANCES) {
                try {
                    val api = ExternalApiClient.getPipedApi(instance)
                    val response = api.getPlaylist(playlistId)
                    if (response.relatedStreams != null && response.relatedStreams.isNotEmpty()) {
                        playlistTitle = response.name ?: playlistTitle
                        playlistImage = response.thumbnailUrl ?: ""
                        videos = response.relatedStreams.mapNotNull {
                            if (it.title != null) Pair(it.title, it.uploaderName ?: "") else null
                        }
                        break
                    }
                } catch (e: Exception) {
                    continue
                }
            }
        }

        if (videos.isEmpty()) throw Exception("Failed to fetch playlist data from YouTube providers.")

        val playlistInfo = OnyxPlaceholder(id = playlistId, title = playlistTitle, subtitle = "YouTube Playlist", imageUrl = playlistImage, type = "custom")
        val mappedSongs = resolveSongsFromJioSaavn(videos, progressCallback)
        return Pair(playlistInfo, mappedSongs)
    }

    private suspend fun resolveSongsFromJioSaavn(
        videoData: List<Pair<String, String>>, 
        progressCallback: (Int, Int) -> Unit
    ): List<PlaylistSong> {
        val results = mutableListOf<PlaylistSong>()
        val total = videoData.size
        
        for ((index, pair) in videoData.withIndex()) {
            val (title, author) = pair
            
            val cleanTitle = title
                .replace(Regex("\\(Official Video\\)", RegexOption.IGNORE_CASE), "")
                .replace(Regex("\\[Official Video\\]", RegexOption.IGNORE_CASE), "")
                .replace(Regex("\\(Official Music Video\\)", RegexOption.IGNORE_CASE), "")
                .replace(Regex("\\[Official Music Video\\]", RegexOption.IGNORE_CASE), "")
                .replace(Regex("\\(Audio\\)", RegexOption.IGNORE_CASE), "")
                .replace(Regex("\\[Audio\\]", RegexOption.IGNORE_CASE), "")
                .replace(Regex("\\(Lyric Video\\)", RegexOption.IGNORE_CASE), "")
                .replace(Regex("\\[Lyric Video\\]", RegexOption.IGNORE_CASE), "")
                .replace(Regex("\\(Lyrics\\)", RegexOption.IGNORE_CASE), "")
                .replace(Regex("\\[Lyrics\\]", RegexOption.IGNORE_CASE), "")
                .replace(Regex("\\|.*$"), "")
                .trim()
            
            val cleanArtist = author.replace(Regex("- Topic", RegexOption.IGNORE_CASE), "").trim()
            val queryFull = "$cleanTitle $cleanArtist".trim()

            var songs = repository.searchSongs(queryFull)
            
            if (songs.isEmpty() && cleanTitle.isNotEmpty()) {
                songs = repository.searchSongs(cleanTitle)
            }

            if (songs.isNotEmpty()) {
                results.add(songs.first())
            }
            
            progressCallback(index + 1, total)
        }
        return results
    }
}
