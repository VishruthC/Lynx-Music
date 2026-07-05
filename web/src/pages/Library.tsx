import { Link } from 'react-router-dom';
import { usePlaylistStore } from '../store/usePlaylistStore';
import { Heart, Music } from 'lucide-react';
import './Library.css';

function Library() {
  const customPlaylists = usePlaylistStore((s) => s.customPlaylists);

  return (
    <div className="library-page">
      <h2>Your Library</h2>

      <div className="library-sections">
        <Link to="/collection/tracks" className="library-tile">
          <div className="library-tile-icon liked">
            <Heart size={28} fill="currentColor" />
          </div>
          <div className="library-tile-info">
            <h3>Liked Songs</h3>
            <p>Your favorite tracks</p>
          </div>
        </Link>

        <div className="library-divider" />

        <h3 className="library-subheading">Playlists</h3>
        {customPlaylists.length === 0 ? (
          <p className="library-empty">No custom playlists yet. Create one from the sidebar.</p>
        ) : (
          <div className="library-playlists">
            {customPlaylists.map((playlist) => (
              <Link
                key={playlist.id}
                to={`/playlist/${playlist.id}`}
                className="library-playlist-item"
              >
                <div className="library-playlist-art">
                  <Music size={24} />
                </div>
                <div className="library-playlist-info">
                  <h4>{playlist.name}</h4>
                  <p>{playlist.songs.length} songs</p>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default Library;
