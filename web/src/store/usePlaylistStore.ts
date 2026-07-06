import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { PlaylistSong } from './usePlayerStore';

export interface CustomPlaylist {
  id: string;
  name: string;
  imageUrl: string;
  songs: PlaylistSong[];
  description?: string;
}

interface PlaylistState {
  customPlaylists: CustomPlaylist[];
  likedSongs: PlaylistSong[];
  
  addPlaylist: (playlist: CustomPlaylist) => void;
  removePlaylist: (id: string) => void;
  addSongToPlaylist: (playlistId: string, song: PlaylistSong) => void;
  toggleLikeSong: (song: PlaylistSong) => void;
}

export const usePlaylistStore = create<PlaylistState>()(
  persist(
    (set) => ({
      customPlaylists: [],
      likedSongs: [],

      addPlaylist: (playlist) => {
        set((state) => ({
          customPlaylists: [...state.customPlaylists, playlist]
        }));
      },

      removePlaylist: (id) => {
        set((state) => ({
          customPlaylists: state.customPlaylists.filter(p => p.id !== id)
        }));
      },

      addSongToPlaylist: (playlistId, song) => {
        set((state) => ({
          customPlaylists: state.customPlaylists.map(p => 
            p.id === playlistId ? { ...p, songs: [...p.songs, song] } : p
          )
        }));
      },

      toggleLikeSong: (song) => {
        set((state) => {
          const isLiked = state.likedSongs.some(s => s.id === song.id);
          if (isLiked) {
            return {
              likedSongs: state.likedSongs.filter(s => s.id !== song.id)
            };
          } else {
            return {
              likedSongs: [...state.likedSongs, song]
            };
          }
        });
      }
    }),
    {
      name: 'lynx-playlists'
    }
  )
);
