package com.example.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import java.util.Calendar

data class OnyxPlaceholder(
    val id: String = "",
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val type: String = "playlist"
)

val moodsData = listOf("Party", "Workout", "Focus", "Relax", "Commute", "Late Night")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnyxHomeScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    onPlaylistClick: (OnyxPlaceholder) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val isOfflineMode by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshingHomeData.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshHomeData() },
        state = rememberPullToRefreshState(),
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp, top = 24.dp)
        ) {
            item { HomeHeader() }

            if (!isOnline && !isOfflineMode) {
                item { OfflineBanner(viewModel) }
            } else if (isOfflineMode) {
                item { OnlineBanner(viewModel) }
            }

            when (val homeState = uiState.homeState) {
                is HomeUiState.Loading -> {
                    item { FullShimmerEffect() }
                }
                is HomeUiState.Error -> {
                    item { ErrorView(message = homeState.message) }
                }
                is HomeUiState.Success -> {
                    if (homeState.heroCarousel.isNotEmpty()) {
                        item { HeroCarouselSection(items = homeState.heroCarousel, onItemClick = onPlaylistClick) }
                    }

                    if (homeState.recentlyPlayed.isNotEmpty()) {
                        item { RecentlyPlayedSection(items = homeState.recentlyPlayed, onItemClick = onPlaylistClick) }
                    }

                    item { MoodChips(onMoodClick = { mood -> Log.d("OnyxHome", "Mood: $mood") }) }

                    if (homeState.trendingNow.isNotEmpty()) {
                        item { StandardSection("Trending now", homeState.trendingNow, onPlaylistClick, cardType = CardType.LARGE) }
                    }

                    if (homeState.madeForYou.isNotEmpty()) {
                        item { StandardSection("Made for you", homeState.madeForYou, onPlaylistClick, cardType = CardType.MEDIUM) }
                    }

                    if (homeState.newReleases.isNotEmpty()) {
                        item { StandardSection("New releases", homeState.newReleases, onPlaylistClick, cardType = CardType.MEDIUM) }
                    }

                    if (homeState.recommendedForYou.isNotEmpty() && homeState.personalizedArtistName.isNotEmpty()) {
                        item { StandardSection("Because you liked ${homeState.personalizedArtistName}", homeState.recommendedForYou, onPlaylistClick, cardType = CardType.MEDIUM) }
                    }

                    if (homeState.topCharts.isNotEmpty()) {
                        item { TopChartsSection(items = homeState.topCharts, onItemClick = onPlaylistClick) }
                    }

                    if (homeState.dailyMix.isNotEmpty()) {
                        item { WideCardsSection("Your daily mix", homeState.dailyMix, onPlaylistClick) }
                    }

                    if (homeState.popularArtists.isNotEmpty()) {
                        item { ArtistsSection("Popular artists", homeState.popularArtists, onPlaylistClick) }
                    }

                    if (homeState.topHits.isNotEmpty()) {
                        item { StandardSection("Top hits", homeState.topHits, onPlaylistClick, cardType = CardType.LARGE) }
                    }

                    if (homeState.workoutMusic.isNotEmpty()) {
                        item { WideCardsSection("Workout", homeState.workoutMusic, onPlaylistClick) }
                    }

                    if (homeState.focusMusic.isNotEmpty()) {
                        item { WideCardsSection("Focus", homeState.focusMusic, onPlaylistClick) }
                    }

                    if (homeState.topAlbums.isNotEmpty()) {
                        item { StandardSection("Top albums", homeState.topAlbums, onPlaylistClick, cardType = CardType.MEDIUM) }
                    }

                    if (homeState.topArtists.isNotEmpty()) {
                        item { ArtistsSection("Top artists", homeState.topArtists, onPlaylistClick) }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeHeader() {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 6 -> "Good evening"
            hour < 12 -> "Good morning"
            hour < 18 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White, modifier = Modifier.size(22.dp))
            }
            IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
fun OfflineBanner(viewModel: MainViewModel) {
    Button(
        onClick = { viewModel.setOfflineMode(true) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Text("No connection. Switch to offline mode", color = MaterialTheme.colorScheme.onErrorContainer)
    }
}

@Composable
fun OnlineBanner(viewModel: MainViewModel) {
    Button(
        onClick = { viewModel.setOfflineMode(false) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Text("Offline mode active. Switch to online", color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
fun HeroCarouselSection(items: List<OnyxPlaceholder>, onItemClick: (OnyxPlaceholder) -> Unit) {
    val pageCount = items.size
    var currentPage by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            currentPage = (currentPage + 1) % pageCount
            if (listState.layoutInfo.totalItemsCount > 0) {
                listState.animateScrollToItem(currentPage)
            }
        }
    }

    Column {
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items, key = { it.id }) { item ->
                HeroCard(item, onClick = { onItemClick(item) })
            }
        }
        if (pageCount > 1) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pageCount) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(width = if (index == currentPage) 20.dp else 6.dp, height = 6.dp)
                            .clip(CircleShape)
                            .background(if (index == currentPage) Color.White else Color.White.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
fun HeroCard(item: OnyxPlaceholder, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(340.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xE6000000)),
                        startY = 50f
                    )
                )
        )
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        ) {
            Text("Trending now", style = MaterialTheme.typography.labelMedium, color = Color(0xFF7dd3fc), fontWeight = FontWeight.SemiBold)
            Text(item.title, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun RecentlyPlayedSection(items: List<OnyxPlaceholder>, onItemClick: (OnyxPlaceholder) -> Unit) {
    val displayItems = items.take(10)
    Column {
        SectionTitle("Jump back in")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(displayItems, key = { it.id }) { item ->
                RecentlyPlayedCard(item, onClick = { onItemClick(item) })
            }
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
fun RecentlyPlayedCard(item: OnyxPlaceholder, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(120.dp).clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp))) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(item.imageUrl).crossfade(true).build(),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun MoodChips(onMoodClick: (String) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(moodsData, key = { it }) { mood ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF2A2A2E),
                modifier = Modifier.clickable { onMoodClick(mood) }
            ) {
                Text(mood, color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

enum class CardType { SMALL, MEDIUM, LARGE }

@Composable
fun StandardSection(title: String, items: List<OnyxPlaceholder>, onItemClick: (OnyxPlaceholder) -> Unit, cardType: CardType = CardType.MEDIUM) {
    if (items.isEmpty()) return
    val displayItems = items.take(20)
    Column {
        SectionTitle(title)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(displayItems, key = { it.id }) { item ->
                when (cardType) {
                    CardType.SMALL -> SmallCard(item, onClick = { onItemClick(item) })
                    CardType.MEDIUM -> MediumCard(item, onClick = { onItemClick(item) })
                    CardType.LARGE -> LargeCard(item, onClick = { onItemClick(item) })
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(28.dp))
}

@Composable
fun SmallCard(item: OnyxPlaceholder, onClick: () -> Unit) {
    Column(modifier = Modifier.width(120.dp).clickable { onClick() }) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(Color(0xFF2A2A2E))) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(item.imageUrl.replace("500x500", "300x300").replace("1000x1000", "300x300")).crossfade(true).build(),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp)
        Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun MediumCard(item: OnyxPlaceholder, onClick: () -> Unit) {
    Column(modifier = Modifier.width(160.dp).clickable { onClick() }) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(14.dp)).background(Color(0xFF2A2A2E))) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(item.imageUrl.replace("500x500", "300x300").replace("1000x1000", "300x300")).crossfade(true).build(),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp)
        Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun LargeCard(item: OnyxPlaceholder, onClick: () -> Unit) {
    Column(modifier = Modifier.width(200.dp).clickable { onClick() }) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(16.dp)).background(Color(0xFF2A2A2E))) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(item.imageUrl.replace("500x500", "400x400").replace("1000x1000", "400x400")).crossfade(true).build(),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun TopChartsSection(items: List<OnyxPlaceholder>, onItemClick: (OnyxPlaceholder) -> Unit) {
    if (items.isEmpty()) return
    val displayItems = items.take(5)
    Column {
        SectionTitle("Top charts")
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            displayItems.forEachIndexed { index, item ->
                TopChartItem(index = index + 1, item = item, onClick = { onItemClick(item) })
                if (index < displayItems.size - 1) {
                    Divider(color = Color.White.copy(alpha = 0.06f), thickness = 1.dp)
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(28.dp))
}

@Composable
fun TopChartItem(index: Int, item: OnyxPlaceholder, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = index.toString().padStart(2, '0'),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (index <= 3) Color(0xFF7dd3fc) else Color.White.copy(alpha = 0.4f),
            modifier = Modifier.width(32.dp)
        )
        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2A2A2E))) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(item.imageUrl.replace("500x500", "150x150").replace("1000x1000", "150x150")).crossfade(true).build(),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)).clickable { onClick() }, contentAlignment = Alignment.Center) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun WideCardsSection(title: String, items: List<OnyxPlaceholder>, onItemClick: (OnyxPlaceholder) -> Unit) {
    if (items.isEmpty()) return
    val displayItems = items.take(10)
    Column {
        SectionTitle(title)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(displayItems, key = { it.id }) { item ->
                WideCard(item, onClick = { onItemClick(item) })
            }
        }
    }
    Spacer(modifier = Modifier.height(28.dp))
}

