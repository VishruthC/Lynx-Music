import { useEffect, useMemo, useRef } from 'react';
import { ChevronDown, Play, Pause, SkipBack, SkipForward, Shuffle, Repeat, Repeat1, Volume2 } from 'lucide-react';
import { usePlayerStore } from '../store/usePlayerStore';
import { searchLyrics } from '../api/saavn';
import './FullscreenPlayer.css';

interface LyricLine {
  time: number;
  text: string;
}

const FullscreenPlayer = () => {
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
    isFullscreen,
    toggleFullscreen,
    lyrics,
    setLyrics,
    isLoadingLyrics,
    setIsLoadingLyrics,
    isShuffle,
    repeatMode,
    toggleShuffle,
    toggleRepeat
  } = usePlayerStore();

  const lyricsScrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (isFullscreen && currentSong) {
      const fetchLyricsData = async () => {
        setIsLoadingLyrics(true);
        try {
          const res = await searchLyrics(`${currentSong.title} ${currentSong.artist}`);
          if (res && res.length > 0) {
            setLyrics(res[0].syncedLyrics || res[0].plainLyrics || 'Lyrics not found');
          } else {
            setLyrics('Lyrics not found');
          }
        } catch (error) {
          console.error('Error fetching lyrics:', error);
          setLyrics('Error loading lyrics');
        } finally {
          setIsLoadingLyrics(false);
        }
      };
      fetchLyricsData();
    }
  }, [isFullscreen, currentSong, setLyrics, setIsLoadingLyrics]);

  const parsedLyrics = useMemo(() => {
    if (!lyrics || lyrics === 'Lyrics not found' || lyrics === 'Error loading lyrics') return [];
    
    // Check if synced
    if (!lyrics.includes('[')) return lyrics.split('\n').map(line => ({ time: -1, text: line }));

    const lines = lyrics.split('\n');
    const result: LyricLine[] = [];
    const timeRegex = /\[(\d{2}):(\d{2})\.(\d{2,3})\]/;

    lines.forEach(line => {
      const match = timeRegex.exec(line);
      if (match) {
        const mins = parseInt(match[1]);
        const secs = parseInt(match[2]);
        const ms = parseInt(match[3]);
        const time = mins * 60 + secs + (ms / 100);
        const text = line.replace(timeRegex, '').trim();
        if (text) result.push({ time, text });
      }
    });
    return result;
  }, [lyrics]);

  const activeIndex = useMemo(() => {
    if (parsedLyrics.length === 0 || parsedLyrics[0].time === -1) return -1;
    let index = -1;
    for (let i = 0; i < parsedLyrics.length; i++) {
      if (currentTime >= parsedLyrics[i].time) {
        index = i;
      } else {
        break;
      }
    }
    return index;
  }, [parsedLyrics, currentTime]);

  useEffect(() => {
    if (lyricsScrollRef.current && activeIndex !== -1) {
      const activeElement = lyricsScrollRef.current.children[activeIndex] as HTMLElement;
      if (activeElement) {
        lyricsScrollRef.current.scrollTo({
          top: activeElement.offsetTop - lyricsScrollRef.current.offsetHeight / 2 + activeElement.offsetHeight / 2,
          behavior: 'smooth'
        });
      }
    }
  }, [activeIndex]);

  if (!isFullscreen || !currentSong) return null;

  const formatTime = (time: number) => {
    const mins = Math.floor(time / 60);
    const secs = Math.floor(time % 60);
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  return (
    <div className="fullscreen-player">
      <div className="background-blur" style={{ backgroundImage: `url(${currentSong.artworkUrl})` }} />
      
      <header className="fs-header">
        <button className="close-btn" onClick={toggleFullscreen}>
          <ChevronDown size={32} />
        </button>
        <div className="header-text">
          <span>PLAYING FROM PLAYLIST</span>
          <strong>Now Playing</strong>
        </div>
        <div style={{ width: 32 }} />
      </header>

      <main className="fs-main-content">
        <div className="player-side">
          <div className="art-wrapper">
            <img src={currentSong.artworkUrl} alt={currentSong.title} />
          </div>
          
          <div className="song-meta">
            <div className="text-details">
              <h2>{currentSong.title}</h2>
              <p>{currentSong.artist}</p>
            </div>
          </div>

          <div className="controls-wrapper">
            <div className="progress-section">
              <input 
                type="range" 
                className="fs-progress-slider"
                min={0}
                max={duration || 0}
                value={currentTime}
                onChange={(e) => seek(Number(e.target.value))}
                style={{ '--progress': `${(currentTime / (duration || 1)) * 100}%` } as React.CSSProperties}
              />
              <div className="time-labels">
                <span>{formatTime(currentTime)}</span>
                <span>{formatTime(duration)}</span>
              </div>
            </div>

            <div className="playback-controls">
              <button 
                className={`fs-icon-btn mode-btn ${isShuffle ? 'active' : ''}`}
                onClick={toggleShuffle}
              >
                <Shuffle size={24} />
                {isShuffle && <div className="active-dot" />}
              </button>
              <button className="fs-icon-btn main" onClick={previous}><SkipBack size={36} fill="currentColor" /></button>
              <button className="play-btn" style={{ transform: 'scale(1.5)' }} onClick={togglePlay}>
                {isPlaying ? <Pause size={24} fill="currentColor" /> : <Play size={24} fill="currentColor" />}
              </button>
              <button className="fs-icon-btn main" onClick={next}><SkipForward size={36} fill="currentColor" /></button>
              <button 
                className={`fs-icon-btn mode-btn ${repeatMode !== 'off' ? 'active' : ''}`}
                onClick={toggleRepeat}
              >
                {repeatMode === 'one' ? <Repeat1 size={24} /> : <Repeat size={24} />}
                {repeatMode !== 'off' && <div className="active-dot" />}
              </button>
            </div>

            <div className="bottom-actions">
               <div className="fs-volume-section">
                  <Volume2 size={20} />
                  <input 
                    type="range" 
                    className="fs-volume-slider"
                    min={0}
                    max={1}
                    step={0.01}
                    value={volume}
                    onChange={(e) => setVolume(Number(e.target.value))}
                    style={{ '--progress': `${volume * 100}%` } as React.CSSProperties}
                  />
               </div>
            </div>
          </div>
        </div>

        <div className="lyrics-side">
          {isLoadingLyrics ? (
            <div className="lyrics-loading">Loading lyrics...</div>
          ) : parsedLyrics.length > 0 ? (
            <div className="lyrics-container" ref={lyricsScrollRef}>
              {parsedLyrics.map((line, idx) => (
                <div 
                  key={idx} 
                  className={`lyric-line ${idx === activeIndex ? 'active' : ''} ${line.time === -1 ? 'plain' : ''}`}
                  onClick={() => line.time !== -1 && seek(line.time)}
                >
                  {line.text}
                </div>
              ))}
            </div>
          ) : (
            <div className="lyrics-empty">No lyrics found</div>
          )}
        </div>
      </main>
    </div>
  );
};

export default FullscreenPlayer;
