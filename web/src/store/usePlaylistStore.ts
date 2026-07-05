import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { PlaylistSong } from './usePlayerStore';

export interface CustomPlaylist {
  id: string;
  name: string;
  imageUrl: string;
  songs: PlaylistSong[];
}

interface PlaylistState {
  customPlaylists: CustomPlaylist[];
  
  addPlaylist: (playlist: CustomPlaylist) => void;
  removePlaylist: (id: string) => void;
  addSongToPlaylist: (playlistId: string, song: PlaylistSong) => void;
}

export const usePlaylistStore = create<PlaylistState>()(
  persist(
    (set) => ({
      customPlaylists: [],

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
      }
    }),
    {
      name: 'lynx-playlists'
    }
  )
);
