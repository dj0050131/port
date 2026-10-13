import type { ApiResponse, PaginatedResult } from './common'

export type FeeCategory = 'pilotage' | 'berth' | 'towage' | 'mooring' | 'other'

export interface FeeRate {
  id: string
  name: string
  category: FeeCategory
  unit: 'per_ship' | 'per_hour' | 'per_ton'
  amount: number
  currency: string
  effectiveFrom: string
  effectiveTo?: string
}

export interface FeeTransaction {
  id: string
  shipName: string
  category: FeeCategory
  amount: number
  currency: string
  createdAt: string
  status: 'pending' | 'confirmed' | 'refunded'
}

export interface FeeSummary {
  total: number
  confirmed: number
  pending: number
  refunded: number
  currency: string
}

export interface FeeTrendPoint {
  time: string
  value: number
}

export interface FeeCategoryStatistic {
  category: FeeCategory
  amount: number
}

export interface FeeSummaryBundle {
  summary: FeeSummary
  trend: FeeTrendPoint[]
  category: FeeCategoryStatistic[]
}

export type FeeRateResponse = ApiResponse<FeeRate[]>
export type FeeTransactionResponse = ApiResponse<PaginatedResult<FeeTransaction>>
export type FeeSummaryResponse = ApiResponse<FeeSummaryBundle>

