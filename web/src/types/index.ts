export interface ImageDto {
  quality: string;
  url: string;
}

export interface DownloadUrlDto {
  quality: string;
  url: string;
}

export interface ArtistItemDto {
  name: string;
}

export interface ArtistsDto {
  primary: ArtistItemDto[];
}

export interface SongDto {
  id: string;
  name: string;
  artists: ArtistsDto;
  image: ImageDto[];
  downloadUrl: DownloadUrlDto[];
  duration: number;
  year: string;
  playCount?: number;
}

export interface SearchData {
  results: SongDto[];
}

export interface SearchResponse {
  success: boolean;
  data: SearchData;
}

export interface GenericDto {
  id: string;
  title?: string;
  name?: string;
  description?: string;
  type: string;
  image: ImageDto[];
}

export interface GenericSearchData {
  results: GenericDto[];
}

export interface GenericSearchResponse {
  success: boolean;
  data: GenericSearchData;
}

export interface CollectionData {
  id: string;
  name?: string;
  title?: string;
  songs: SongDto[];
}

export interface CollectionDetailsResponse {
  success: boolean;
  data: CollectionData;
}

export interface LrcLibResponse {
  id: number;
  syncedLyrics?: string;
  plainLyrics?: string;
}
