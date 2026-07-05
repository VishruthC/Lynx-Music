import { Play, Pause, SkipBack, SkipForward, Repeat, Repeat1, Shuffle, Volume2, Maximize2 } from 'lucide-react';
import { usePlayerStore } from '../store/usePlayerStore';
import './PlayerBar.css';

const PlayerBar = () => {
  const { 
    currentSong, 
    isPlaying, 
    togglePlay, 
    next, 
    previous, 
    currentTime, 
    duration,
    seek,
    volume,
    setVolume,
    toggleFullscreen,
    isShuffle,
    repeatMode,
    toggleShuffle,
    toggleRepeat
  } = usePlayerStore();

  const formatTime = (time: number) => {
    const mins = Math.floor(time / 60);
    const secs = Math.floor(time % 60);
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const handleSeek = (e: React.ChangeEvent<HTMLInputElement>) => {
    seek(Number(e.target.value));
  };

  const handleVolume = (e: React.ChangeEvent<HTMLInputElement>) => {
    setVolume(Number(e.target.value));
  };

  if (!currentSong) return <div className="player-bar empty" />;

  return (
    <div className="player-bar">
      <div className="song-info">
        <img src={currentSong.artworkUrl} alt={currentSong.title} className="now-playing-img" />
        <div className="text-info">
          <div className="title">{currentSong.title}</div>
          <div className="artist">{currentSong.artist}</div>
        </div>
      </div>

      <div className="controls-section">
        <div className="main-controls">
          <button 
            className={`icon-btn mode-btn ${isShuffle ? 'active' : ''}`} 
            onClick={toggleShuffle}
          >
            <Shuffle size={18} />
            {isShuffle && <div className="active-dot" />}
          </button>
          <button className="icon-btn" onClick={previous}><SkipBack size={24} fill="currentColor" /></button>
          <button className="play-btn" onClick={togglePlay}>
            {isPlaying ? <Pause size={24} fill="currentColor" /> : <Play size={24} fill="currentColor" />}
          </button>
          <button className="icon-btn" onClick={next}><SkipForward size={24} fill="currentColor" /></button>
          <button 
            className={`icon-btn mode-btn ${repeatMode !== 'off' ? 'active' : ''}`}
            onClick={toggleRepeat}
          >
            {repeatMode === 'one' ? <Repeat1 size={18} /> : <Repeat size={18} />}
            {repeatMode !== 'off' && <div className="active-dot" />}
          </button>
        </div>
        
        <div className="progress-bar-container">
          <span className="time">{formatTime(currentTime)}</span>
          <input 
            type="range" 
            className="progress-slider"
            min={0}
            max={duration || 0}
            value={currentTime}
            onChange={handleSeek}
            style={{ '--progress': `${(currentTime / (duration || 1)) * 100}%` } as React.CSSProperties}
          />
          <span className="time">{formatTime(duration)}</span>
        </div>
      </div>

      <div className="volume-section">
        <Volume2 size={20} />
        <input 
          type="range" 
          className="volume-slider"
          min={0}
          max={1}
          step={0.01}
          value={volume}
          onChange={handleVolume}
          style={{ '--progress': `${volume * 100}%` } as React.CSSProperties}
        />
        <Maximize2 size={18} className="maximize-icon" onClick={toggleFullscreen} />
      </div>
    </div>
  );
};

export default PlayerBar;
