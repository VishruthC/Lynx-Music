import { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  searchPlaylists,
  searchAlbums,
  searchArtists,
  getTrendingPlaylists,
  getNewReleases,
  getTopCharts,
  getMadeForYou,
  getDailyMix,
  getWorkoutMusic,
  getFocusMusic,
  getPopularArtists
} from '../api/saavn';
import type { GenericDto } from '../types';
import { useUserActivityStore, type ActivityEntry } from '../store/useUserActivityStore';
import './Home.css';

// --- Shared Types ---
interface HomeItem {
  id: string;
  title: string;
  subtitle: string;
  image: string;
  type: string;
}

// --- Card Sizes per Android ---
const SMALL_W = 120;
const MEDIUM_W = 160;
const LARGE_W = 200;

// --- Mood Chips ---
const MOODS = ['Party', 'Workout', 'Focus', 'Relax', 'Commute', 'Late Night'];

// --- Helpers ---
function getGreeting(): string {
  const hour = new Date().getHours();
  if (hour < 6) return 'Good evening';
  if (hour < 12) return 'Good morning';
  if (hour < 18) return 'Good afternoon';
  return 'Good evening';
}

function mapToHomeItem(item: GenericDto): HomeItem {
  const image = Array.isArray(item.image)
    ? item.image[item.image.length - 1]?.url || ''
    : '';
  return {
    id: item.id,
    title: item.title || item.name || 'Untitled',
    subtitle: item.description || '',
    image,
    type: item.type || 'playlist',
  };
}

// --- Components ---

function SectionTitle({ title }: { title: string }) {
  return <h3 className="section-title">{title}</h3>;
}

function HorizontalScroll({
  children,
  gap = 14,
}: {
  children: React.ReactNode;
  gap?: number;
}) {
  return (
    <div
      className="horizontal-scroll"
      style={{ gap: `${gap}px` }}
    >
      {children}
    </div>
  );
}

// ---------- Hero Carousel ----------
function HeroCarousel({ items }: { items: HomeItem[] }) {
  const [current, setCurrent] = useState(0);
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (items.length <= 1) return;
    const id = setInterval(() => {
      setCurrent((prev) => {
        const next = (prev + 1) % items.length;
        if (scrollRef.current) {
          const child = scrollRef.current.children[next] as HTMLElement;
          if (child) {
            scrollRef.current.scrollTo({
              left: child.offsetLeft - 20,
              behavior: 'smooth',
            });
          }
        }
        return next;
      });
    }, 4000);
    return () => clearInterval(id);
  }, [items.length]);

  return (
    <div className="hero-carousel-wrapper">
      <HorizontalScroll>
        {items.map((item) => (
          <HeroCard key={item.id} item={item} />
        ))}
      </HorizontalScroll>
      {items.length > 1 && (
        <div className="carousel-dots">
          {items.map((_, i) => (
            <div
              key={i}
              className={`carousel-dot ${i === current ? 'active' : ''}`}
            />
          ))}
        </div>
      )}
    </div>
  );
}

function HeroCard({ item }: { item: HomeItem }) {
  const navigate = useNavigate();
  return (
    <div
      className="hero-card-wrapper"
      onClick={() =>
        navigate(
          item.type === 'album' ? `/album/${item.id}` : `/playlist/${item.id}`
        )
      }
    >
      <div className="hero-card">
        <img src={item.image} alt={item.title} />
        <div className="hero-overlay" />
        <div className="hero-text">
          <span className="hero-label">Trending now</span>
          <h4 className="hero-title">{item.title}</h4>
          <p className="hero-subtitle">{item.subtitle}</p>
        </div>
      </div>
    </div>
  );
}

// ---------- Recently Played ----------
function RecentlyPlayedSection({ items }: { items: HomeItem[] }) {
  return (
    <section className="home-section">
      <SectionTitle title="Jump back in" />
      <HorizontalScroll>
        {items.map((item) => (
          <RecentlyPlayedCard key={item.id} item={item} />
        ))}
      </HorizontalScroll>
    </section>
  );
}

