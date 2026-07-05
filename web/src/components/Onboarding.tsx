import { useState } from 'react';
import './Onboarding.css';

const ONBOARDING_KEY = 'lynx-onboarding-completed';

const slides = [
  {
    icon: (
      <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M9 18V5l12-2v13" />
        <circle cx="6" cy="18" r="3" />
        <circle cx="18" cy="16" r="3" />
      </svg>
    ),
    title: 'Welcome to LynxMusic',
    description: 'Your high-fidelity music streaming companion. Discover, stream, and enjoy millions of songs from the JioSaavn catalog.',
  },
  {
    icon: (
      <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3Z" />
        <path d="M19 unplug-2" />
        <line x1="12" y1="19" x2="12" y2="22" />
      </svg>
    ),
    title: 'Personalized For You',
    description: 'The more you listen, the better it gets. Your home feed adapts with recommendations based on your top artists.',
  },
  {
    icon: (
      <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
        <polyline points="7 10 12 15 17 10" />
        <line x1="12" y1="15" x2="12" y2="3" />
      </svg>
    ),
    title: 'Import & Organize',
    description: 'Create custom playlists, import from YouTube, and save your favorites. Your library stays organized across sessions.',
  },
  {
    icon: (
      <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M9 19c-5 0-8-3.5-8-7s3.5-7 8-7" />
        <path d="M15 5c5 0 8 3.5 8 7s-3.5 7-8 7" />
        <line x1="12" y1="2" x2="12" y2="22" />
      </svg>
    ),
    title: 'Synced Lyrics',
    description: 'Sing along with perfectly synced lyrics that scroll in real-time as the song plays.',
  },
];

export function hasSeenOnboarding(): boolean {
  try {
    return localStorage.getItem(ONBOARDING_KEY) === 'true';
  } catch {
    return false;
  }
}

export function markOnboardingComplete() {
  try {
    localStorage.setItem(ONBOARDING_KEY, 'true');
  } catch {
    // ignore
  }
}

export function resetOnboarding() {
  try {
    localStorage.removeItem(ONBOARDING_KEY);
  } catch {
    // ignore
  }
}

function Onboarding({ onComplete }: { onComplete: () => void }) {
  const [current, setCurrent] = useState(0);

  const handleNext = () => {
    if (current < slides.length - 1) {
      setCurrent((prev) => prev + 1);
    } else {
      markOnboardingComplete();
      onComplete();
    }
  };

  const handleSkip = () => {
    markOnboardingComplete();
    onComplete();
  };

  const slide = slides[current];

  return (
    <div className="onboarding-overlay">
      <div className="onboarding-container">
        <div className="onboarding-dots">
          {slides.map((_, i) => (
            <div
              key={i}
              className={`onboarding-dot ${i === current ? 'active' : ''}`}
            />
          ))}
        </div>

        <div className="onboarding-content" key={current}>
          <div className="onboarding-icon">{slide.icon}</div>
          <h2 className="onboarding-title">{slide.title}</h2>
          <p className="onboarding-description">{slide.description}</p>
        </div>

        <div className="onboarding-actions">
          <button className="onboarding-skip" onClick={handleSkip}>
            Skip
          </button>
          <button className="onboarding-next" onClick={handleNext}>
            {current === slides.length - 1 ? 'Get Started' : 'Next'}
          </button>
        </div>
      </div>
    </div>
  );
}

export default Onboarding;
