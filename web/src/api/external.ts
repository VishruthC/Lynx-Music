import axios from 'axios';

export interface InvidiousVideo {
  title: string;
  author: string;
}

export interface InvidiousPlaylistResponse {
  title: string;
  playlistThumbnail: string;
  videos: InvidiousVideo[];
}

const INVIDIOUS_INSTANCES = [
  'https://invidious.projectsegfau.lt/',
  'https://inv.tux.im/',
  'https://invidious.flokinet.to/',
  'https://inv.thepixora.com/',
  'https://invidious.io.lol/',
  'https://invidious.no-logs.com/',
  'https://invidious.perennialte.ch/'
];

const PIPED_INSTANCES = [
  'https://pipedapi.kavin.rocks/',
  'https://pipedapi.tokyo.lewd.icu/',
  'https://pipedapi.moomoo.me/'
];

const PROXIES = [
  'https://corsproxy.io/?',
  'https://api.allorigins.win/raw?url=',
  'https://proxy.cors.sh/'
];

export const getYouTubePlaylist = async (
  playlistId: string, 
  onLog?: (msg: string) => void
): Promise<InvidiousPlaylistResponse> => {
  let lastError = null;

  // 1. Try Invidious Instances
  for (const instance of INVIDIOUS_INSTANCES) {
    const targetUrl = `${instance}api/v1/playlists/${playlistId}`;
    for (const proxy of PROXIES) {
      try {
        onLog?.(`Trying Invidious (${instance.split('/')[2]}) via proxy...`);
        const { data } = await axios.get(`${proxy}${encodeURIComponent(targetUrl)}`, {
          timeout: 10000
        });
        
        if (data && (data.videos || data.title)) {
          return {
            title: data.title || 'Imported Playlist',
            playlistThumbnail: data.playlistThumbnail || '',
            videos: (data.videos || []).map((v: any) => ({
              title: v.title,
              author: v.author
            }))
          };
        }
      } catch (err: any) {
        lastError = err;
      }
    }
  }

  // 2. Fallback to Piped API
  for (const instance of PIPED_INSTANCES) {
    const targetUrl = `${instance}playlists/${playlistId}`;
    for (const proxy of PROXIES) {
      try {
        onLog?.(`Trying Piped (${instance.split('/')[2]}) via proxy...`);
        const { data } = await axios.get(`${proxy}${encodeURIComponent(targetUrl)}`, {
          timeout: 10000
        });

        if (data && data.relatedStreams) {
          return {
            title: data.name || 'Imported Playlist',
            playlistThumbnail: data.thumbnailUrl || '',
            videos: data.relatedStreams.map((s: any) => ({
              title: s.title,
              author: s.uploaderName
            }))
          };
        }
      } catch (err: any) {
        lastError = err;
      }
    }
  }
  
  throw lastError || new Error('Failed to fetch playlist. All providers and proxies timed out.');
};
