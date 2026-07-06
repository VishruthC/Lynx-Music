import { useState, useEffect } from 'react';
import { Search as SearchIcon, X, Play } from 'lucide-react';
import { searchSongs, searchAlbums, searchArtists } from '../api/saavn';
import type { SongDto, GenericDto } from '../types';
import { usePlayerStore } from '../store/usePlayerStore';
import type { PlaylistSong } from '../store/usePlayerStore';
import { Link } from 'react-router-dom';
import './Search.css';

// Predefined genre data for browse section
const BROWSE_GENRES = [
  { name: 'Pop', gradient: 'linear-gradient(135deg, #ec4899, #f43f5e)' },
  { name: 'Rock', gradient: 'linear-gradient(135deg, #1e293b, #0f172a)' },
  { name: 'Hip-Hop', gradient: 'linear-gradient(135deg, #8b5cf6, #d946ef)' },
  { name: 'Electronic', gradient: 'linear-gradient(135deg, #3b82f6, #06b6d4)' },
  { name: 'Chill', gradient: 'linear-gradient(135deg, #10b981, #059669)' },
  { name: 'Focus', gradient: 'linear-gradient(135deg, #6366f1, #4f46e5)' },
  { name: 'Workout', gradient: 'linear-gradient(135deg, #f59e0b, #d97706)' },
  { name: 'Party', gradient: 'linear-gradient(135deg, #ef4444, #b91c1c)' },
  { name: 'Jazz', gradient: 'linear-gradient(135deg, #b45309, #78350f)' },
  { name: 'Classical', gradient: 'linear-gradient(135deg, #14b8a6, #0d9488)' },
  { name: 'Romance', gradient: 'linear-gradient(135deg, #f43f5e, #be123c)' },
  { name: 'Devotional', gradient: 'linear-gradient(135deg, #84cc16, #4d7c0f)' }
];

