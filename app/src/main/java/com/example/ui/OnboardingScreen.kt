package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OnboardingPage(
    val icon: Int,
    val title: String,
    val description: String
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    var currentPage by remember { mutableIntStateOf(0) }

    val pages = listOf(
        OnboardingPage(
            icon = 0,
            title = "Welcome to LynxMusic",
            description = "Your high-fidelity music streaming companion. Discover, stream, and enjoy millions of songs from the JioSaavn catalog."
        ),
        OnboardingPage(
            icon = 1,
            title = "Personalized For You",
            description = "The more you listen, the better it gets. Your home feed adapts with 'Made for you' and 'Because you liked' sections based on your top artists."
        ),
        OnboardingPage(
            icon = 2,
            title = "Import & Organize",
            description = "Create custom playlists, import from YouTube, and save your favorites. Your library stays organized across sessions."
        ),
        OnboardingPage(
            icon = 3,
            title = "Local Music Access",
            description = "Browse and play your own audio files directly in the app. Your personal collection, right alongside streaming content."
        ),
        OnboardingPage(
            icon = 4,
            title = "Synced Lyrics",
            description = "Sing along with perfectly synced lyrics that scroll in real-time as the song plays. Available for both streaming and local tracks."
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0a0a0b)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 48.dp)
            ) {
                pages.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .width(if (index == currentPage) 24.dp else 8.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (index == currentPage) Color.White
                                else Color.White.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            // Page content
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(400)) +
                        slideInVertically(animationSpec = tween(400)) { it / 4 })
                        .togetherWith(
                            fadeOut(animationSpec = tween(300)) +
                            slideOutVertically(animationSpec = tween(300)) { it / 4 }
                        )
                },
                label = "page_transition"
            ) { page ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Icon
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF1c1c1f))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val tint = Color(0xFF7dd3fc)
                        when (page) {
                            0 -> Icon(imageVector = Icons.Filled.MusicNote, contentDescription = null,
                                tint = tint, modifier = Modifier.size(48.dp))
                            1 -> Icon(imageVector = Icons.Filled.Person, contentDescription = null,
                                tint = tint, modifier = Modifier.size(48.dp))
                            2 -> Icon(imageVector = Icons.Filled.CloudDownload, contentDescription = null,
                                tint = tint, modifier = Modifier.size(48.dp))
                            3 -> Icon(imageVector = Icons.Filled.PhoneAndroid, contentDescription = null,
                                tint = tint, modifier = Modifier.size(48.dp))
                            4 -> Icon(imageVector = Icons.Filled.Lyrics, contentDescription = null,
                                tint = tint, modifier = Modifier.size(48.dp))
                            else -> {}
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = pages[page].title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = pages[page].description,
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Skip",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onComplete() }
                )

                Button(
                    onClick = {
                        if (currentPage < pages.size - 1) {
                            currentPage++
                        } else {
                            onComplete()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.defaultMinSize(minWidth = 120.dp)
                ) {
                    Text(
                        text = if (currentPage == pages.size - 1) "Get Started" else "Next",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
