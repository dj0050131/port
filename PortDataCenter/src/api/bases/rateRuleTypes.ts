import request from '@/utils/request'
import type { RateRuleTypes } from '@/types/bases/rateRuleTypes'
import type { PageResult } from '@/types/base'

export const getRateRuleTypesList = (params: { page: number; size: number; keyword?: string }) => {
  return request<{
    records: RateRuleTypes[]
    total: number
    current: number
    size: number
  }>({
    url: '/api/base/rateRuleTypes/page',
    method: 'get',
    params
  })
}

export const createRateRuleTypes = (data: RateRuleTypes) => {
  return request<number>({
    url: '/api/base/rateRuleTypes',
    method: 'post',
    data
  })
}

export const updateRateRuleTypes = (id: number, data: RateRuleTypes) => {
  return request<void>({
    url: `/api/base/rateRuleTypes/${id}`,
    method: 'put',
    data
  })
}

export const deleteRateRuleTypes = (id: number) => {
  return request<void>({
    url: `/api/base/rateRuleTypes/${id}`,
    method: 'delete'
  })
}

export const getSystemRateRuleTypes = () => {
  return request<RateRuleTypes[]>({
    url: '/api/base/rateRuleTypes/system',
    method: 'get'
  })
}