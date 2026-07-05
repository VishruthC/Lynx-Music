package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchResponse(
    val success: Boolean?,
    val data: SearchData?
)

@JsonClass(generateAdapter = true)
data class SearchData(
    val results: List<SongDto>?
)

@JsonClass(generateAdapter = true)
data class SongDto(
    val id: String?,
    val name: String?,
    val artists: ArtistsDto?,
    val image: List<ImageDto>?,
    val downloadUrl: List<DownloadUrlDto>?,
    val duration: Long?,
    val year: String?,
    val playCount: Long?
)

@JsonClass(generateAdapter = true)
data class ArtistsDto(
    val primary: List<ArtistItemDto>?
)

@JsonClass(generateAdapter = true)
data class ArtistItemDto(
    val name: String?
)

@JsonClass(generateAdapter = true)
data class ImageDto(
    val quality: String?,
    val url: String?
)

@JsonClass(generateAdapter = true)
data class DownloadUrlDto(
    val quality: String?,
    val url: String?
)

@JsonClass(generateAdapter = true)
data class LrcLibResponse(
    val id: Long?,
    val syncedLyrics: String?,
    val plainLyrics: String?
)

@JsonClass(generateAdapter = true)
data class GenericSearchResponse(
    val success: Boolean?,
    val data: GenericSearchData?
)

@JsonClass(generateAdapter = true)
data class GenericSearchData(
    val results: List<GenericDto>?
)

@JsonClass(generateAdapter = true)
data class GenericDto(
    val id: String?,
    val title: String?, // Some endpoints use title, others name
    val name: String?,
    val description: String?,
    val type: String?,
    val image: List<ImageDto>?
)

@JsonClass(generateAdapter = true)
data class CollectionDetailsResponse(
    val success: Boolean?,
    val data: CollectionData?
)

@JsonClass(generateAdapter = true)
data class CollectionData(
    val id: String?,
    val name: String?,
    val title: String?,
    val songs: List<SongDto>?
)
