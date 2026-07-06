<div align="center">
  <img src="./web/public/favicon.svg" alt="LynxMusic" height="64" />

  # LynxMusic

  A high-fidelity JioSaavn music player with offline playback, personalized recommendations, and synced lyrics.  
  Built for **Android** (Kotlin/Jetpack Compose) and the **Web** (React/TypeScript).

  [![Android](https://img.shields.io/badge/Android-Kotlin%20%2F%20Jetpack%20Compose-7F52FF?style=flat-square&logo=android&logoColor=white)](./app)
  [![Web](https://img.shields.io/badge/Web-React%20%2F%20TypeScript-3178C6?style=flat-square&logo=react&logoColor=white)](./web)
  [![API](https://img.shields.io/badge/API-JioSaavn%20v4-ff6b6b?style=flat-square)](https://jiosaavn-api-v4.vercel.app)
  [![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)

  [Overview](#overview) • [Features](#features) • [Tech Stack](#tech-stack) • [Getting Started](#getting-started) • [Project Structure](#project-structure) • [Architecture](#architecture) • [APIs](#external-apis)

</div>

## Overview

LynxMusic is a dual-platform music streaming application that acts as a custom frontend for the **JioSaavn** catalog. It provides a feature-rich, Spotify-like experience with personalized home feeds, playlist management, offline playback (Android), a 5-band equalizer (Android), and synced lyrics on both platforms.

The app tracks your listening activity locally to serve personalized recommendations: recently played songs appear in "Jump back in", your most-played artist drives "Made for you" and "Because you liked" sections, and the home feed adapts to your taste over time.

> [!NOTE]
> LynxMusic does **not** host any music. It streams content from the JioSaavn API and displays lyrics from LrcLib.net. Internet access is required unless downloading for offline playback on Android.

## Features

### Music Playback

| Feature | Android | Web |
|---|---|---|
| Stream songs, albums, playlists from JioSaavn | &check; | &check; |
| Play/pause, skip, seek, progress tracking | &check; | &check; |
| Full-screen now-playing with album art | &check; | &check; |
| Persistent mini-player / player bar | &check; | &check; |
| Background playback (MediaSessionService) | &check; | &mdash; |
| Playback speed control (1x-2x) | &check; | &mdash; |
| Shuffle & repeat modes (off/all/one) | &check; | &check; |
| Volume control | &check; | &check; |

### Personalization & Home Feed

| Feature | Android | Web |
|---|---|---|
| Hero carousel (auto-rotating) | &check; | &check; |
| Recently played ("Jump back in") | &check; | &check; |
| Mood chips (Party, Workout, Focus...) | &check; | &check; |
| Trending now, New releases, Top charts | &check; | &check; |
| **Made for you** (personalized by top artist) | &check; | &check; |
| **Because you liked {artist}** (recommendations) | &check; | &check; |
| Daily Mix, Workout, Focus sections | &check; | &check; |
| Popular artists & top artists | &check; | &check; |
| Listening activity tracking (local) | &check; | &check; |
| Pull-to-refresh home feed | &check; | &mdash; |

### Lyrics

| Feature | Android | Web |
|---|---|---|
| Fetch synced & plain lyrics from LrcLib.net | &check; | &check; |
| Auto-scrolling synced lyrics (`[mm:ss.xx]` parser) | &check; | &check; |
| Lyrics in full-screen player | &check; | &check; |

### Playlist Management

| Feature | Android | Web |
|---|---|---|
| Custom playlists (create, delete, rename) | &check; | &check; |
| Add/remove songs from playlists | &check; | &check; |
| Like/save songs to library | &check; | &check; |
| **Import from YouTube/Invidious/Piped** | &check; | &check; |
| Offline download of playlists | &check; | &mdash; |
| Local device audio playback | &check; | &mdash; |

### Search

| Feature | Android | Web |
|---|---|---|
| Search songs | &check; | &check; |
| Search artists, albums, playlists | &check; | &mdash; |
| Genre browsing | &check; | &mdash; |
| Filter & sort results | &check; | &mdash; |

### Audio & Effects (Android only)

- 5+ band graphic equalizer with presets
- Bass Boost effect
- Virtual Surround effect
- ExoPlayer-based audio engine with 500MB LRU cache
- Offline playback via downloaded playlists

## Tech Stack

<details open>
<summary><h3>Android</h3></summary>

| Layer | Technology |
|---|---|
| **Language** | Kotlin |
| **UI** | Jetpack Compose (Material 3) |
| **Architecture** | MVVM (ViewModel + StateFlow) |
| **Navigation** | Custom tab-based (Compose) |
| **Networking** | Retrofit + OkHttp + Moshi |
| **Local DB** | Room (SQLite) |
| **Audio** | Media3 ExoPlayer + MediaSessionService |
| **Image Loading** | Coil |
| **Caching** | Media3 SimpleCache (500MB) |
| **Audio Effects** | Android AudioFx (Equalizer, BassBoost, Virtualizer) |
| **Async** | Kotlin Coroutines + Flow |
| **Min SDK** | 24 / Target SDK 36 |

</details>

<details open>
<summary><h3>Web</h3></summary>

| Layer | Technology |
|---|---|
| **Language** | TypeScript 6 |
| **Framework** | React 19 |
| **Bundler** | Vite 8 |
| **Routing** | react-router-dom 7 |
| **State Management** | Zustand 5 (persisted) |
| **HTTP Client** | Axios |
| **Styling** | Vanilla CSS |
| **Audio** | HTML5 Audio element |

</details>

## Getting Started

### Web

```bash
# Navigate to the web app
cd web

# Install dependencies
npm install

# Start the dev server
npm run dev

# Build for production
npm run build
```

The web app will be available at `http://localhost:5173`.

### Android

**Prerequisites:** Android Studio, JDK 17+

1. Open Android Studio and select **Open**.
2. Choose the project root directory (`LynxMusic`).
3. Allow Android Studio to sync Gradle and fix any incompatibilities.
4. Create a `.env` file in the project root with your Gemini API key (see `.env.example`):
   ```env
   GEMINI_API_KEY=your_key_here
   ```
5. Remove this line from `app/build.gradle.kts` if present:
   ```kotlin
   signingConfig = signingConfigs.getByName("debugConfig")
   ```
6. Build and run on an emulator or physical device.

> [!TIP]
> The Gemini API key is reserved for future AI features and is **not required** for the app to function. The app works fully with the JioSaavn API alone.

### Environment Variables

| Variable | Required | Description |
|---|---|---|
| `GEMINI_API_KEY` | No | Reserved for AI-powered features |

## Project Structure

```
LynxMusic/
├── app/                              # Android app (Kotlin)
│   └── src/main/java/com/example/
│       ├── MainActivity.kt           # Entry point, DI setup
│       ├── data/                     # Data layer
│       │   ├── ApiModels.kt          # API DTOs
│       │   ├── AppDatabase.kt        # Room database
│       │   ├── MusicRepository.kt    # Central repository
│       │   ├── SaavnApi.kt           # Retrofit API interface
│       │   ├── RetrofitClient.kt     # HTTP client
│       │   ├── PlaylistImporter.kt   # Spotify/YouTube import
│       │   ├── UserActivityDao.kt    # Activity tracking DAO
│       │   ├── UserActivityEntity.kt # Room entities
│       │   ├── UserActivityTracker.kt# Listening tracker
│       │   ├── NetworkMonitor.kt     # Connectivity observer
│       │   └── LocalAudioHelper.kt   # Device audio scanner
│       ├── player/                   # Audio engine
│       │   ├── PlayerController.kt   # ExoPlayer wrapper
│       │   ├── MusicService.kt       # MediaSessionService
│       │   ├── CacheModule.kt        # 500MB LRU cache
│       │   ├── AudioEffectManager.kt # Equalizer + effects
│       │   └── PlaylistDownloader.kt # Offline downloads
│       └── ui/                       # UI (Jetpack Compose)
│           ├── MainViewModel.kt      # ViewModel + UI state
│           ├── OnyxHomeScreen.kt     # Home feed
│           ├── AppScreens.kt         # Navigation shell
│           ├── OnyxPlaylistScreen.kt # Playlist detail
│           ├── FullScreenPlayer.kt   # Now-playing + lyrics
│           ├── EqualizerScreen.kt    # EQ + effects
│           ├── CurationScreen.kt     # AI playlist curation
│           ├── ImportScreen.kt       # Import from Spotify/YT
│           └── theme/                # Color, Typography, Theme
│
├── web/                              # Web app (React/TypeScript)
│   └── src/
│       ├── App.tsx                   # Router + layout
│       ├── api/
│       │   ├── saavn.ts              # JioSaavn API calls
│       │   └── external.ts           # YouTube import API
│       ├── store/
│       │   ├── usePlayerStore.ts     # Audio playback state
│       │   ├── usePlaylistStore.ts   # Custom playlists
│       │   └── useUserActivityStore.ts # Listening history
│       ├── pages/
│       │   ├── Home.tsx              # Home feed
│       │   ├── Search.tsx            # Song search
│       │   └── PlaylistDetails.tsx   # Playlist/album detail
│       ├── components/
│       │   ├── Sidebar.tsx           # Navigation sidebar
│       │   ├── Section.tsx           # Generic grid section
│       │   ├── PlayerBar.tsx         # Bottom player bar
│       │   └── FullscreenPlayer.tsx  # Full-screen + lyrics
│       └── types/
│           └── index.ts             # API response types
│
├── build.gradle.kts                  # Root Gradle config
├── settings.gradle.kts               # Gradle settings
└── metadata.json                     # Project metadata
```

## Architecture

### Data Flow

Both platforms follow a similar data flow:

```
JioSaavn API v4  ──────┐
LrcLib.net              │
  │                     │
  ▼                     ▼
┌──────────┐     ┌──────────────┐
│ Retrofit  │     │   Axios      │
│ (OkHttp)  │     │  (Web)      │
└────┬─────┘     └──────┬───────┘
     │                  │
     ▼                  ▼
┌──────────────┐  ┌──────────────┐
│  Repository  │  │  API module  │
│  (Room +     │  │  (direct)    │
│   Network)   │  │              │
└──────┬───────┘  └──────┬───────┘
       │                 │
       ▼                 ▼
┌──────────────┐  ┌──────────────┐
│  ViewModel   │  │  Zustand     │
│  (StateFlow) │  │  Stores      │
└──────┬───────┘  └──────┬───────┘
       │                 │
       ▼                 ▼
┌──────────────┐  ┌──────────────┐
│  Compose UI  │  │  React       │
│  (Material3) │  │  Components  │
└──────────────┘  └──────────────┘
```

### Android

- **MVVM pattern** with `MainViewModel` holding all UI state as `StateFlow` objects.
- **Room database** with 4 entities: `PlaylistSong`, `CustomPlaylistEntity`, `CustomPlaylistSong`, `UserActivityEntity` + `ListeningSession`.
- **ExoPlayer** runs in a `MediaSessionService` for background playback.
- **Navigation** uses a custom `Scaffold` with bottom tabs (Home, Search, Library, Import). Full-screen player, equalizer, and curation screens are `ModalBottomSheet` instances.
- **UserActivityTracker** observes `PlayerState` and logs listening sessions to Room, feeding into personalized recommendations.

### Web

- **React Router v7** with routes for `/`, `/search`, `/playlist/:id`, `/album/:id`.
- **Zustand** stores manage state: `usePlayerStore` (audio playback via HTML5 `<Audio>`), `usePlaylistStore` (custom playlists), `useUserActivityStore` (listening history).
- **localStorage** persistence via Zustand's `persist` middleware.
- **No backend** -- the web app is fully client-side, communicating directly with the JioSaavn API and LrcLib.net.

## External APIs

| Service | Usage | Both Platforms |
|---|---|---|
| [JioSaavn API v4](https://jiosaavn-api-v4.vercel.app) | Music search, streaming, playlist/album details | &check; |
| [LrcLib.net](https://lrclib.net) | Synced and plain lyrics retrieval | &check; |
| Invidious / Piped | YouTube playlist import (no API key needed) | Android only |

## Personalized Recommendations

LynxMusic personalizes the home feed based on your listening activity:

1. **Tracking**: Every song you play is recorded locally with artist, title, timestamp, and play count.
2. **Top artists**: Your most-played artists are computed from the tracking data.
3. **Personalized sections**:
   - **Jump back in** - Recently played songs
   - **Made for you** - Playlists matching your top artist + "Mix"
   - **Because you liked {artist}** - Playlists matching your top artist

> [!IMPORTANT]
> All tracking data is stored **locally** on your device. No data is sent to any server.
> - Android: Room database (SQLite)
> - Web: localStorage (via Zustand persist)

---

<div align="center">
  Built with ❤️ using Kotlin, Jetpack Compose, React, and TypeScript.
</div>