@Composable
fun WideCard(item: OnyxPlaceholder, onClick: () -> Unit) {
    Row(
        modifier = Modifier.width(300.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF1C1C1F)).clickable { onClick() }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF2A2A2E))) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(item.imageUrl.replace("500x500", "200x200").replace("1000x1000", "200x200")).crossfade(true).build(),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

@Composable
fun ArtistsSection(title: String, items: List<OnyxPlaceholder>, onItemClick: (OnyxPlaceholder) -> Unit) {
    if (items.isEmpty()) return
    val displayItems = items.take(10)
    Column {
        SectionTitle(title)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(displayItems, key = { it.id }) { item ->
                ArtistCard(item, onClick = { onItemClick(item) })
            }
        }
    }
    Spacer(modifier = Modifier.height(28.dp))
}

@Composable
fun ArtistCard(item: OnyxPlaceholder, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(100.dp).clickable { onClick() }) {
        Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Color(0xFF2A2A2E))) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(item.imageUrl.replace("500x500", "300x300").replace("1000x1000", "300x300")).crossfade(true).build(),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f), maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

// --- Loading & Error ---

@Composable
fun FullShimmerEffect() {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFF2A2A2E)))
        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.width(200.dp).height(28.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2A2A2E)))
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            repeat(3) { ShimmerCard() }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Box(modifier = Modifier.width(150.dp).height(28.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2A2A2E)))
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            repeat(3) { ShimmerCard() }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Box(modifier = Modifier.width(180.dp).height(28.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2A2A2E)))
        Spacer(modifier = Modifier.height(16.dp))
        repeat(3) { ShimmerListItem() }
    }
}

@Composable
fun ShimmerCard() {
    Column {
        Box(modifier = Modifier.size(160.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF2A2A2E)))
        Spacer(modifier = Modifier.height(10.dp))
        Box(modifier = Modifier.width(140.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF2A2A2E)))
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.width(100.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF2A2A2E)))
    }
}

@Composable
fun ShimmerListItem() {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF2A2A2E)))
        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2A2A2E)))
        Column(modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.width(120.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF2A2A2E)))
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.width(80.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF2A2A2E)))
        }
    }
}

@Composable
fun ErrorView(message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Unable to load home", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f))
    }
}
