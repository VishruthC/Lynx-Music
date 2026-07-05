package com.example.data

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

interface SaavnApi {
    @GET("api/search/songs")
    suspend fun searchSongs(
        @Query("query") query: String
    ): SearchResponse

    @GET("api/search/albums")
    suspend fun searchAlbums(@Query("query") query: String): GenericSearchResponse

    @GET("api/search/artists")
    suspend fun searchArtists(@Query("query") query: String): GenericSearchResponse

    @GET("api/search/playlists")
    suspend fun searchPlaylists(@Query("query") query: String): GenericSearchResponse

    @GET("api/playlists")
    suspend fun getPlaylistDetails(
        @Query("id") id: String,
        @Query("limit") limit: Int = 50
    ): CollectionDetailsResponse

    @GET("api/albums")
    suspend fun getAlbumDetails(@Query("id") id: String): CollectionDetailsResponse

    @GET("https://lrclib.net/api/search")
    suspend fun searchLyrics(
        @Query("q") query: String
    ): List<LrcLibResponse>
}