function RecentlyPlayedCard({ item }: { item: HomeItem }) {
  const navigate = useNavigate();
  return (
    <div
      className="recent-card recent-card-size"
      onClick={() =>
        navigate(
          item.type === 'album' ? `/album/${item.id}` : `/playlist/${item.id}`
        )
      }
    >
      <div className="recent-img-wrapper">
        <img src={item.image} alt={item.title} />
        <div className="recent-play-overlay">
          <svg viewBox="0 0 24 24" width="24" height="24">
            <path d="M7 6v12l10-6z" fill="white" />
          </svg>
        </div>
      </div>
      <p className="recent-title">{item.title}</p>
      <p className="recent-subtitle">{item.subtitle}</p>
    </div>
  );
}

// ---------- Mood Chips ----------
function MoodChips() {
  return (
    <div className="mood-chips-row">
      {MOODS.map((mood) => (
        <button key={mood} className="mood-chip">
          {mood}
        </button>
      ))}
    </div>
  );
}

// ---------- Standard Sections (Small / Medium / Large) ----------
function StandardSection({
  title,
  items,
  cardType = 'medium',
}: {
  title: string;
  items: HomeItem[];
  cardType?: 'small' | 'medium' | 'large';
}) {
  if (!items.length) return null;
  return (
    <section className="home-section">
      <SectionTitle title={title} />
      <HorizontalScroll>
        {items.map((item) =>
          cardType === 'small' ? (
            <SmallCard key={item.id} item={item} />
          ) : cardType === 'large' ? (
            <LargeCard key={item.id} item={item} />
          ) : (
            <MediumCard key={item.id} item={item} />
          )
        )}
      </HorizontalScroll>
    </section>
  );
}

function SmallCard({ item }: { item: HomeItem }) {
  const navigate = useNavigate();
  return (
    <div
      className="card card-small"
      onClick={() =>
        navigate(
          item.type === 'album' ? `/album/${item.id}` : `/playlist/${item.id}`
       )
      }
    >
      <div className="card-img-wrapper" style={{ width: SMALL_W }}>
        <img src={item.image} alt={item.title} />
      </div>
      <p className="card-title">{item.title}</p>
      <p className="card-subtitle">{item.subtitle}</p>
    </div>
  );
}

function MediumCard({ item }: { item: HomeItem }) {
  const navigate = useNavigate();
  return (
    <div
      className="card card-medium"
      onClick={() =>
        navigate(
          item.type === 'album' ? `/album/${item.id}` : `/playlist/${item.id}`
        )
      }
    >
      <div className="card-img-wrapper" style={{ width: MEDIUM_W }}>
        <img src={item.image} alt={item.title} />
      </div>
      <p className="card-title">{item.title}</p>
      <p className="card-subtitle">{item.subtitle}</p>
    </div>
  );
}

function LargeCard({ item }: { item: HomeItem }) {
  const navigate = useNavigate();
  return (
    <div
      className="card card-large"
      onClick={() =>
        navigate(
          item.type === 'album' ? `/album/${item.id}` : `/playlist/${item.id}`
        )
      }
    >
      <div className="card-img-wrapper" style={{ width: LARGE_W }}>
        <img src={item.image} alt={item.title} />
      </div>
      <p className="card-title">{item.title}</p>
      <p className="card-subtitle">{item.subtitle}</p>
    </div>
  );
}

// ---------- Top Charts ----------
function TopChartsSection({ items }: { items: HomeItem[] }) {
  if (!items.length) return null;
  return (
    <section className="home-section">
      <SectionTitle title="Top charts" />
      <div className="top-charts-list">
        {items.map((item, index) => (
          <TopChartItem key={item.id} index={index + 1} item={item} />
        ))}
      </div>
    </section>
  );
}

