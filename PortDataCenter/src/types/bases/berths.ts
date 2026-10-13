export interface Berths {
  id?: number;
  berthCode?: string;
  portArea?: string;
  berthType?: string;
  berthName?: string;
  wharfDirection?: string;
  longitude?: number;
  latitude?: number;
  startLatitude?: number;
  startLongitude?: number;
  endLatitude?: number;
  endLongitude?: number;
  berthDirection?: string;
  maxDraft?: number;
  berthLength?: number;
  berthDepth?: number;
  berthStatus?: any;
  contactPhone?: string;
  createdAt?: string;
  updatedAt?: string;
  portAreaId?: number;
  wharfId?: number;
}