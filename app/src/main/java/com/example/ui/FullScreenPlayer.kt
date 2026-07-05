package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.PlaylistSong
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenPlayer(
    viewModel: MainViewModel,
    onClose: () -> Unit,
    onOpenEqualizer: () -> Unit = {}
) {
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val lyrics by viewModel.lyrics.collectAsStateWithLifecycle()
    val isLoadingLyrics by viewModel.isLoadingLyrics.collectAsStateWithLifecycle()
    val savedSongs by viewModel.savedSongs.collectAsStateWithLifecycle()
    val customPlaylists by viewModel.customPlaylists.collectAsStateWithLifecycle()
    
    val currentSong = playerState.currentSong ?: return

    val isSaved = savedSongs.any { it.id == currentSong.id }

    var showFullScreenLyrics by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler(enabled = showFullScreenLyrics) {
        showFullScreenLyrics = false
    }

    val progress = if (playerState.duration > 0) {
        playerState.playbackPosition.toFloat() / playerState.duration.toFloat()
    } else {
        0f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        AsyncImage(
            model = currentSong.artworkUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Top Bar
                Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Close", modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onBackground)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("NOW PLAYING", style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp), letterSpacing = 2.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    Text(currentSong.title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Artwork
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFD1E4FF),
                                Color(0xFF3B4858)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = currentSong.artworkUrl,
                    contentDescription = "Album Art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // High-Fidelity Badge
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("HI-RES OFF-LINE", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title & Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentSong.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = currentSong.artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SongOptionsMenu(
                        song = currentSong,
                        customPlaylists = customPlaylists,
                        onAddToPlaylist = { id, s -> viewModel.addSongToCustomPlaylist(id, s) },
                        isSaved = isSaved,
                        onToggleSave = { viewModel.toggleSaveCurrentSong() },
                        tint = MaterialTheme.colorScheme.primary,
                        onOpenEqualizer = onOpenEqualizer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Bar
            var sliderPosition by remember { mutableStateOf<Float?>(null) }
            val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(1000, easing = LinearEasing),
                label = "slider_progress_anim"
            )
            val currentSliderValue = sliderPosition ?: animatedProgress

            Slider(
                value = currentSliderValue,
                onValueChange = { sliderPosition = it },
                onValueChangeFinished = {
                    sliderPosition?.let { viewModel.playerController.seekTo(it) }
                    sliderPosition = null
                },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            // Time mapping
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).offset(y = (-8).dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatTime(playerState.playbackPosition), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                Text(formatTime(playerState.duration), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var showSpeedDialog by remember { mutableStateOf(false) }
                IconButton(onClick = { showSpeedDialog = true }) {
                    Icon(Icons.Default.Speed, contentDescription = "Speed", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                }
                if (showSpeedDialog) {
                    AlertDialog(
                        onDismissRequest = { showSpeedDialog = false },
                        title = { Text("Playback Speed") },
                        text = {
                            Column {
                                listOf(1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                    TextButton(onClick = {
                                        viewModel.playerController.setPlaybackSpeed(speed)
                                        showSpeedDialog = false
                                    }) {
                                        Text("${speed}x")
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showSpeedDialog = false }) { Text("Close") }
                        }
                    )
                }

                IconButton(onClick = { viewModel.playerController.skipToPrevious() }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onBackground)
                }
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { viewModel.playerController.togglePlayPause() }
                        .testTag("play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                IconButton(onClick = { viewModel.playerController.skipToNext() }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onBackground)
                }
                
                val context = androidx.compose.ui.platform.LocalContext.current
                IconButton(onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out this song!")
                        putExtra(android.content.Intent.EXTRA_TEXT, "I'm listening to ${currentSong.title} by ${currentSong.artist}. Listen here: ${currentSong.streamUrl}")
                    }
                    context.startActivity(android.content.Intent.createChooser(intent, "Share via..."))
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Lyrics
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable { if (!lyrics.isNullOrBlank()) showFullScreenLyrics = true }
                    .padding(24.dp)
            ) {
                if (isLoadingLyrics) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
                } else if (!lyrics.isNullOrBlank()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                            Icon(Icons.Default.Lyrics, contentDescription = "Lyrics", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("LYRICS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        val lyricLines = remember(lyrics) { parseSyncedLyrics(lyrics ?: "") }
                        
                        if (lyricLines.isNotEmpty()) {
                            val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                            
                            val currentIndex = lyricLines.indexOfLast { line -> playerState.playbackPosition >= line.timeMs }
                            
                            LaunchedEffect(currentIndex) {
                                if (currentIndex >= 0) {
                                    val targetIndex = (currentIndex - 2).coerceAtLeast(0)
                                    listState.animateScrollToItem(targetIndex)
                                }
                            }
                            
                            androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                                items(lyricLines.size) { index ->
                                    val line = lyricLines[index]
                                    val isCurrent = index == currentIndex
                                    Text(
                                        text = line.text,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        lineHeight = 28.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = lyrics!!,
                                modifier = Modifier.verticalScroll(rememberScrollState()),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                lineHeight = 28.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No lyrics available",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        }
        
        androidx.compose.animation.AnimatedVisibility(
            visible = showFullScreenLyrics,
            enter = androidx.compose.animation.slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(400, easing = LinearOutSlowInEasing)
            ) + androidx.compose.animation.fadeIn(animationSpec = tween(400)),
            exit = androidx.compose.animation.slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(400, easing = FastOutLinearInEasing)
            ) + androidx.compose.animation.fadeOut(animationSpec = tween(400))
        ) {
            FullScreenLyricsView(
                lyrics = lyrics,
                playerState = playerState,
                onClose = { showFullScreenLyrics = false }
            )
        }
    }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

data class LyricLine(val timeMs: Long, val text: String)

fun parseSyncedLyrics(syncedLyrics: String): List<LyricLine> {
    val regex = Regex("^\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)")
    return syncedLyrics.lines().mapNotNull { line ->
        val matchResult = regex.matchEntire(line.trim())
        if (matchResult != null) {
            val minutes = matchResult.groupValues[1].toLong()
            val seconds = matchResult.groupValues[2].toLong()
            val fractional = matchResult.groupValues[3]
            // If length is 2, it's hundredths. If 3, it's milliseconds.
            val ms = if (fractional.length == 2) fractional.toLong() * 10 else fractional.toLong()
            val text = matchResult.groupValues[4].trim()
            val timeMs = (minutes * 60 + seconds) * 1000 + ms
            LyricLine(timeMs, text)
        } else null
    }
}

@Composable
fun FullScreenLyricsView(
    lyrics: String?,
    playerState: com.example.player.PlayerState,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        val song = playerState.currentSong
        if (song != null) {
            AsyncImage(
                model = song.artworkUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
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
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Top Bar
                Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Close", modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onBackground)
                }
                Text("LYRICS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.size(48.dp)) // To center the title
            }
            
            if (!lyrics.isNullOrBlank()) {
                val lyricLines = remember(lyrics) { parseSyncedLyrics(lyrics) }
                
                if (lyricLines.isNotEmpty()) {
                    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                    val currentIndex = lyricLines.indexOfLast { line -> playerState.playbackPosition >= line.timeMs }
                    
                    LaunchedEffect(currentIndex) {
                        if (currentIndex >= 0) {
                            val targetIndex = (currentIndex - 3).coerceAtLeast(0)
                            listState.animateScrollToItem(targetIndex)
                        }
                    }
                    
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(bottom = 120.dp) // Leave space at bottom
                    ) {
                        items(lyricLines.size) { index ->
                            val line = lyricLines[index]
                            val isCurrent = index == currentIndex
                            Text(
                                text = line.text,
                                style = if (isCurrent) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                lineHeight = if (isCurrent) 40.sp else 36.sp,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = lyrics,
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
    }
}
