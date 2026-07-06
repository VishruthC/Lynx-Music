import React, { useState } from 'react';
import { Play, Pause, Heart, MoreHorizontal, Rewind, FastForward, Shuffle, Repeat, Repeat1 } from 'lucide-react';
import { usePlayerStore } from '../store/usePlayerStore';
import { usePlaylistStore } from '../store/usePlaylistStore';
import './RightPlayer.css';

const RightPlayer = () => {
  const {
    currentSong,
    isPlaying,
    togglePlay,
    next,
    previous,
    currentTime,
    duration,
    seek,
    isShuffle,
    repeatMode,
    toggleShuffle,
    toggleRepeat
  } = usePlayerStore();

  const likedSongs = usePlaylistStore(state => state.likedSongs);
  const toggleLikeSong = usePlaylistStore(state => state.toggleLikeSong);
  const isLiked = currentSong ? likedSongs.some(s => s.id === currentSong.id) : false;
  const [showMenu, setShowMenu] = useState(false);

  const formatTime = (time: number) => {
    if (isNaN(time) || !time) return '0:00';
    const mins = Math.floor(time / 60);
    const secs = Math.floor(time % 60);
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const formatRemainingTime = (current: number, total: number) => {
    if (isNaN(total) || !total) return '-0:00';
    const remaining = total - current;
    if (remaining <= 0) return '-0:00';
    const mins = Math.floor(remaining / 60);
    const secs = Math.floor(remaining % 60);
    return `-${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const handleSeek = (e: React.ChangeEvent<HTMLInputElement>) => {
    seek(Number(e.target.value));
  };

  // Use currently playing artwork or generated placeholder
  const artwork = currentSong?.artworkUrl || '/placeholder_album_art.jpg';
  const title = currentSong?.title || 'FLOWERS';
  const artist = currentSong?.artist || 'Miley Cyrus';

  return (
    <div className="right-player-container">
      <div 
        className="right-player-card"
        style={{ backgroundImage: `url(${artwork})` }}
      >
        {/* Absolute top actions */}
        <div className="top-actions">
          <button 
            className={`action-btn glass-btn ${isLiked ? 'liked' : ''}`}
            onClick={() => currentSong && toggleLikeSong(currentSong)}
            aria-label="Like track"
          >
            <Heart size={18} fill={isLiked ? '#ef4444' : 'none'} color={isLiked ? '#ef4444' : 'currentColor'} />
          </button>
          
          <div className="more-options-container">
            <button 
              className="action-btn glass-btn" 
              onClick={() => setShowMenu(!showMenu)}
              aria-label="More options"
            >
              <MoreHorizontal size={18} />
            </button>
            {showMenu && (
              <>
                <div className="dropdown-backdrop" onClick={() => setShowMenu(false)} />
                <div className="glass-dropdown">
                  <button className="dropdown-item" onClick={() => setShowMenu(false)}>Add to Playlist</button>
                  <button className="dropdown-item" onClick={() => setShowMenu(false)}>Share</button>
                  <button className="dropdown-item" onClick={() => setShowMenu(false)}>Add to Queue</button>
                  <button className="dropdown-item" onClick={() => setShowMenu(false)}>Go to Artist</button>
                  <button className="dropdown-item" onClick={() => setShowMenu(false)}>Song Credits</button>
                  <button className="dropdown-item" onClick={() => setShowMenu(false)}>Go to Album</button>
                </div>
              </>
            )}
          </div>
        </div>

        {/* Fading blur overlay - restricted to bottom section and masked to reduce upward */}
        <div className="blur-background" />

        {/* Bottom content and playback controls */}
        <div className="bottom-section">
          <div className="track-info">
            <h2 className="track-title">{title}</h2>
            <div className="artist-and-stats">
              <span className="track-artist">{artist}</span>
            </div>
          </div>

          <div className="progress-section">
            <input
              type="range"
              className="progress-slider"
              min={0}
              max={duration || 0}
              value={currentTime || 0}
              onChange={handleSeek}
              style={{ '--progress': `${((currentTime || 0) / (duration || 1)) * 100}%` } as React.CSSProperties}
            />
            <div className="progress-time">
              <span>{formatTime(currentTime)}</span>
              <span>{formatRemainingTime(currentTime, duration)}</span>
            </div>
          </div>

          <div className="playback-controls">
            <button 
              className={`control-btn shuffle-btn ${isShuffle ? 'active' : ''}`}
              onClick={toggleShuffle}
              aria-label="Shuffle"
            >
              <Shuffle size={18} />
              {isShuffle && <span className="dot" />}
            </button>

            <button 
              className="control-btn prev-btn" 
              onClick={previous}
              aria-label="Previous"
              disabled={!currentSong}
            >
              <Rewind size={26} fill="currentColor" color="currentColor" />
            </button>

            <button 
              className="play-pause-btn" 
              onClick={togglePlay}
              aria-label={isPlaying ? 'Pause' : 'Play'}
              disabled={!currentSong}
            >
              {isPlaying ? (
                <Pause size={38} fill="currentColor" color="currentColor" />
              ) : (
                <Play size={38} fill="currentColor" color="currentColor" style={{ transform: 'translateX(2px)' }} />
              )}
            </button>

            <button 
              className="control-btn next-btn" 
              onClick={next}
              aria-label="Next"
              disabled={!currentSong}
            >
              <FastForward size={26} fill="currentColor" color="currentColor" />
            </button>

            <button 
              className={`control-btn repeat-btn ${repeatMode !== 'off' ? 'active' : ''}`}
              onClick={toggleRepeat}
              aria-label="Repeat"
            >
              {repeatMode === 'one' ? <Repeat1 size={18} /> : <Repeat size={18} />}
              {repeatMode !== 'off' && <span className="dot" />}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RightPlayer;
