package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.PlaylistSong

@Composable
fun OnyxPlaylistScreen(
    playlist: OnyxPlaceholder,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    previewSongs: List<PlaylistSong>? = null,
    onSavePreview: (() -> Unit)? = null
) {
    val savedSongs by viewModel.savedSongs.collectAsStateWithLifecycle()
    val customPlaylists by viewModel.customPlaylists.collectAsStateWithLifecycle()
    
    val displaySongs = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<List<PlaylistSong>>(previewSongs ?: emptyList()) }
    val isLoading = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(previewSongs == null) }

    androidx.compose.runtime.LaunchedEffect(playlist.id, playlist.title, previewSongs) {
        if (previewSongs != null) {
            displaySongs.value = previewSongs
            isLoading.value = false
        } else {
            isLoading.value = true
            val results = viewModel.getPlaylistDetails(playlist)
            displaySongs.value = results
            isLoading.value = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Blurred background derived from playlist artwork - scoped to the top 600dp to fade out completely
        Box(modifier = Modifier.fillMaxWidth().height(600.dp)) {
            AsyncImage(
                model = playlist.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(80.dp)
                    .clip(androidx.compose.ui.graphics.RectangleShape)
            )
            // Gradient overlay to blend into the pure black list smoothly
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Black.copy(alpha = 0.2f),
                            0.5f to Color.Black.copy(alpha = 0.6f),
                            1.0f to Color.Black
                        )
                    )
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().animateContentSize(animationSpec = tween(400, easing = LinearOutSlowInEasing)),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                    ) {
                    IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Playlist Art
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        AsyncImage(
                            model = playlist.imageUrl,
                            contentDescription = playlist.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = playlist.title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = playlist.subtitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    if (onSavePreview != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onSavePreview,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Import", modifier = Modifier.size(20.dp), tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save to Library", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${displaySongs.value.size} tracks",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Download button logic
                            val downloadingPlaylists by viewModel.downloadingPlaylists.collectAsStateWithLifecycle()
                            val downloadProgressMap by viewModel.downloadProgress.collectAsStateWithLifecycle()
                            val isDownloaded = viewModel.isPlaylistDownloaded(displaySongs.value)
                            val isDownloading = downloadingPlaylists.contains(playlist.id)
                            val progress = downloadProgressMap[playlist.id] ?: 0f

                            if (isDownloaded || progress >= 1f) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Downloaded",
                                    tint = MaterialTheme.colorScheme.primary, // Or Color.Green / White
                                    modifier = Modifier.size(32.dp)
                                )
                            } else if (isDownloading) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp)) {
                                    CircularProgressIndicator(
                                        progress = progress, // Or use a determined overload
                                        modifier = Modifier.fillMaxSize(),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 3.dp
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = { 
                                        if (displaySongs.value.isNotEmpty()) {
                                            viewModel.downloadPlaylist(playlist.id, displaySongs.value)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download, // ArrowDownward/Download
                                        contentDescription = "Download",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            // Play Button
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color.White) // White accent
                                    .clickable {
                                        if (displaySongs.value.isNotEmpty()) {
                                            viewModel.playPlaylist(displaySongs.value, 0)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black, modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    if (isLoading.value) {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    } else if (displaySongs.value.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text("No songs found", color = Color.Gray)
                        }
                    }
                }
            }
            
            // Songs List
            if (!isLoading.value) {
                itemsIndexed(displaySongs.value) { index, song ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (displaySongs.value.isNotEmpty()) {
                                    viewModel.playPlaylist(displaySongs.value, index)
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    Text(
                        text = "${index + 1}",
                        color = Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.width(32.dp)
                    )
                    
                    AsyncImage(
                        model = song.artworkUrl.replace("500x500", "150x150").replace("1000x1000", "150x150"),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    SongOptionsMenu(
                        song = song,
                        customPlaylists = customPlaylists,
                        onAddToPlaylist = { id, s -> viewModel.addSongToCustomPlaylist(id, s) },
                        isSaved = savedSongs.any { it.id == song.id },
                        onToggleSave = { viewModel.toggleSaveSong(song) },
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
}