const Search = () => {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<{
    songs: SongDto[];
    albums: GenericDto[];
    artists: GenericDto[];
  }>({ songs: [], albums: [], artists: [] });
  const [loading, setLoading] = useState(false);

  const { playSong, setQueue } = usePlayerStore();

  useEffect(() => {
    if (query.trim().length < 2) {
      setResults({ songs: [], albums: [], artists: [] });
      return;
    }

    const delayDebounceFn = setTimeout(async () => {
      setLoading(true);
      try {
        const [songsRes, albumsRes, artistsRes] = await Promise.all([
          searchSongs(query),
          searchAlbums(query),
          searchArtists(query)
        ]);

        setResults({
          songs: songsRes.data.results || [],
          albums: albumsRes.data.results || [],
          artists: artistsRes.data.results || []
        });
      } catch (error) {
        console.error('Search error:', error);
      } finally {
        setLoading(false);
      }
    }, 500);

    return () => clearTimeout(delayDebounceFn);
  }, [query]);

  const handlePlaySong = (song: SongDto) => {
    const playlistSong: PlaylistSong = {
      id: song.id,
      title: song.name,
      artist: song.artists.primary[0]?.name || 'Unknown',
      artworkUrl: song.image[song.image.length - 1]?.url || '',
      streamUrl: song.downloadUrl[song.downloadUrl.length - 1]?.url || ''
    };
    playSong(playlistSong);
  };

  const handlePlayAllSongs = () => {
    if (results.songs.length === 0) return;
    const playlistSongs: PlaylistSong[] = results.songs.map(song => ({
      id: song.id,
      title: song.name,
      artist: song.artists.primary[0]?.name || 'Unknown',
      artworkUrl: song.image[song.image.length - 1]?.url || '',
      streamUrl: song.downloadUrl[song.downloadUrl.length - 1]?.url || ''
    }));
    setQueue(playlistSongs, 0);
  };

  // Extract top result (either first artist or first song)
  const topResult = results.artists[0] || results.songs[0];

  return (
    <div className="search-page">
      <div className="search-header">
        <div className="search-input-wrapper">
          <SearchIcon size={20} className="search-icon" />
          <input 
            type="text" 
            placeholder="What do you want to listen to?" 
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            autoFocus
          />
          {query && <X size={20} className="clear-icon" onClick={() => setQuery('')} />}
        </div>
      </div>

      {loading && (
        <div className="loading-container">
          <div className="loading-spinner" />
          <span>Searching catalog...</span>
        </div>
      )}

      {/* Default Browse Section when search is empty */}
      {!query && !loading && (
        <div className="browse-genres-section">
          <h2>Browse All</h2>
          <div className="genres-grid">
            {BROWSE_GENRES.map((genre) => (
              <div 
                key={genre.name} 
                className="genre-card" 
                style={{ background: genre.gradient }}
                onClick={() => setQuery(genre.name)}
              >
                <span>{genre.name}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Results View */}
      {query && !loading && (
        <div className="search-results">
          {results.songs.length === 0 && results.albums.length === 0 && results.artists.length === 0 ? (
            <div className="no-results">
              <h3>No results found for "{query}"</h3>
              <p>Please check your spelling or search for another term.</p>
            </div>
          ) : (
            <>
              {/* Top Split Section: Featured Result & Top Songs */}
              <div className="top-split-section">
                {/* Left: Top Result */}
                {topResult && (
                  <div className="top-result-block">
                    <h3>Top Result</h3>
                    <div className="top-result-card">
                      <img 
                        src={topResult.image[topResult.image.length - 1]?.url || topResult.image[0]?.url || '/placeholder_album_art.jpg'} 
                        alt={topResult.name || topResult.title}
                        className={topResult.type === 'artist' ? 'artist-circle' : ''}
                      />
                      <span className="result-type-badge">{topResult.type}</span>
                      <h2>{topResult.name || topResult.title}</h2>
                      
                      {topResult.type === 'song' ? (
                        <p>{(topResult as any).artists?.primary[0]?.name}</p>
                      ) : (
                        <p>Popular artist</p>
                      )}

                      {/* Hover play action */}
                      {topResult.type === 'song' && (
                        <button 
                          className="top-result-play-btn"
                          onClick={() => handlePlaySong(topResult as any)}
                        >
                          <Play size={24} fill="black" color="black" />
                        </button>
                      )}
                    </div>
                  </div>
                )}

                {/* Right: Top Songs List */}
                {results.songs.length > 0 && (
                  <div className="top-songs-block">
                    <div className="block-header">
                      <h3>Songs</h3>
                      <button className="play-all-link-btn" onClick={handlePlayAllSongs}>Play All</button>
                    </div>
                    
                    <div className="songs-list">
                      {results.songs.slice(0, 4).map((song) => (
                        <div key={song.id} className="song-row" onClick={() => handlePlaySong(song)}>
                          <div className="song-row-art">
                            <img src={song.image[0]?.url} alt={song.name} />
                            <div className="row-hover-play">
                              <Play size={12} fill="white" color="white" />
                            </div>
                          </div>
                          <div className="song-row-meta">
                            <span className="song-title">{song.name}</span>
                            <span className="song-artist">{song.artists.primary[0]?.name}</span>
                          </div>
                          <span className="song-duration">
                            {Math.floor(song.duration / 60)}:{(song.duration % 60).toString().padStart(2, '0')}
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>

              {/* Albums Carousel Section */}
              {results.albums.length > 0 && (
                <div className="search-section-block">
                  <h3>Albums</h3>
                  <div className="albums-horizontal-scroll">
                    {results.albums.map((album) => (
                      <Link 
                        key={album.id} 
                        to={`/album/${album.id}`} 
                        className="album-card"
                      >
                        <div className="album-art-container">
                          <img 
                            src={album.image[album.image.length - 1]?.url || album.image[0]?.url} 
                            alt={album.title || album.name} 
                          />
                          <div className="album-play-overlay">
                            <div className="play-circle">
                              <Play size={18} fill="black" color="black" />
                            </div>
                          </div>
                        </div>
                        <div className="album-info">
                          <h4>{album.title || album.name}</h4>
                          <p>Album</p>
                        </div>
                      </Link>
                    ))}
                  </div>
                </div>
              )}

              {/* Artists Section */}
              {results.artists.length > 0 && (
                <div className="search-section-block">
                  <h3>Artists</h3>
                  <div className="artists-horizontal-scroll">
                    {results.artists.map((artist) => (
                      <div 
                        key={artist.id} 
                        className="artist-card-item"
                        onClick={() => setQuery(artist.name || artist.title || '')}
                      >
                        <div className="artist-avatar-container">
                          <img 
                            src={artist.image[artist.image.length - 1]?.url || artist.image[0]?.url} 
                            alt={artist.name || artist.title} 
                          />
                        </div>
                        <div className="artist-info">
                          <h4>{artist.name || artist.title}</h4>
                          <p>Artist</p>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      )}
    </div>
  );
};

export default Search;
