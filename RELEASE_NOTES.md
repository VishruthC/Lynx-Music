# Release Notes

## LynxMusic v1.0

A high-fidelity JioSaavn music player with offline playback, personalized recommendations, and synced lyrics.

---

## What's New

### Home Feed Redesign (Web)

The web home screen has been completely redesigned to match the Android layout, featuring:

- **Hero Carousel** — Auto-rotating featured content with pagination dots
- **Jump Back In** — Recently played songs from your listening history
- **Mood Chips** — Quick filters for Party, Workout, Focus, Relax, Commute, Late Night
- **Trending Now** — What's popular right now
- **Made For You** — Personalized recommendations based on your top artist
- **New Releases** — Fresh albums and singles
- **Top Charts** — Numbered chart listings with play buttons
- **Daily Mix / Workout / Focus** — Contextual curated sections
- **Popular Artists** — Circular artist avatars with top tracks

### Personalized Recommendations (Android & Web)

Your home feed now adapts to your taste:

- **Listening Activity Tracking**: Every play is recorded locally (no server, fully private)
  - Android: Room database (SQLite)
  - Web: localStorage (via Zustand persist)
- **Top Artists**: Automatically computed from your play history
- **"Made For You"**: Searches for playlists matching your top artist + "Mix"
- **"Because You Liked {Artist}"**: Direct playlist recommendations based on your top artist
- **"Jump Back In"**: Recently played songs, ordered by recency

### First-Time Onboarding (Android & Web)

New users now see an elegant introduction on first launch:

- **5 animated slides** with smooth transitions
- Features overview: Streaming, Personalization, Import, Local Music (Android), Lyrics
- Skip option available at any time
- Progress dots for navigation feedback

**Android**:
  - persiste via SharedPreferences
  - Highlights **Local Music Access** as a key feature

**Web**:
  - persists via localStorage
  - Clean, centered UI with icon + title + description per slide

### Library Page (Web)

The previously dead `/library` sidebar link is now functional:

- Shows **Liked Songs** tile
- Lists all **Custom Playlists** with song counts
- Navigate to any playlist with one click

### Security Improvements

- **Hardened `.gitignore`**: Comprehensive rules to prevent accidental commits of:
  - Keystore files (`.jks`, `.p12`, `.pem`)
  - Build artifacts (`.apk`, `.aab`, `.dex`)
  - ProGuard/R8 `mapping.txt` (prevents APK deobfuscation)
  - Secrets (`.env`, `secrets.properties`, `google-services.json`)
  - IDE files, logs, temp files

---

## Improvements

| Area | Change |
|---|---|
| **Web Home** | Now mirrors Android Onyx layout with horizontal scrolling sections |
| **Web Loading** | Shimmer skeleton placeholders instead of blank screen |
| **Web Error** | Graceful error messages with retry capability |
| **Android Made For You** | Now personalized by top artist instead of static "Mix" search |
| **Android MusicRepository** | Added `getMadeForYou(topArtist)` for dynamic personalization |
| **Web MusicRepository** | Added `getMadeForYou()` helper accepting optional artist query |
| **.gitignore** | Security-focused rules to prevent credential leaks |
| **README.md** | Complete rewrite with features, architecture, and setup guide |

---

## Bug Fixes

- Fixed `FullscreenPlayer.css` syntax error (`width: 100'` → `width: 100%`)
- Fixed web `/library` sidebar link (was a dead link, now shows playlists)
- Fixed TypeScript `enum` to string literal conversion for `erasableSyntaxOnly` compliance
- Removed unused variable `ARTIST_W` and `WIDE_W` from web Home page

---

## Known Issues

- **Web**: No offline playback (Android only via ExoPlayer cache)
- **Web**: No equalizer or audio effects (Android only)
- **Web**: No background playback (requires Service Worker / PWA)
- **Android**: Deprecated APIs noted in `AudioEffectManager` (Virtualizer, BassBoost) — functionality intact but flagged for future migration
- **Android**: `fallbackToDestructiveMigration()` on Room should specify `true/false` explicitly (deprecated since Room 2.4)
- **Both**: "Because you liked" and "Made for you" may show duplicate results if the top artist search returns the same playlists

---

## Technical Details

### Android
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 16)
- **Kotlin**: Latest stable
- **Architecture**: MVVM with StateFlow + Room + ExoPlayer

### Web
- **TypeScript**: 6.0
- **React**: 19
- **Vite**: 8
- **Zustand**: 5 (with persist middleware)

---

## Dependencies (Key Changes)

No dependency version changes. All changes are code-level.

---

## Migration Notes

- No breaking changes for existing users
- Onboarding will show once automatically for new installs
- Existing users' listening history is preserved (localStorage / Room)

---

## Credits

Built with Kotlin, Jetpack Compose, React, and TypeScript.
