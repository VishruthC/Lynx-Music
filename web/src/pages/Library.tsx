import React, { useState } from 'react';
import { usePlaylistStore } from '../store/usePlaylistStore';
import { usePlayerStore } from '../store/usePlayerStore';
import { Heart, Music, Plus, Play, Trash2, Disc } from 'lucide-react';
import './Library.css';

function Library() {
  const { customPlaylists, likedSongs, addPlaylist, removePlaylist } = usePlaylistStore();
  const { setQueue, currentSong } = usePlayerStore();

  const [showCreateMenu, setShowCreateMenu] = useState(false);
  const [newPlaylistName, setNewPlaylistName] = useState('');
  const [newPlaylistDescription, setNewPlaylistDescription] = useState('');
  const [selectedPlaylistId, setSelectedPlaylistId] = useState<string | null>(null);

  const handleCreatePlaylist = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newPlaylistName.trim()) return;

    addPlaylist({
      id: `custom_${Date.now()}`,
      name: newPlaylistName.trim(),
      imageUrl: '',
      songs: [],
      description: newPlaylistDescription.trim() || undefined
    });

    setNewPlaylistName('');
    setNewPlaylistDescription('');
    setShowCreateMenu(false);
  };

  const handlePlayLikedSong = (index: number) => {
    setQueue(likedSongs, index);
  };

  const handlePlayWholeLikedQueue = () => {
    if (likedSongs.length > 0) {
      setQueue(likedSongs, 0);
    }
  };

  // If a playlist is selected, show its detail view in the library page (or navigation)
  const selectedPlaylist = customPlaylists.find(p => p.id === selectedPlaylistId);

  return (
    <div className="library-page android-scheme">
      {/* Top Header matching Android UI */}
      <div className="library-header">
        <h1>Your Library</h1>
        <div className="header-actions">
          <div className="create-playlist-container">
            <button 
              className="header-icon-btn add-playlist-btn" 
              title="Create Playlist"
              onClick={() => setShowCreateMenu(!showCreateMenu)}
            >
              <Plus size={24} />
            </button>
            {showCreateMenu && (
              <>
                <div className="dropdown-backdrop" onClick={() => setShowCreateMenu(false)} />
                <form className="glass-create-playlist-dropdown" onSubmit={handleCreatePlaylist}>
                  <h3>New Playlist</h3>
                  <input 
                    type="text" 
                    placeholder="Playlist name"
                    value={newPlaylistName}
                    onChange={(e) => setNewPlaylistName(e.target.value)}
                    autoFocus
                    maxLength={32}
                    className="dropdown-input"
                  />
                  <textarea 
                    placeholder="Description (optional)"
                    value={newPlaylistDescription}
                    onChange={(e) => setNewPlaylistDescription(e.target.value)}
                    maxLength={120}
                    className="dropdown-textarea"
                  />
                  <div className="dropdown-form-buttons">
                    <button 
                      type="button" 
                      className="dropdown-form-btn cancel"
                      onClick={() => setShowCreateMenu(false)}
                    >
                      Cancel
                    </button>
                    <button 
                      type="submit" 
                      className="dropdown-form-btn submit"
                      disabled={!newPlaylistName.trim()}
                    >
                      Create
                    </button>
                  </div>
                </form>
              </>
            )}
          </div>
        </div>
      </div>

      <div className="library-list-container">
        {selectedPlaylistId === 'liked_songs' ? (
          /* Liked Songs Detail View */
          <div className="playlist-detail-view animate-fade-in">
            <button className="back-to-list-btn" onClick={() => setSelectedPlaylistId(null)}>
              &larr; Back to Library
            </button>
            <div className="liked-songs-play-banner">
              <div className="banner-art">
                <Heart size={48} fill="white" />
              </div>
              <div className="banner-details">
                <h2>Liked Songs</h2>
                <p>{likedSongs.length} tracks saved</p>
                {likedSongs.length > 0 && (
                  <button className="play-banner-btn" onClick={handlePlayWholeLikedQueue}>
                    <Play size={20} fill="black" color="black" />
                    <span>Play Collection</span>
                  </button>
                )}
              </div>
            </div>

            {likedSongs.length === 0 ? (
              <div className="empty-state">
                <Heart size={48} className="empty-icon liked" />
                <h3>No liked songs yet</h3>
                <p>Click the Heart icon on any song while playing to save it here.</p>
              </div>
            ) : (
              <div className="songs-list-table">
                {likedSongs.map((song, index) => {
                  const isCurrent = currentSong?.id === song.id;
                  return (
                    <div 
                      key={song.id} 
                      className={`table-row ${isCurrent ? 'current' : ''}`}
                      onClick={() => handlePlayLikedSong(index)}
                    >
                      <span className="col-num">{index + 1}</span>
                      <span className="col-title">
                        <img src={song.artworkUrl} alt={song.title} className="row-artwork" />
                        <div className="row-metadata">
                          <span className="song-title-text">{song.title}</span>
                          <span className="song-artist-text">{song.artist}</span>
                        </div>
                      </span>
                      <span className="col-actions" onClick={(e) => e.stopPropagation()}>
                        <button 
                          className="row-delete-btn"
                          onClick={() => usePlaylistStore.getState().toggleLikeSong(song)}
                        >
                          <Trash2 size={16} />
                        </button>
                      </span>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        ) : selectedPlaylist ? (
          /* Custom Playlist Detail View */
          <div className="playlist-detail-view animate-fade-in">
            <button className="back-to-list-btn" onClick={() => setSelectedPlaylistId(null)}>
              &larr; Back to Library
            </button>
            <div className="liked-songs-play-banner">
              <div className="banner-art playlist-bg">
                <Music size={48} color="white" />
              </div>
              <div className="banner-details">
                <h2>{selectedPlaylist.name}</h2>
                <p>{selectedPlaylist.songs.length} songs</p>
                {selectedPlaylist.songs.length > 0 && (
                  <button className="play-banner-btn" onClick={() => setQueue(selectedPlaylist.songs, 0)}>
                    <Play size={20} fill="black" color="black" />
                    <span>Play Playlist</span>
                  </button>
                )}
              </div>
            </div>

            {selectedPlaylist.songs.length === 0 ? (
              <div className="empty-state">
                <Music size={48} className="empty-icon" />
                <h3>No songs in this playlist</h3>
                <p>Search for songs and add them to this playlist using the song options menu.</p>
              </div>
            ) : (
              <div className="songs-list-table">
                {selectedPlaylist.songs.map((song, index) => {
                  const isCurrent = currentSong?.id === song.id;
                  return (
                    <div 
                      key={song.id} 
                      className={`table-row ${isCurrent ? 'current' : ''}`}
                      onClick={() => setQueue(selectedPlaylist.songs, index)}
                    >
                      <span className="col-num">{index + 1}</span>
                      <span className="col-title">
                        <img src={song.artworkUrl} alt={song.title} className="row-artwork" />
                        <div className="row-metadata">
                          <span className="song-title-text">{song.title}</span>
                          <span className="song-artist-text">{song.artist}</span>
                        </div>
                      </span>
                      <span className="col-actions"></span>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        ) : (
          /* Vertical Android LazyColumn list design */
          <div className="android-lazy-column animate-fade-in">
            {/* 1. Liked Songs Row */}
            <div 
              className="android-list-row"
              onClick={() => setSelectedPlaylistId('liked_songs')}
            >
              <div className="row-art-wrapper heart-gradient">
                <Heart size={28} fill="white" color="white" />
              </div>
              <div className="row-info-block">
                <h3>Liked Songs</h3>
                <p>{likedSongs.length} tracks</p>
              </div>
            </div>



            {/* 3. Custom Playlists rows */}
            {customPlaylists.map(playlist => (
              <div 
                key={playlist.id} 
                className="android-list-row"
                onClick={() => setSelectedPlaylistId(playlist.id)}
              >
                <div className="row-art-wrapper playlist-gradient">
                  <Disc size={28} color="white" />
                </div>
                <div className="row-info-block">
                  <h3>{playlist.name}</h3>
                  <p>{playlist.songs.length} songs</p>
                </div>
                <button 
                  className="row-delete-action-btn"
                  onClick={(e) => {
                    e.stopPropagation();
                    if (confirm(`Delete playlist "${playlist.name}"?`)) {
                      removePlaylist(playlist.id);
                    }
                  }}
                  title="Delete Playlist"
                >
                  <Trash2 size={18} />
                </button>
              </div>
            ))}

            {customPlaylists.length === 0 && (
              <div className="empty-playlists-callout">
                <p>No custom playlists yet. Click the Plus icon above to create one.</p>
              </div>
            )}
          </div>
        )}
      </div>


    </div>
  );
}

export default Library;
