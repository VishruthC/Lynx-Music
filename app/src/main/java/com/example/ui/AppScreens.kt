package com.example.ui

import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Brush
import coil.compose.AsyncImage
import com.example.data.PlaylistSong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var showPlayerFullScreen by remember { mutableStateOf(false) }
    var selectedPlaylist by remember { mutableStateOf<OnyxPlaceholder?>(null) }

    var showEqualizer by remember { mutableStateOf(false) }
    var showCurationScreen by remember { mutableStateOf(false) }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.loadLocalAudio()
            selectedPlaylist = OnyxPlaceholder(
                id = "local_audio",
                title = "Local Device Audio",
                type = "local",
                subtitle = "Audio files on this device",
                imageUrl = "https://images.unsplash.com/photo-1542281286-9e0a16bb7366" // Generic tech/audio background
            )
        }
    }

    BackHandler(enabled = showCurationScreen || showEqualizer || showPlayerFullScreen || selectedPlaylist != null) {
        if (showCurationScreen) {
            showCurationScreen = false
        } else if (showEqualizer) {
            showEqualizer = false
        } else if (showPlayerFullScreen) {
            showPlayerFullScreen = false
        } else if (selectedPlaylist != null) {
            selectedPlaylist = null
        }
    }

    val uiAlpha by androidx.compose.animation.core.animateFloatAsState(targetValue = if (selectedPlaylist != null) 0f else 1f, label = "uiAlpha")

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AppDynamicBackground(viewModel)

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Column {
                    SmartMiniPlayer(
                        viewModel = viewModel,
                        showPlayerFullScreen = showPlayerFullScreen,
                        onFullScreenToggle = { showPlayerFullScreen = true }
                    )
                    OnyxBottomNavigationBar(
                        activeTab = uiState.activeTab,
                        onTabSelected = { 
                            selectedPlaylist = null
                            showCurationScreen = false
                            showEqualizer = false
                            viewModel.resetImportState()
                            viewModel.setActiveTab(it)
                        }
                    )
                }
            }
        ) { paddingValues ->
            // Active Tab Content
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                AnimatedContent(
                    targetState = Pair(uiState.activeTab, selectedPlaylist),
                    transitionSpec = {
                        if (targetState.second != null && initialState.second == null) {
                            slideIntoContainer(
                                towards = androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Start,
                                animationSpec = tween(300, easing = LinearOutSlowInEasing)
                            ) togetherWith fadeOut(animationSpec = tween(300))
                        } else if (targetState.second == null && initialState.second != null) {
                            fadeIn(animationSpec = tween(300)) togetherWith slideOutOfContainer(
                                towards = androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.End,
                                animationSpec = tween(300, easing = FastOutLinearInEasing)
                            )
                        } else {
                            fadeIn(animationSpec = tween(300, easing = LinearOutSlowInEasing)) togetherWith fadeOut(animationSpec = tween(300, easing = FastOutLinearInEasing))
                        }
                    },
                    label = "tab_transition"
                ) { (tab, playlist) ->
                    when (tab) {
                        0 -> {
                            if (playlist == null) {
                                OnyxHomeScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    viewModel = viewModel,
                                    onPlaylistClick = { selectedPlaylist = it }
                                )
                            } else {
                                OnyxPlaylistScreen(
                                    playlist = playlist,
                                    viewModel = viewModel,
                                    onBack = { selectedPlaylist = null }
                                )
                            }
                        }
                        1 -> {
                            if (playlist == null) {
                                SearchScreen(viewModel = viewModel, onItemSelected = { selectedPlaylist = it })
                            } else {
                                OnyxPlaylistScreen(
                                    playlist = playlist,
                                    viewModel = viewModel,
                                    onBack = { selectedPlaylist = null }
                                )
                            }
                        }
                        2 -> {
                            if (playlist == null) {
                                LibraryScreen(
                                    viewModel = viewModel,
                                    onPlaylistClick = { selectedPlaylist = it },
                                    onOpenCuration = { showCurationScreen = true },
                                    onLaunchPermission = { perm -> permissionLauncher.launch(perm) }
                                )
                            } else {
                                OnyxPlaylistScreen(
                                    playlist = playlist,
                                    viewModel = viewModel,
                                    onBack = { selectedPlaylist = null }
                                )
                            }
                        }
                        3 -> {
                            val importState by viewModel.importState.collectAsStateWithLifecycle()
                            if (importState is ImportState.Preview) {
                                val state = importState as ImportState.Preview
                                OnyxPlaylistScreen(
                                    playlist = state.playlist,
                                    viewModel = viewModel,
                                    onBack = { viewModel.resetImportState() },
                                    previewSongs = state.songs,
                                    onSavePreview = { viewModel.saveImportedPlaylist() }
                                )
                            }
                            else if (importState is ImportState.Success) {
                                val state = importState as ImportState.Success
                                OnyxPlaylistScreen(
                                    playlist = state.playlist,
                                    viewModel = viewModel,
                                    onBack = { viewModel.resetImportState() },
                                    previewSongs = state.songs,
                                    onSavePreview = null
                                )
                            }
                            else {
                                ImportScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPlayerFullScreen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val scope = rememberCoroutineScope()
        ModalBottomSheet(
            onDismissRequest = { showPlayerFullScreen = false },
            sheetState = sheetState,
            containerColor = Color.Transparent,
            dragHandle = null,
            scrimColor = Color.Black.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxSize()
        ) {
            FullScreenPlayer(
                viewModel = viewModel,
                onClose = { 
                    scope.launch { sheetState.hide(); showPlayerFullScreen = false } 
                },
                onOpenEqualizer = { showEqualizer = true }
            )
        }
    }

    if (showEqualizer) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showEqualizer = false },
            sheetState = sheetState,
            containerColor = Color(0xFF121212),
            dragHandle = null,
            modifier = Modifier.fillMaxSize()
        ) {
            EqualizerScreen(onBack = { showEqualizer = false })
        }
    }

    if (showCurationScreen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCurationScreen = false },
            sheetState = sheetState,
            containerColor = Color(0xFF121212),
            dragHandle = null,
            modifier = Modifier.fillMaxSize()
        ) {
            CurationScreen(viewModel = viewModel, onBack = { showCurationScreen = false })
        }
    }
}

