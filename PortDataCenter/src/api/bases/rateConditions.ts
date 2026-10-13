import request from '@/utils/request'
import type { RateCondition } from '@/types/bases/rateConditions'
import type { PaginatedResult } from '@/types/common'

export const getRateConditionsPage = (params: {
  page: number
  size: number
  standardRateId?: number
  conditionType?: string
  keyword?: string
}) => {
  return request<PaginatedResult<RateCondition>>({
    url: '/api/base/rateConditions/page',
    method: 'get',
    params: {
      current: params.page,
      size: params.size,
      standardRateId: params.standardRateId,
      conditionType: params.conditionType,
      keyword: params.keyword,
    },
  })
}

export const listRateConditionsByStandardRateId = (standardRateId: number) => {
  return request<RateCondition[]>({
    url: '/api/base/rateConditions/list',
    method: 'get',
    params: { standardRateId },
  })
}

export const getRateConditionById = (id: number) => {
  return request<RateCondition>({
    url: `/api/base/rateConditions/${id}`,
    method: 'get',
  })
}

export const createRateCondition = (data: RateCondition) => {
  return request<number>({
    url: '/api/base/rateConditions',
    method: 'post',
    data,
  })
}

export const updateRateCondition = (id: number, data: Partial<RateCondition>) => {
  return request<void>({
    url: `/api/base/rateConditions/${id}`,
    method: 'put',
    data,
  })
}

export const deleteRateCondition = (id: number) => {
  return request<void>({
    url: `/api/base/rateConditions/${id}`,
    method: 'delete',
  })
}
