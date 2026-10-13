import type { ApiResponse, GeoCoordinate, PaginatedResult, PaginationQuery } from './common'

export type ShipKind = 'container' | 'bulk' | 'tanker' | 'passenger' | 'ro-ro'

export interface ShipSummary {
  id: string
  name: string
  imo: string
  mmsi: string
  type: ShipKind
  flag: string
  tonnage: number
  length: number
  width: number
  lastPort?: string
  nextPort?: string
  status: 'arrived' | 'departed' | 'anchored' | 'maintenance'
}

export interface ShipDetail extends ShipSummary {
  draught: number
  owner: string
  agent: string
  contact: string
  certificateValidUntil: string
  lastInspectionAt: string
}

export interface ShipPosition {
  shipId: string
  course: number
  speed: number
  coordinate: GeoCoordinate
  updatedAt: string
}

export type ShipListResponse = ApiResponse<PaginatedResult<ShipSummary>>
export type ShipDetailResponse = ApiResponse<ShipDetail>
export type ShipPositionResponse = ApiResponse<ShipPosition[]>

export type ShipListQuery = PaginationQuery & {
  keyword?: string
  type?: ShipKind
  status?: ShipSummary['status']
}
