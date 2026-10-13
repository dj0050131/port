import request from '@/utils/request'
import type { ChargeItem } from '@/types/bases/chargeItems'

export const getChargeItemList = (categoryId?: number) => {
  return request<ChargeItem[]>({
    url: '/api/base/chargeItems/list',
    method: 'get',
    params: categoryId ? { categoryId } : undefined
  })
}

export const getChargeItemsPage = (params: { page: number; size: number; keyword?: string; categoryId?: number }) => {
  return request<{ records: ChargeItem[]; total: number; current: number; size: number }>({
    url: '/api/base/chargeItems/page',
    method: 'get',
    params: {
      current: params.page,
      size: params.size,
      keyword: params.keyword,
      categoryId: params.categoryId,
    },
  })
}

export const getChargeItemDetail = (id: number) => {
  return request<ChargeItem>({
    url: `/api/base/chargeItems/${id}`,
    method: 'get',
  })
}

export const createChargeItem = (data: Partial<ChargeItem>) => {
  return request<number>({
    url: '/api/base/chargeItems',
    method: 'post',
    data,
  })
}

export const updateChargeItem = (id: number, data: Partial<ChargeItem>) => {
  return request<void>({
    url: `/api/base/chargeItems/${id}`,
    method: 'put',
    data,
  })
}

export const deleteChargeItem = (id: number) => {
  return request<void>({
    url: `/api/base/chargeItems/${id}`,
    method: 'delete',
  })
}

