export interface TourismMetric {
  id: string
  label: string
  value: number
  unit?: string
  subLabel?: string
  trend?: number
}

export interface VisitorSourceItem {
  rank: number
  region: string
  value: number
}

export interface VehicleTypeShare {
  name: string
  value: number
}

export interface HourlyTrafficPoint {
  time: string
  value: number
}

export interface VehicleRecord {
  plate: string
  type: string
  inTime: string
  outTime: string
  status: string
}

export interface WeatherSnapshot {
  location: string
  temperature: number
  condition: string
  humidity: number
  visibility: number
  windSpeed: number
  windDirection: string
  pressure: number
  pm25: number
  pm10: number
  co: number
  o3: number
}

export type MapPointType = 'scenic' | 'parking' | 'monitor'

export interface MapPoint {
  name: string
  coord: [number, number]
  type: MapPointType
  value: number
  detail: string
}

export interface VideoFeed {
  id: string
  title: string
  location: string
  status: 'online' | 'offline'
  thumbnail: string
}

export interface TourismDashboardDataset {
  tab: string
  metrics: TourismMetric[]
  visitorSources: VisitorSourceItem[]
  vehicleTypes: VehicleTypeShare[]
  hourlyTraffic: HourlyTrafficPoint[]
  vehicleRecords: VehicleRecord[]
  weather: WeatherSnapshot
  mapPoints: MapPoint[]
  videoFeeds: VideoFeed[]
}

