import type {UnwrapRef} from "vue";

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: string
}

export interface PaginationQuery {
  page: number
  pageSize: number
}

export interface PaginatedResult<T> {
  records: UnwrapRef<T[]> | T[];
  items: T[]
  total: number
  page: number
  pageSize: number
}

export interface GeoCoordinate {
  lng: number
  lat: number
}

export type Nullable<T> = T | null | undefined