function TopChartItem({ index, item }: { index: number; item: HomeItem }) {
  const navigate = useNavigate();
  return (
    <div
      className="top-chart-row"
      onClick={() =>
        navigate(
          item.type === 'album' ? `/album/${item.id}` : `/playlist/${item.id}`
        )
      }
    >
      <span className={`chart-rank ${index <= 3 ? 'highlight' : ''}`}>
        {String(index).padStart(2, '0')}
      </span>
      <div className="chart-img-wrapper">
        <img src={item.image} alt={item.title} />
      </div>
      <div className="chart-text">
        <p className="chart-title">{item.title}</p>
        <p className="chart-subtitle">{item.subtitle}</p>
      </div>
      <div className="chart-play-btn">
        <svg viewBox="0 0 24 24" width="16" height="16">
          <path d="M7 6v12l10-6z" fill="white" />
        </svg>
      </div>
    </div>
  );
}

// ---------- Wide Cards ----------
function WideCardsSection({ title, items }: { title: string; items: HomeItem[] }) {
  if (!items.length) return null;
  return (
    <section className="home-section">
      <SectionTitle title={title} />
      <HorizontalScroll>
        {items.map((item) => (
          <WideCard key={item.id} item={item} />
        ))}
      </HorizontalScroll>
    </section>
  );
}

function WideCard({ item }: { item: HomeItem }) {
  const navigate = useNavigate();
  return (
    <div
      className="wide-card"
      onClick={() =>
        navigate(
          item.type === 'album' ? `/album/${item.id}` : `/playlist/${item.id}`
        )
      }
    >
      <div className="wide-img-wrapper">
        <img src={item.image} alt={item.title} />
      </div>
      <div className="wide-text">
        <p className="wide-title">{item.title}</p>
        <p className="wide-subtitle">{item.subtitle}</p>
      </div>
      <svg viewBox="0 0 24 24" width="24" height="24">
        <path d="M7 6v12l10-6z" fill="white" />
      </svg>
    </div>
  );
}

// ---------- Artists ----------
function ArtistsSection({ title, items }: { title: string; items: HomeItem[] }) {
  if (!items.length) return null;
  return (
    <section className="home-section">
      <SectionTitle title={title} />
      <HorizontalScroll>
        {items.map((item) => (
          <ArtistCard key={item.id} item={item} />
        ))}
      </HorizontalScroll>
    </section>
  );
}

function ArtistCard({ item }: { item: HomeItem }) {
  const navigate = useNavigate();
  return (
    <div
      className="artist-card"
      onClick={() =>
        navigate(
          item.type === 'album' ? `/album/${item.id}` : `/playlist/${item.id}`
        )
      }
    >
      <div className="artist-img-wrapper">
        <img src={item.image} alt={item.title} />
      </div>
      <p className="artist-title">{item.title}</p>
      <p className="artist-subtitle">{item.subtitle}</p>
    </div>
  );
}

// ---------- Loading / Error / Empty States ----------
function ShimmerCard() {
  return (
    <div className="shimmer-card">
      <div className="shimmer-img" />
      <div className="shimmer-line short" />
      <div className="shimmer-line" />
    </div>
  );
}

function ShimmerListItem() {
  return (
    <div className="shimmer-list-item">
      <div className="shimmer-num" />
      <div className="shimmer-img-small" />
      <div className="shimmer-text-block">
        <div className="shimmer-line short" />
        <div className="shimmer-line" />
      </div>
    </div>
  );
}

function FullShimmerEffect() {
  return (
    <div className="shimmer-container">
      <div className="shimmer-hero" />
      <div className="shimmer-group">
        <div className="shimmer-title" />
        <div className="shimmer-row">
          <ShimmerCard />
          <ShimmerCard />
          <ShimmerCard />
        </div>
      </div>
      <div className="shimmer-group">
        <div className="shimmer-title" />
        <div className="shimmer-list">
          <ShimmerListItem />
          <ShimmerListItem />
          <ShimmerListItem />
        </div>
      </div>
    </div>
  );
}

function ErrorView({ message }: { message: string }) {
  return (
    <div className="error-view">
      <p className="error-title">Unable to load home</p>
      <p className="error-message">{message}</p>
    </div>
  );
}

