import { useState, useEffect } from 'react';
import { Search as SearchIcon, X } from 'lucide-react';
import { searchSongs, searchAlbums, searchArtists } from '../api/saavn';
import type { SongDto, GenericDto } from '../types';
import { usePlayerStore } from '../store/usePlayerStore';
import type { PlaylistSong } from '../store/usePlayerStore';
import './Search.css';

const Search = () => {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<{
    songs: SongDto[];
    albums: GenericDto[];
    artists: GenericDto[];
  }>({ songs: [], albums: [], artists: [] });
  const [loading, setLoading] = useState(false);

  const { playSong } = usePlayerStore();

  useEffect(() => {
    if (query.length < 2) {
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

  return (
    <div className="search-page">
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

      {loading && <div className="loading">Searching...</div>}

      <div className="search-results">
        {results.songs.length > 0 && (
          <div className="result-section">
            <h3>Songs</h3>
            <div className="songs-list">
              {results.songs.map((song) => (
                <div key={song.id} className="song-row" onClick={() => handlePlaySong(song)}>
                  <img src={song.image[0]?.url} alt={song.name} />
                  <div className="col-title">{song.name}</div>
                  <div className="col-artist">{song.artists.primary[0]?.name}</div>
                  <div className="song-duration">
                    {Math.floor(song.duration / 60)}:{(song.duration % 60).toString().padStart(2, '0')}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Add Albums and Artists sections later if needed */}
      </div>
    </div>
  );
};

export default Search;
