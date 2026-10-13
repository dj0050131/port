import request from '@/utils/request'
import type { FiscalYear, PageResult } from '@/types/fiscalYear'

export interface FiscalYearQueryParams {
  page?: number
  size?: number
  current?: number
  pageSize?: number
}

export const fetchFiscalYearsPage = (params: FiscalYearQueryParams) => {
  return request<PageResult<FiscalYear>>({
    url: '/api/billing/fiscalYears/page',
    method: 'get',
    params
  })
}

export const createFiscalYear = (data: FiscalYear) => {
  return request<number>({
    url: '/api/billing/fiscalYears',
    method: 'post',
    data
  })
}

export const updateFiscalYear = (id: number, data: FiscalYear) => {
  return request<void>({
    url: `/api/billing/fiscalYears/${id}`,
    method: 'put',
    data
  })
}

export const deleteFiscalYear = (id: number) => {
  return request<void>({
    url: `/api/billing/fiscalYears/${id}`,
    method: 'delete'
  })
}

export const updateFiscalYearStatus = (id: number, status: boolean) => {
  return request<void>({
    url: `/api/billing/fiscalYears/${id}/status`,
    method: 'put',
    params: { status }
  })
}

