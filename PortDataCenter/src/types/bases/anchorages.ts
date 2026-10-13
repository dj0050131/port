export interface Anchorages {
  id?: number;
  portAreaId?: number;
  anchorageCode?: string;
  anchorageNameZh?: string;
  anchorageNameEn?: string;
  anchorageType?: any;
  capacity?: number;
  maxDraft?: number;
  minDraft?: number;
  longitude?: number;
  latitude?: number;
  radius?: number;
  boundaryCoordinates?: any;
  tideRestriction?: string;
  weatherRestriction?: string;
  status?: any;
  createdAt?: string;
  updatedAt?: string;
}