import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getPlaylistDetails, getAlbumDetails } from '../api/saavn';
import type { CollectionData } from '../types';
import { usePlayerStore } from '../store/usePlayerStore';
import { usePlaylistStore } from '../store/usePlaylistStore';
import type { PlaylistSong } from '../store/usePlayerStore';
import { Play, Clock } from 'lucide-react';
import './PlaylistDetails.css';

const PlaylistDetails = ({ isAlbum = false }: { isAlbum?: boolean }) => {
  const { id } = useParams<{ id: string }>();
  const [collection, setCollection] = useState<CollectionData | null>(null);
  const [loading, setLoading] = useState(true);

  const { setQueue } = usePlayerStore();
  const customPlaylists = usePlaylistStore(state => state.customPlaylists);

  useEffect(() => {
    const fetchDetails = async () => {
      if (!id) return;
      
      // Check if it's a custom playlist first
      const custom = customPlaylists.find(p => p.id === id);
      if (custom) {
        setCollection({
          id: custom.id,
          name: custom.name,
          title: custom.name,
          songs: custom.songs.map(s => ({
            id: s.id,
            name: s.title,
            artists: { primary: [{ name: s.artist }] },
            image: [{ quality: '500x500', url: s.artworkUrl }],
            downloadUrl: [{ quality: '320kbps', url: s.streamUrl }],
            duration: 0, // We should probably save duration too
            year: ''
          }))
        });
        setLoading(false);
        return;
      }

      setLoading(true);
      try {
        const res = isAlbum ? await getAlbumDetails(id) : await getPlaylistDetails(id);
        setCollection(res.data);
      } catch (error) {
        console.error('Error fetching details:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchDetails();
  }, [id, isAlbum, customPlaylists]);

  const handlePlayAll = () => {
    if (!collection) return;
    const songs: PlaylistSong[] = collection.songs.map(song => ({
      id: song.id,
      title: song.name,
      artist: song.artists.primary[0]?.name || 'Unknown',
      artworkUrl: song.image[song.image.length - 1]?.url || '',
      streamUrl: song.downloadUrl[song.downloadUrl.length - 1]?.url || ''
    }));
    setQueue(songs, 0);
  };

  const handlePlaySong = (index: number) => {
    if (!collection) return;
    const songs: PlaylistSong[] = collection.songs.map(song => ({
      id: song.id,
      title: song.name,
      artist: song.artists.primary[0]?.name || 'Unknown',
      artworkUrl: song.image[song.image.length - 1]?.url || '',
      streamUrl: song.downloadUrl[song.downloadUrl.length - 1]?.url || ''
    }));
    setQueue(songs, index);
  };

  if (loading) return <div className="loading">Loading...</div>;
  if (!collection) return <div className="error">Collection not found</div>;

  const headerImg = collection.songs[0]?.image[collection.songs[0].image.length - 1]?.url;

  return (
    <div className="playlist-details">
      <div className="page-background-blur" style={{ backgroundImage: `url(${headerImg})` }} />
      
      <header className="playlist-header">
        <img src={headerImg} alt={collection.name} />
        <div className="header-info">
          <span className="type">{isAlbum ? 'ALBUM' : 'PLAYLIST'}</span>
          <h1>{collection.name || collection.title}</h1>
          <div className="meta">
            <span>{collection.songs.length} songs</span>
          </div>
        </div>
      </header>

      <div className="playlist-actions">
        <button className="play-btn-large" onClick={handlePlayAll}>
          <Play size={24} fill="black" />
        </button>
      </div>

      <div className="songs-table">
        <div className="table-header">
          <div className="col-idx">#</div>
          <div className="col-title">Title</div>
          <div className="col-artist">Artist</div>
          <div className="col-album">Album</div>
          <div className="col-duration"><Clock size={16} /></div>
        </div>
        
        <div className="songs-list">
          {collection.songs.map((song, idx) => (
            <div key={song.id} className="song-row" onClick={() => handlePlaySong(idx)}>
              <div className="col-idx">{idx + 1}</div>
              <div className="col-title">
                <img src={song.image[0]?.url} alt={song.name} />
                <div className="song-name">{song.name}</div>
              </div>
              <div className="col-artist">{song.artists.primary[0]?.name}</div>
              <div className="col-album">{collection.name || collection.title}</div>
              <div className="col-duration">
                {Math.floor(song.duration / 60)}:{(song.duration % 60).toString().padStart(2, '0')}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default PlaylistDetails;
