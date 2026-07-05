package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.MusicRepository
import com.example.data.UserActivityTracker
import com.example.player.PlayerController
import com.example.ui.MainScreen
import com.example.ui.MainViewModel
import com.example.ui.OnboardingScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var playerController: PlayerController
    private lateinit var mainViewModel: MainViewModel

    companion object {
        private const val PREFS_NAME = "lynx_prefs"
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val onboardingComplete = prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)

        val database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "wave_music_db")
            .fallbackToDestructiveMigration()
            .build()
        val repository = MusicRepository(database.playlistDao(), database.userActivityDao())
        val localAudioHelper = com.example.data.LocalAudioHelper(applicationContext)
        val playlistDownloader = com.example.player.PlaylistDownloader(applicationContext)
        val networkMonitor = com.example.data.NetworkMonitor(applicationContext)
        val playlistImporter = com.example.data.PlaylistImporter(repository)
        playerController = PlayerController(this)
        playerController.connect()

        UserActivityTracker(database.userActivityDao(), playerController.playerState)

        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(repository, localAudioHelper, playerController, playlistDownloader, networkMonitor, playlistImporter) as T
            }
        }
        mainViewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
        handleIntent(intent)

        var showOnboarding by mutableStateOf(!onboardingComplete)

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (showOnboarding) {
                        OnboardingScreen(onComplete = {
                            prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, true).apply()
                            showOnboarding = false
                        })
                    } else {
                        MainScreen(mainViewModel)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent) {
        val data = intent.data
        if (data != null && data.scheme == "onyx" && data.host == "spotify-auth") {
            val code = data.getQueryParameter("code")
            if (code != null) {
                if (::mainViewModel.isInitialized) {
                    mainViewModel.handleSpotifyAuthCode(code)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerController.release()
    }
}
