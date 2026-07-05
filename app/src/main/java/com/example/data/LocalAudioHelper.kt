package com.example.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalAudioHelper(private val context: Context) {
    suspend fun getLocalAudioFiles(): List<PlaylistSong> = withContext(Dispatchers.IO) {
        val audioList = mutableListOf<PlaylistSong>()
        
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.ALBUM_ID
        )

        // Only music
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
            val yearColumn = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
            val albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = if (idColumn >= 0) cursor.getLong(idColumn) else 0L
                val title = if (titleColumn >= 0) cursor.getString(titleColumn) ?: "Unknown" else "Unknown"
                val artist = if (artistColumn >= 0) cursor.getString(artistColumn) ?: "Unknown" else "Unknown"
                val duration = if (durationColumn >= 0) cursor.getLong(durationColumn) else 0L
                val year = if (yearColumn >= 0) cursor.getString(yearColumn) ?: "" else ""
                val albumId = if (albumIdColumn >= 0) cursor.getLong(albumIdColumn) else 0L

                val contentUri: Uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                
                // Content provider for album art
                val artworkUri = Uri.parse("content://media/external/audio/albumart/$albumId").toString()

                audioList.add(
                    PlaylistSong(
                        id = id.toString(),
                        title = title,
                        artist = artist,
                        artworkUrl = artworkUri, // Local content uri as string, Coil might handle it
                        streamUrl = contentUri.toString(),
                        duration = duration / 1000L, // seconds maybe? Assuming it's ms
                        year = year
                    )
                )
            }
        }
        audioList
    }
}
