import request from '@/utils/request'
import type { PaginatedResult } from '@/types/common'
import type { FeeRate, FeeSummaryBundle, FeeTransaction } from '@/types/fee'

export const fetchFeeRates = () => {
  return request<FeeRate[]>({
    url: '/fee/rate/list',
    method: 'get',
  })
}

export const fetchFeeTransactions = (page: number, pageSize: number) => {
  return request<PaginatedResult<FeeTransaction>>({
    url: '/fee/transactions',
    method: 'get',
    params: { page, size: pageSize },
  })
}

export const fetchFeeSummary = () => {
  return request<FeeSummaryBundle>({
    url: '/fee/summary',
    method: 'get',
  })
}
