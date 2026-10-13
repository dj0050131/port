import request from '@/utils/request'
import type { 
  ApiResponse, 
  PaginatedResult 
} from '@/types/common'
import type { FeeWriteOffs } from '@/types/writeoffs'
import type { PageQueryParams } from './charge'

// API 函数
export const createFeeWriteOff = (data: Partial<FeeWriteOffs>) => {
  return request<ApiResponse<number>>({
    url: '/api/billing/writeoffs',
    method: 'post',
    data
  })
}
export const getFeeWriteOffById = (id: number) => {
  return request<ApiResponse<FeeWriteOffs>>({
    url: `/api/billing/writeoffs/${id}`,
    method: 'get'
  })
}
export const updateFeeWriteOff = (id: number, data: Partial<FeeWriteOffs>) => {
  return request<ApiResponse<void>>({
    url: `/api/billing/writeoffs/${id}`,
    method: 'put',
    data
  })
}
export const deleteFeeWriteOff = (id: number) => {
  return request<ApiResponse<void>>({
    url: `/api/billing/writeoffs/${id}`,
    method: 'delete'
  })
}
export const deleteFeeWriteOffBatch = (ids: number[]) => {
  return request<ApiResponse<void>>({
    url: `/api/billing/writeoffs/batch`,
    method: 'delete',
    params: {
      ids: ids.join(',')
    }
  })
}
export const fetchFeeWriteOffs = (params: PageQueryParams) => {
  return request<ApiResponse<PaginatedResult<FeeWriteOffs>>>({
    url: '/api/billing/writeoffs/page',
    method: 'get',
    params
  })
}