import request from '@/utils/request'
import type { ApiResponse, PaginatedResult } from '@/types/common'
import type { UpdateHistoryItem, UpdateHistoryQuery } from '@/types/updateHistory'

export const fetchUpdateHistoryPage = (params: UpdateHistoryQuery) => {
  return request<ApiResponse<PaginatedResult<UpdateHistoryItem>>>({
    url: '/api/system/update-history/page',
    method: 'get',
    params,
  })
}

export const getUpdateHistoryDetail = (id: number) => {
  return request<ApiResponse<UpdateHistoryItem>>({
    url: `/api/system/update-history/${id}`,
    method: 'get',
  })
}

export const createUpdateHistory = (data: UpdateHistoryItem) => {
  return request<ApiResponse<number>>({
    url: '/api/system/update-history',
    method: 'post',
    data,
  })
}

export const updateUpdateHistory = (id: number, data: UpdateHistoryItem) => {
  return request<ApiResponse<void>>({
    url: `/api/system/update-history/${id}`,
    method: 'put',
    data,
  })
}

export const deleteUpdateHistory = (id: number) => {
  return request<ApiResponse<void>>({
    url: `/api/system/update-history/${id}`,
    method: 'delete',
  })
}