@Composable
fun OnyxBottomNavigationBar(activeTab: Int, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Translucent frosted glass effect for nav bar
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(vertical = 12.dp, horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OnyxNavItem(
            icon = Icons.Default.Home,
            label = "Home",
            selected = activeTab == 0,
            onClick = { onTabSelected(0) }
        )
        OnyxNavItem(
            icon = Icons.Default.Search,
            label = "Search",
            selected = activeTab == 1,
            onClick = { onTabSelected(1) }
        )
        OnyxNavItem(
            icon = Icons.Default.LibraryMusic,
            label = "Library",
            selected = activeTab == 2,
            onClick = { onTabSelected(2) }
        )
        OnyxNavItem(
            icon = Icons.Default.CloudDownload,
            label = "Import",
            selected = activeTab == 3,
            onClick = { onTabSelected(3) }
        )
    }
}

@Composable
fun OnyxNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) Color.White else Color.White.copy(alpha = 0.5f),
        animationSpec = tween(300, easing = LinearOutSlowInEasing),
        label = "nav_color_anim"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.padding(8.dp)
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun OnyxMiniPlayer(
    song: PlaylistSong,
    isPlaying: Boolean,
    progress: Float,
    onPlayPause: () -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        if (dragAmount < -10) {
                            change.consume()
                            onClick()
                        }
                    }
                )
            }
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val listImageUrl = song.artworkUrl.replace("500x500", "150x150").replace("1000x1000", "150x150")
            AsyncImage(
                model = listImageUrl,
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = song.artist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = onPlayPause, modifier = Modifier.testTag("mini_play_pause")) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White
                )
            }
        }
        // Progress Bar
        val animatedProgress by animateFloatAsState(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(1000, easing = LinearEasing),
            label = "mini_progress_anim"
        )
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color.White.copy(alpha = 0.2f))) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = animatedProgress)
                    .height(2.dp)
                    .background(Color.White)
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: MainViewModel, onItemSelected: (OnyxPlaceholder) -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val customPlaylists by viewModel.customPlaylists.collectAsStateWithLifecycle()
    val savedSongs by viewModel.savedSongs.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Songs", "Playlists", "Albums", "Artists", "Genres")
    
    var selectedSort by remember { mutableStateOf("Relevance") }
    val sortOptions = listOf("Relevance", "Release Date", "Popularity", "Duration")

    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            query = uiState.searchQuery,
            onQueryChange = viewModel::updateSearchQuery,
            onSearch = { },
            active = false,
            onActiveChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search songs or artists...") }
        ) { }

        // Search Filters
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { filter ->
                val isSelected = selectedFilter == filter
                val bgColor by animateColorAsState(if (isSelected) Color.White else Color.White.copy(alpha = 0.1f), tween(300))
                val contentColor by animateColorAsState(if (isSelected) Color.Black else Color.White, tween(300))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(bgColor)
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = filter,
                        color = contentColor,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        // Sort Options
        if (uiState.searchQuery.isNotEmpty() && !uiState.isSearching) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortOptions) { sortOp ->
                    val isSelected = selectedSort == sortOp
                    val bgColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.05f), tween(300))
                    val contentColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White, tween(300))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .clickable { selectedSort = sortOp }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = sortOp,
                            color = contentColor,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (uiState.searchQuery.isEmpty()) {
            Text("Browse All", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            val genres = listOf(
                Pair("Pop", Color(0xFFE13300)),
                Pair("Hip-Hop", Color(0xFFFF4632)),
                Pair("Rock", Color(0xFFE91429)),
                Pair("Jazz", Color(0xFF777777)),
                Pair("Electronic", Color(0xFFaf2896)),
                Pair("Classical", Color(0xFF1E3264)),
                Pair("R&B", Color(0xFFba5d07)),
                Pair("Country", Color(0xFFd84000)),
                Pair("Indie", Color(0xFFe1118c)),
                Pair("Metal", Color(0xFFe91429)),
                Pair("Chill", Color(0xFF477d95)),
                Pair("Workout", Color(0xFF777777)),
                Pair("Party", Color(0xFFaf2896)),
                Pair("Focus", Color(0xFF1e3264)),
                Pair("Sleep", Color(0xFF1e3264)),
                Pair("Romance", Color(0xFF8c1932))
            )
            
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(genres.size) { index ->
                    val genre = genres[index]
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.5f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(genre.second)
                            .clickable {
                                viewModel.updateSearchQuery(genre.first)
                            }
                            .padding(12.dp)
                    ) {
                        Text(
                            text = genre.first,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else if (uiState.isSearching) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val displayedResults = uiState.searchResults.let { list ->
                when (selectedSort) {
                    "Release Date" -> list.sortedByDescending { it.year }
                    "Popularity" -> list.sortedByDescending { it.playCount }
                    "Duration" -> list.sortedByDescending { it.duration }
                    else -> list
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedFilter == "Songs" || selectedFilter == "All" || selectedFilter == "Genres") {
                    if (displayedResults.isEmpty() && (selectedFilter == "Songs" || selectedFilter == "Genres")) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                Text("No results found for '${uiState.searchQuery}'.", color = Color.Gray)
                            }
                        }
                    } else {
                        itemsIndexed(displayedResults) { _, song ->
                            SongItem(
                                song = song, 
                                onClick = { viewModel.playSong(song) },
                                customPlaylists = customPlaylists,
                                onAddToPlaylist = { id, s -> viewModel.addSongToCustomPlaylist(id, s) },
                                isSaved = savedSongs.any { it.id == song.id },
                                onToggleSave = { viewModel.toggleSaveSong(song) }
                            )
                        }
                    }
                }
                if (selectedFilter == "Artists" || selectedFilter == "All") {
                    val artistsToShow = uiState.artistResults
                    if (artistsToShow.isNotEmpty()) {
                        items(artistsToShow) { placeholder ->
                            FilterResultItem(
                                title = placeholder.title,
                                subtitle = placeholder.subtitle,
                                imageUrl = placeholder.imageUrl,
                                onClick = { onItemSelected(placeholder) }
                            )
                        }
                    } else if (selectedFilter == "Artists") {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                Text("No Artists found for '${uiState.searchQuery}'.", color = Color.Gray)
                            }
                        }
                    }
                }
                if (selectedFilter == "Albums" || selectedFilter == "All") {
                    val albumsToShow = uiState.albumResults
                    if (albumsToShow.isNotEmpty()) {
                        items(albumsToShow) { placeholder ->
                            FilterResultItem(
                                title = placeholder.title,
                                subtitle = placeholder.subtitle,
                                imageUrl = placeholder.imageUrl,
                                onClick = { onItemSelected(placeholder) }
                            )
                        }
                    } else if (selectedFilter == "Albums") {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                Text("No Albums found for '${uiState.searchQuery}'.", color = Color.Gray)
                            }
                        }
                    }
                }
                if (selectedFilter == "Playlists" || selectedFilter == "All") {
                    val playlistsToShow = uiState.playlistResults
                    if (playlistsToShow.isNotEmpty()) {
                        items(playlistsToShow) { placeholder ->
                            FilterResultItem(
                                title = placeholder.title,
                                subtitle = placeholder.subtitle,
                                imageUrl = placeholder.imageUrl,
                                onClick = { onItemSelected(placeholder) }
                            )
                        }
                    } else if (selectedFilter == "Playlists") {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                Text("No Playlists found for '${uiState.searchQuery}'.", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(viewModel: MainViewModel, onPlaylistClick: (OnyxPlaceholder) -> Unit, onOpenCuration: () -> Unit = {}, onLaunchPermission: (String) -> Unit = {}) {
    val savedSongs by viewModel.savedSongs.collectAsStateWithLifecycle()
    val localSongs by viewModel.localAudio.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    
    val localAudioPlaceholder = OnyxPlaceholder(
        id = "local_audio",
        title = "Local Device Audio",
        type = "local",
        subtitle = "Audio files on this device",
        imageUrl = "https://images.unsplash.com/photo-1542281286-9e0a16bb7366" // Generic tech/audio background
    )

    val context = androidx.compose.ui.platform.LocalContext.current

    // Default Liked Songs playlist
    val likedSongsPlaylist = OnyxPlaceholder(
        id = "liked_songs",
        title = "Liked Songs",
        type = "liked",
        subtitle = "${savedSongs.size} tracks",
        imageUrl = "https://images.unsplash.com/photo-1518199266791-5375a8316d4d?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=60&sat=-100" // A grey monochrome heart
    )

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
                    Text("Create", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF282828)
        )
    }

    val customPlaylists by viewModel.customPlaylists.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Library",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Row {
                IconButton(onClick = onOpenCuration) {
                    Icon(Icons.Default.Star, contentDescription = "Curate Playlist", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Create Playlist", tint = Color.White)
                }
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize().animateContentSize(animationSpec = tween(400, easing = LinearOutSlowInEasing)),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onPlaylistClick(likedSongsPlaylist) }
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val listImageUrl = likedSongsPlaylist.imageUrl.replace("500x500", "150x150").replace("1000x1000", "150x150")
                    coil.compose.AsyncImage(
                        model = listImageUrl,
                        contentDescription = "Liked Songs",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = likedSongsPlaylist.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = likedSongsPlaylist.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { 
                            val perm = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                android.Manifest.permission.READ_MEDIA_AUDIO
                            } else {
                                android.Manifest.permission.READ_EXTERNAL_STORAGE
                            }
                            if (androidx.core.content.ContextCompat.checkSelfPermission(context, perm) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                viewModel.loadLocalAudio()
                                onPlaylistClick(localAudioPlaceholder)
                            } else {
                                onLaunchPermission(perm)
                            }
                        }
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val listImageUrl = localAudioPlaceholder.imageUrl.replace("500x500", "150x150").replace("1000x1000", "150x150")
                    coil.compose.AsyncImage(
                        model = listImageUrl,
                        contentDescription = "Local Audio",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = localAudioPlaceholder.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (localSongs.isNotEmpty()) "${localSongs.size} tracks" else localAudioPlaceholder.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            items(customPlaylists) { playlist ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onPlaylistClick(playlist) }
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val listImageUrl = playlist.imageUrl.replace("500x500", "150x150").replace("1000x1000", "150x150")
                    coil.compose.AsyncImage(
                        model = listImageUrl,
                        contentDescription = null,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = playlist.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = playlist.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = { viewModel.deleteCustomPlaylist(playlist.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Playlist", tint = Color.White.copy(alpha = 0.4f))
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val savedSongs by viewModel.savedSongs.collectAsStateWithLifecycle()
    val customPlaylists by viewModel.customPlaylists.collectAsStateWithLifecycle()
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Spacer(modifier = Modifier.height(32.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Music Lover", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Local Profile", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.7f))
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Text("Listening Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        if (savedSongs.isEmpty()) {
            Text("No activity yet. Listen to some tracks!", color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.6f))
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                itemsIndexed(savedSongs) { index, song ->
                    SongItem(
                        song = song, 
                        onClick = { viewModel.playPlaylist(savedSongs, index) },
                        customPlaylists = customPlaylists,
                        onAddToPlaylist = { id, s -> viewModel.addSongToCustomPlaylist(id, s) },
                        isSaved = true,
                        onToggleSave = { viewModel.toggleSaveSong(song) }
                    )
                }
            }
        }
    }
}

@Composable
fun SongItem(
    song: PlaylistSong,
    onClick: () -> Unit,
    customPlaylists: List<OnyxPlaceholder> = emptyList(),
    onAddToPlaylist: (String, PlaylistSong) -> Unit = { _,_ -> },
    isSaved: Boolean = false,
    onToggleSave: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val listImageUrl = song.artworkUrl.replace("500x500", "150x150").replace("1000x1000", "150x150")
        AsyncImage(
            model = listImageUrl,
            contentDescription = "Artwork",
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        SongOptionsMenu(
            song = song,
            customPlaylists = customPlaylists,
            onAddToPlaylist = onAddToPlaylist,
            isSaved = isSaved,
            onToggleSave = onToggleSave
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongOptionsMenu(
    song: PlaylistSong,
    customPlaylists: List<OnyxPlaceholder>,
    onAddToPlaylist: (String, PlaylistSong) -> Unit,
    isSaved: Boolean = false,
    onToggleSave: () -> Unit = {},
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onOpenEqualizer: (() -> Unit)? = null
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onToggleSave) {
            Icon(
                imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = tint
            )
        }
        Box {
            IconButton(onClick = { showBottomSheet = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = tint
                )
            }
        }
    }
        
    if (showBottomSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, top = 8.dp)
            ) {
                if (onOpenEqualizer != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showBottomSheet = false
                                onOpenEqualizer()
                            }
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Equalizer",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Text(
                            text = "Equalizer",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 24.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = "Add '${song.title}' to Playlist",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    fontWeight = FontWeight.Bold
                )
                
                if (customPlaylists.isEmpty()) {
                    Text(
                        text = "No custom playlists available.",
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                } else {
                    LazyColumn {
                        items(customPlaylists) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onAddToPlaylist(playlist.id, song)
                                        showBottomSheet = false
                                    }
                                    .padding(horizontal = 24.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                                Text(
                                    text = playlist.title,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MiniPlayer(song: PlaylistSong, isPlaying: Boolean, onPlayPause: () -> Unit, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val listImageUrl = song.artworkUrl.replace("500x500", "150x150").replace("1000x1000", "150x150")
            AsyncImage(
                model = listImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = song.artist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onPlayPause, modifier = Modifier.testTag("mini_play_pause")) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play"
                )
            }
        }
    }
}

@Composable
fun FilterResultItem(title: String, subtitle: String, imageUrl: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val listImageUrl = imageUrl.replace("500x500", "150x150").replace("1000x1000", "150x150")
        AsyncImage(
            model = listImageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(if (subtitle == "Artist") CircleShape else RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AppDynamicBackground(viewModel: MainViewModel) {
    val currentSong by androidx.compose.runtime.remember(viewModel) {
        viewModel.playerState.map { it.currentSong }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(initialValue = null)

    if (currentSong != null) {
        AsyncImage(
            model = currentSong!!.artworkUrl,
            contentDescription = null,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(30.dp)
                .clip(androidx.compose.ui.graphics.RectangleShape)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E1E1E), Color.Black)
                    )
                )
        )
    }
}

@Composable
fun SmartMiniPlayer(viewModel: MainViewModel, showPlayerFullScreen: Boolean, onFullScreenToggle: () -> Unit) {
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    if (playerState.currentSong != null) {
        OnyxMiniPlayer(
            song = playerState.currentSong!!,
            isPlaying = playerState.isPlaying,
            progress = if (playerState.duration > 0) playerState.playbackPosition.toFloat() / playerState.duration else 0f,
            onPlayPause = { viewModel.playerController.togglePlayPause() },
            onClick = onFullScreenToggle
        )
    }
}
