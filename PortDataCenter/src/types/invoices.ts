import type { ApiResponse, PaginatedResult } from './common'
import type { BillingResultVO } from './billingVouchers'

export interface InvoiceQueryParams {
  current: number
  size: number
  keyword?: string
  status?: string
}

export type InvoicePageResponse = ApiResponse<PaginatedResult<BillingResultVO>>

