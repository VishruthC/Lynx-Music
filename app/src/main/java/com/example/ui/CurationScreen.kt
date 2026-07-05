package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurationScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val suggestedCurationSongs by viewModel.suggestedCurationSongs.collectAsStateWithLifecycle()
    val isCurating by viewModel.isCurating.collectAsStateWithLifecycle()
    val customPlaylists by viewModel.customPlaylists.collectAsStateWithLifecycle()
    val savedSongs by viewModel.savedSongs.collectAsStateWithLifecycle()

    var selectedContext by remember { mutableStateOf<String?>(null) }
    var playlistToAddTo by remember { mutableStateOf<String?>(null) }

    val moods = listOf("Happy", "Chill", "Focus", "Sad", "Romantic", "Energetic")
    val activities = listOf("Workout", "Study", "Party", "Sleep", "Commute", "Gaming")

    LaunchedEffect(Unit) {
        viewModel.clearCuration()
    }

    var showDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Create Playlist", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = { Text("Playlist name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha=0.5f)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPlaylistName.isNotBlank()) {
                        viewModel.createCustomPlaylist(newPlaylistName)
                        showDialog = false
                        newPlaylistName = ""
                    }
                }) {
                    Text("Create", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Curate a Playlist", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                if (playlistToAddTo == null) {
                    Text(
                        "1. Select a Custom Playlist",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (customPlaylists.isEmpty()) {
                        Button(
                            onClick = { showDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Create Playlist", color = Color.White)
                        }
                    } else {
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Playlist", tint = Color.White)
                            }
                            customPlaylists.forEach { playlist ->
                                FilterChip(
                                    selected = playlistToAddTo == playlist.id,
                                    onClick = { playlistToAddTo = playlist.id },
                                    label = { Text(playlist.title) },
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Adding to: ${customPlaylists.find { it.id == playlistToAddTo }?.title}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { playlistToAddTo = null; selectedContext = null; viewModel.clearCuration() }) {
                            Text("Change")
                        }
                    }
                }

                if (playlistToAddTo != null) {
                    Text(
                        "2. Pick a Vibe",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text("Moods", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        moods.forEach { mood ->
                            FilterChip(
                                selected = selectedContext == mood,
                                onClick = {
                                    selectedContext = mood
                                    viewModel.fetchSuggestionsForContext(mood)
                                },
                                label = { Text(mood) },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Activities", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        activities.forEach { activity ->
                            FilterChip(
                                selected = selectedContext == activity,
                                onClick = {
                                    selectedContext = activity
                                    viewModel.fetchSuggestionsForContext(activity)
                                },
                                label = { Text(activity) },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isCurating) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (selectedContext != null && suggestedCurationSongs.isNotEmpty()) {
                Text(
                    "Suggested Songs for '$selectedContext'",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(suggestedCurationSongs) { song ->
                        SongItem(
                            song = song,
                            onClick = { /* Could preview song */ },
                            customPlaylists = customPlaylists,
                            onAddToPlaylist = { id, s -> viewModel.addSongToCustomPlaylist(id, s) },
                            isSaved = savedSongs.any { it.id == song.id },
                            onToggleSave = { viewModel.toggleSaveSong(song) }
                        )
                    }
                }
            } else if (selectedContext != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No suggestions found for '$selectedContext'", color = Color.Gray)
                }
            }
        }
    }
}
