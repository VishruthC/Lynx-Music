# Project Requirements Document (PRD)

## Tech Stack
- **Framework**: Vite + React 19 + TypeScript.
- **Styling**: Vanilla CSS + Variables (following Style Guide).
- **State**: Zustand (Player, Playlists).
- **Icons**: Lucide React.
- **API**: Axios (JioSaavn V4).

## File Structure (Refined)
```
web/
├── DESIGN/           # Context Artifacts
├── src/
│   ├── api/          # Data fetching
│   ├── components/   # UI components
│   ├── pages/        # Route pages
│   ├── store/        # Zustand stores
│   ├── types/        # TS interfaces
│   └── App.css       # Global theme
```

## Page Sections & Priority
1. **Global Player**: P0 (Core functionality).
2. **Fullscreen Player**: P0 (Immersive experience).
3. **Home Page**: P1 (Engagement).
4. **Search Page**: P1 (Discovery).
5. **Playlist Details**: P1 (User content).

## Responsiveness
- **Desktop**: Sidebar + Full grid.
- **Mobile**: Hidden sidebar + Mobile-bottom-nav + Simplified player.
