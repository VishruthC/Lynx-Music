import { create } from 'zustand';
import { useUserActivityStore } from './useUserActivityStore';

export interface PlaylistSong {
  id: string;
  title: string;
  artist: string;
  artworkUrl: string;
  streamUrl: string;
}

interface PlayerState {
  currentSong: PlaylistSong | null;
  queue: PlaylistSong[];
  currentIndex: number;
  isPlaying: boolean;
  duration: number;
  currentTime: number;
  volume: number;
  isFullscreen: boolean;
  lyrics: string | null;
  isLoadingLyrics: boolean;
  isShuffle: boolean;
  repeatMode: 'off' | 'all' | 'one';

  // Actions
  setQueue: (songs: PlaylistSong[], index: number) => void;
  playSong: (song: PlaylistSong) => void;
  togglePlay: () => void;
  next: () => void;
  previous: () => void;
  setVolume: (volume: number) => void;
  updateProgress: (time: number, duration: number) => void;
  seek: (time: number) => void;
  toggleFullscreen: () => void;
  setLyrics: (lyrics: string | null) => void;
  setIsLoadingLyrics: (isLoading: boolean) => void;
  toggleShuffle: () => void;
  toggleRepeat: () => void;
}

const audio = new Audio();

export const usePlayerStore = create<PlayerState>((set, get) => {
  
  audio.addEventListener('timeupdate', () => {
    set({ currentTime: audio.currentTime, duration: audio.duration || 0 });
  });

  audio.addEventListener('ended', () => {
    const { repeatMode } = get();
    if (repeatMode === 'one') {
      audio.currentTime = 0;
      audio.play();
    } else {
      get().next();
    }
  });

  return {
    currentSong: null,
    queue: [],
    currentIndex: -1,
    isPlaying: false,
    duration: 0,
    currentTime: 0,
    volume: 1,
    isFullscreen: false,
    lyrics: null,
    isLoadingLyrics: false,
    isShuffle: false,
    repeatMode: 'off',

    setQueue: (songs, index) => {
      const song = songs[index];
      if (!song) return;

      audio.src = song.streamUrl;
      audio.play();

      useUserActivityStore.getState().trackPlay(song.id, song.title, song.artist, song.artworkUrl);

      set({ 
        queue: songs, 
        currentIndex: index, 
        currentSong: song,
        isPlaying: true 
      });
    },

    playSong: (song) => {
      audio.src = song.streamUrl;
      audio.play();

      useUserActivityStore.getState().trackPlay(song.id, song.title, song.artist, song.artworkUrl);

      set({ 
        currentSong: song, 
        queue: [song], 
        currentIndex: 0, 
        isPlaying: true 
      });
    },

    togglePlay: () => {
      const { isPlaying } = get();
      if (isPlaying) {
        audio.pause();
      } else {
        audio.play();
      }
      set({ isPlaying: !isPlaying });
    },

    next: () => {
      const { queue, currentIndex, isShuffle, repeatMode } = get();
      if (queue.length === 0) return;

      let nextIndex = currentIndex;

      if (isShuffle) {
        nextIndex = Math.floor(Math.random() * queue.length);
      } else {
        nextIndex = currentIndex + 1;
        if (nextIndex >= queue.length) {
          if (repeatMode === 'all') {
            nextIndex = 0;
          } else {
            return; // End of queue, no repeat
          }
        }
      }

      const nextSong = queue[nextIndex];
      if (!nextSong) return;

      audio.src = nextSong.streamUrl;
      audio.play();

      useUserActivityStore.getState().trackPlay(nextSong.id, nextSong.title, nextSong.artist, nextSong.artworkUrl);

      set({ 
        currentIndex: nextIndex, 
        currentSong: nextSong,
        isPlaying: true 
      });
    },

    previous: () => {
      const { queue, currentIndex, currentTime } = get();
      if (queue.length === 0) return;

      // If playing for more than 3 seconds, restart current song
      if (currentTime > 3) {
        audio.currentTime = 0;
        audio.play();
        return;
      }

      let prevIndex = currentIndex - 1;
      if (prevIndex < 0) {
        prevIndex = queue.length - 1; // loop to end
      }

      const prevSong = queue[prevIndex];
      if (!prevSong) return;

      audio.src = prevSong.streamUrl;
      audio.play();

      useUserActivityStore.getState().trackPlay(prevSong.id, prevSong.title, prevSong.artist, prevSong.artworkUrl);

      set({ 
        currentIndex: prevIndex, 
        currentSong: prevSong,
        isPlaying: true 
      });
    },

    setVolume: (volume) => {
      audio.volume = volume;
      set({ volume });
    },

    updateProgress: (time, duration) => {
      set({ currentTime: time, duration });
    },

    seek: (time) => {
      audio.currentTime = time;
      set({ currentTime: time });
    },

    toggleFullscreen: () => {
      set({ isFullscreen: !get().isFullscreen });
    },

    setLyrics: (lyrics) => {
      set({ lyrics });
    },

    setIsLoadingLyrics: (isLoading) => {
      set({ isLoadingLyrics: isLoading });
    },

    toggleShuffle: () => {
      set({ isShuffle: !get().isShuffle });
    },

    toggleRepeat: () => {
      const current = get().repeatMode;
      const nextMode = current === 'off' ? 'all' : current === 'all' ? 'one' : 'off';
      set({ repeatMode: nextMode });
    }
  };
});