// ---------- Main Home Page ----------

const Home = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [homeData, setHomeData] = useState<{
    hero: HomeItem[];
    trending: HomeItem[];
    recentlyPlayed: HomeItem[];
    newReleases: HomeItem[];
    topCharts: HomeItem[];
    madeForYou: HomeItem[];
    dailyMix: HomeItem[];
    popularArtists: HomeItem[];
    topHits: HomeItem[];
    topAlbums: HomeItem[];
    topArtists: HomeItem[];
    workout: HomeItem[];
    focusMusic: HomeItem[];
    recommended: HomeItem[];
    personalizedArtist: string;
  }>({
    hero: [],
    trending: [],
    recentlyPlayed: [],
    newReleases: [],
    topCharts: [],
    madeForYou: [],
    dailyMix: [],
    popularArtists: [],
    topHits: [],
    topAlbums: [],
    topArtists: [],
    workout: [],
    focusMusic: [],
    recommended: [],
    personalizedArtist: '',
  });

  const getRecentActivity = useUserActivityStore((s) => s.getRecentActivity);
  const getTopArtists = useUserActivityStore((s) => s.getTopArtists);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      setError(null);

      const activity = getRecentActivity(10);
      const topArtistsResult = getTopArtists(1);
      const topArtist = topArtistsResult[0]?.artist || '';

      try {
        const [
          heroRes,
          madeForYouRes,
          newReleasesRes,
          topChartsRes,
          dailyMixRes,
          popularArtistsRes,
          topHitsRes,
          topAlbumsRes,
          topArtistsRes,
          workoutRes,
          focusRes,
          recommendedRes,
        ] = await Promise.all([
          getTrendingPlaylists(),
          getMadeForYou(topArtist ? `${topArtist} Mix` : undefined),
          getNewReleases(),
          getTopCharts(),
          getDailyMix(),
          getPopularArtists(),
          searchPlaylists('Top Hits'),
          searchAlbums('New'),
          searchArtists('Top'),
          getWorkoutMusic(),
          getFocusMusic(),
          topArtist ? searchPlaylists(topArtist) : Promise.resolve({ data: { results: [] } }),
        ]);

        setHomeData({
          hero: (heroRes.data?.results || []).slice(0, 5).map(mapToHomeItem),
          trending: (heroRes.data?.results || []).slice(0, 6).map(mapToHomeItem),
          recentlyPlayed: activity.map((e: ActivityEntry) => ({
            id: e.songId,
            title: e.title,
            subtitle: e.artist,
            image: e.artworkUrl,
            type: 'song',
          })),
          newReleases: (newReleasesRes.data?.results || []).slice(0, 6).map(mapToHomeItem),
          topCharts: (topChartsRes.data?.results || []).slice(0, 6).map(mapToHomeItem),
          madeForYou: (madeForYouRes.data?.results || []).slice(0, 6).map(mapToHomeItem),
          dailyMix: (dailyMixRes.data?.results || []).slice(0, 6).map(mapToHomeItem),
          popularArtists: (popularArtistsRes.data?.results || []).slice(0, 6).map(mapToHomeItem),
          topHits: (topHitsRes.data?.results || []).slice(0, 6).map(mapToHomeItem),
          topAlbums: (topAlbumsRes.data?.results || []).slice(0, 6).map(mapToHomeItem),
          topArtists: (topArtistsRes.data?.results || []).slice(0, 6).map(mapToHomeItem),
          workout: (workoutRes.data?.results || []).slice(0, 6).map(mapToHomeItem),
          focusMusic: (focusRes.data?.results || []).slice(0, 6).map(mapToHomeItem),
          recommended: (recommendedRes.data?.results || []).slice(0, 6).map(mapToHomeItem),
          personalizedArtist: topArtist,
        });
      } catch (err: any) {
        console.error('Error fetching home data:', err);
        setError(err?.message || 'Failed to load home data');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [getRecentActivity, getTopArtists]);

  if (loading) return <FullShimmerEffect />;
  if (error) return <ErrorView message={error} />;

  return (
    <div className="home-page">
      {/* Header with greeting and icons */}
      <header className="home-header">
        <h2>{getGreeting()}</h2>
        <div className="home-header-icons">
          <button aria-label="Notifications">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9" />
              <path d="M10.3 21a1.94 1.94 0 0 0 3.4 0" />
            </svg>
          </button>
          <button aria-label="Settings">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1.28 1.85 2 2 0 0 1-2.36-.43l-.3-.3a2 2 0 0 0-2.83 0l-.42.42a2 2 0 0 0 0 2.83l.3.3a2 2 0 0 1 .43 2.36 2 2 0 0 1-1.85 1.28H2.67a2 2 0 0 0-2 2v.44a2 2 0 0 0 2 2h.18a2 2 0 0 1 1.85 1.28 2 2 0 0 1-.43 2.36l-.3.3a2 2 0 0 0 0 2.83l.42.42a2 2 0 0 0 2.83 0l.3-.3a2 2 0 0 1 2.36.43 2 2 0 0 1 1.28 1.85v.18a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1.28-1.85 2 2 0 0 1 2.36.43l.3.3a2 2 0 0 0 2.83 0l.42-.42a2 2 0 0 0 0-2.83l-.3-.3a2 2 0 0 1-.43-2.36 2 2 0 0 1 1.85-1.28h.59a2 2 0 0 0 2-2v-.44a2 2 0 0 0-2-2h-.18a2 2 0 0 1-1.85-1.28 2 2 0 0 1 .43-2.36l.3-.3a2 2 0 0 0 0-2.83l-.42-.42a2 2 0 0 0-2.83 0l-.3.3a2 2 0 0 1-2.36-.43 2 2 0 0 1-1.28-1.85V4a2 2 0 0 0-2-2Z" />
              <circle cx="12" cy="12" r="3" />
            </svg>
          </button>
        </div>
      </header>

      {homeData.hero.length > 0 && <HeroCarousel items={homeData.hero} />}

      {homeData.recentlyPlayed.length > 0 && (
        <RecentlyPlayedSection items={homeData.recentlyPlayed} />
      )}

      <MoodChips />

      {homeData.trending.length > 0 && (
        <StandardSection
          title="Trending now"
          items={homeData.trending}
          cardType="large"
        />
      )}

      {homeData.madeForYou.length > 0 && (
        <StandardSection
          title="Made for you"
          items={homeData.madeForYou}
          cardType="medium"
        />
      )}

      {homeData.recommended.length > 0 && homeData.personalizedArtist && (
        <StandardSection
          title={`Because you liked ${homeData.personalizedArtist}`}
          items={homeData.recommended}
          cardType="medium"
        />
      )}

      {homeData.newReleases.length > 0 && (
        <StandardSection
          title="New releases"
          items={homeData.newReleases}
          cardType="medium"
        />
      )}

      {homeData.topCharts.length > 0 && (
        <TopChartsSection items={homeData.topCharts} />
      )}

      {homeData.dailyMix.length > 0 && (
        <WideCardsSection title="Your daily mix" items={homeData.dailyMix} />
      )}

      {homeData.popularArtists.length > 0 && (
        <ArtistsSection title="Popular artists" items={homeData.popularArtists} />
      )}

      {homeData.topHits.length > 0 && (
        <StandardSection title="Top hits" items={homeData.topHits} cardType="large" />
      )}

      {homeData.workout.length > 0 && (
        <WideCardsSection title="Workout" items={homeData.workout} />
      )}

      {homeData.focusMusic.length > 0 && (
        <WideCardsSection title="Focus" items={homeData.focusMusic} />
      )}

      {homeData.topAlbums.length > 0 && (
        <StandardSection title="Top albums" items={homeData.topAlbums} cardType="medium" />
      )}

      {homeData.topArtists.length > 0 && (
        <ArtistsSection title="Top artists" items={homeData.topArtists} />
      )}
    </div>
  );
};

export default Home;
