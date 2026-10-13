import type { ApiResponse } from './common'

export interface WeatherSnapshot {
  temperature: number
  humidity: number
  windSpeed: number
  windDirection: string
  visibility: number
  condition: string
  pressure: number
  updatedAt: string
}

export interface TideSnapshot {
  currentHeight: number
  nextHighTide: string
  nextLowTide: string
  warning?: string
}

export interface ForecastItem {
  time: string
  temperature: number
  tide: number
  condition: string
}

export interface WeatherTidePayload {
  weather: WeatherSnapshot
  tide: TideSnapshot
  forecast: ForecastItem[]
}

export type WeatherTideResponse = ApiResponse<WeatherTidePayload>

