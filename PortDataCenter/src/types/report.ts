import type { ApiResponse } from './common'

export interface ReportSeriesItem {
  name: string
  value: number
}

export interface ReportChartData {
  title: string
  xAxis: string[]
  series: ReportSeriesItem[]
}

export interface ReportPayload {
  trafficTrend: ReportChartData
  feeTrend: ReportChartData
  pilotageTrend: ReportChartData
  vesselTypeDistribution: ReportChartData
  utilizationRate: ReportChartData
}

export type ReportResponse = ApiResponse<ReportPayload>

