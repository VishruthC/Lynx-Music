import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export interface ActivityEntry {
  songId: string;
  title: string;
  artist: string;
  artworkUrl: string;
  playCount: number;
  lastPlayedTimestamp: number;
}

export interface TopArtistResult {
  artist: string;
  total: number;
}

interface UserActivityState {
  entries: ActivityEntry[];
  trackPlay: (songId: string, title: string, artist: string, artworkUrl: string) => void;
  getRecentActivity: (limit?: number) => ActivityEntry[];
  getTopArtists: (limit?: number) => TopArtistResult[];
  getTopPlayed: (limit?: number) => ActivityEntry[];
}

export const useUserActivityStore = create<UserActivityState>()(
  persist(
    (set, get) => ({
      entries: [],

      trackPlay: (songId, title, artist, artworkUrl) => {
        set((state) => {
          const existing = state.entries.find((e) => e.songId === songId);
          const now = Date.now();

          if (existing) {
            return {
              entries: state.entries.map((e) =>
                e.songId === songId
                  ? {
                      ...e,
                      playCount: e.playCount + 1,
                      lastPlayedTimestamp: now,
                      title,
                      artist,
                      artworkUrl,
                    }
                  : e
              ),
            };
          }

          return {
            entries: [
              ...state.entries,
              {
                songId,
                title,
                artist,
                artworkUrl,
                playCount: 1,
                lastPlayedTimestamp: now,
              },
            ],
          };
        });
      },

      getRecentActivity: (limit = 10) => {
        return [...get().entries]
          .sort((a, b) => b.lastPlayedTimestamp - a.lastPlayedTimestamp)
          .slice(0, limit);
      },

      getTopArtists: (limit = 5) => {
        const artistMap = new Map<string, number>();
        for (const entry of get().entries) {
          artistMap.set(entry.artist, (artistMap.get(entry.artist) || 0) + entry.playCount);
        }
        return [...artistMap.entries()]
          .map(([artist, total]) => ({ artist, total }))
          .sort((a, b) => b.total - a.total)
          .slice(0, limit);
      },

      getTopPlayed: (limit = 10) => {
        return [...get().entries]
          .sort((a, b) => b.playCount - a.playCount)
          .slice(0, limit);
      },
    }),
    {
      name: 'lynx-user-activity',
    }
  )
);
