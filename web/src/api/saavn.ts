import axios from 'axios';
import type { 
  SearchResponse, 
  GenericSearchResponse, 
  CollectionDetailsResponse, 
  LrcLibResponse 
} from '../types';

const BASE_URL = 'https://jiosaavn-api-v4.vercel.app/';

const api = axios.create({
  baseURL: BASE_URL,
});

export const searchSongs = async (query: string): Promise<SearchResponse> => {
  const { data } = await api.get('api/search/songs', { params: { query } });
  return data;
};

export const searchAlbums = async (query: string): Promise<GenericSearchResponse> => {
  const { data } = await api.get('api/search/albums', { params: { query } });
  return data;
};

export const searchArtists = async (query: string): Promise<GenericSearchResponse> => {
  const { data } = await api.get('api/search/artists', { params: { query } });
  return data;
};

export const searchPlaylists = async (query: string): Promise<GenericSearchResponse> => {
  const { data } = await api.get('api/search/playlists', { params: { query } });
  return data;
};

export const getPlaylistDetails = async (id: string, limit = 50): Promise<CollectionDetailsResponse> => {
  const { data } = await api.get('api/playlists', { params: { id, limit } });
  return data;
};

export const getAlbumDetails = async (id: string): Promise<CollectionDetailsResponse> => {
  const { data } = await api.get('api/albums', { params: { id } });
  return data;
};

export const searchLyrics = async (query: string): Promise<LrcLibResponse[]> => {
  const { data } = await axios.get('https://lrclib.net/api/search', { params: { q: query } });
  return data;
};

export const getTrendingPlaylists = async () => searchPlaylists("Trending");
export const getNewReleases = async () => searchAlbums("New Release");
export const getTopCharts = async () => searchPlaylists("Top 50");
export const getMadeForYou = async (query?: string) => searchPlaylists(query || "Mix");
export const getDailyMix = async () => searchPlaylists("Daily Mix");
export const getWorkoutMusic = async () => searchPlaylists("Workout");
export const getFocusMusic = async () => searchPlaylists("Focus");
export const getPopularArtists = async () => searchArtists("Popular");
