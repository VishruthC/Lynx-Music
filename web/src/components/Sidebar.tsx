import { Link, useLocation } from 'react-router-dom';
import { Home, Search, Library, PlusSquare, Heart } from 'lucide-react';
import { usePlaylistStore } from '../store/usePlaylistStore';
import './Sidebar.css';

const Sidebar = () => {
  const location = useLocation();
  const customPlaylists = usePlaylistStore(state => state.customPlaylists);

  return (
    <div className="sidebar">
      <div className="logo">
        <h1 className="brand">LynxMusic</h1>
      </div>
      
      <nav className="nav-links">
        <Link to="/" className={location.pathname === '/' ? 'active' : ''}>
          <Home size={24} />
          <span>Home</span>
        </Link>
        <Link to="/search" className={location.pathname === '/search' ? 'active' : ''}>
          <Search size={24} />
          <span>Search</span>
        </Link>
        <Link to="/library" className={location.pathname === '/library' ? 'active' : ''}>
          <Library size={24} />
          <span>Library</span>
        </Link>
      </nav>

      <div className="playlist-actions">
        <button className="action-btn">
          <PlusSquare size={24} />
          <span>Create Playlist</span>
        </button>
        <button className="action-btn">
          <Heart size={24} fill={location.pathname === '/collection/tracks' ? 'currentColor' : 'none'} />
          <span>Liked Songs</span>
        </button>
      </div>

      <div className="divider" />
      
      <div className="playlists-list">
        {customPlaylists.map(playlist => (
          <Link 
            key={playlist.id} 
            to={`/playlist/${playlist.id}`} 
            className={`playlist-link ${location.pathname === `/playlist/${playlist.id}` ? 'active' : ''}`}
          >
            {playlist.name}
          </Link>
        ))}
      </div>
    </div>
  );
};

export default Sidebar;
