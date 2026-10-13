import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'
import type { InvoicePageResponse, InvoiceQueryParams } from '@/types/invoices'

export const fetchInvoices = (params: InvoiceQueryParams) => {
  return request<InvoicePageResponse>({
    url: '/api/billing/invoices/page',
    method: 'get',
    params,
  })
}

export const redInvoices = (ids: number[], reason: string) => {
  return request<ApiResponse<void>>({
    url: '/api/billing/invoices/red',
    method: 'post',
    params: {
      ids: ids.join(','),
      reason,
    },
  })
}

export const voidInvoices = (ids: number[], reason: string) => {
  return request<ApiResponse<void>>({
    url: '/api/billing/invoices/void',
    method: 'post',
    params: {
      ids: ids.join(','),
      reason,
    },
  })
}

