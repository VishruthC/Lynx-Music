import { useState } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import Search from './pages/Search';
import PlaylistDetails from './pages/PlaylistDetails';
import Library from './pages/Library';
import Sidebar from './components/Sidebar';
import RightPlayer from './components/RightPlayer';
import FullscreenPlayer from './components/FullscreenPlayer';
import Onboarding, { hasSeenOnboarding } from './components/Onboarding';
import './App.css';

function App() {
  const [showOnboarding, setShowOnboarding] = useState(!hasSeenOnboarding());

  if (showOnboarding) {
    return <Onboarding onComplete={() => setShowOnboarding(false)} />;
  }

  return (
    <Router>
      <div className="app-container">
        <Sidebar />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/search" element={<Search />} />
            <Route path="/library" element={<Library />} />
            <Route path="/playlist/:id" element={<PlaylistDetails />} />
            <Route path="/album/:id" element={<PlaylistDetails isAlbum={true} />} />
          </Routes>
        </main>
        <RightPlayer />
        <FullscreenPlayer />
      </div>
    </Router>
  );
}

export default App;
