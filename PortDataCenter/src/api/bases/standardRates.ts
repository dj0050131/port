import request from '@/utils/request'
import type { 
  StandardRates, 
  StandardRatesWithRulesDTO,
  RateRuleTypes,
  RateRules
} from '@/types/bases/standardRates'
import type { PageResult } from '@/types/base'

export const createStandardRateWithRules = (data: StandardRatesWithRulesDTO) => {
  return request<number>({
    url: '/api/base/standardRates',
    method: 'post',
    data
  })
}

export const updateStandardRateWithRules = (id: number, data: StandardRatesWithRulesDTO) => {
  return request<void>({
    url: `/api/base/standardRates/${id}`,
    method: 'put',
    data
  })
}

export const deleteStandardRate = (id: number) => {
  return request<void>({
    url: `/api/base/standardRates/${id}`,
    method: 'delete'
  })
}

export const getStandardRateWithRulesById = (id: number) => {
  return request<StandardRatesWithRulesDTO>({
    url: `/api/base/standardRates/${id}`,
    method: 'get'
  })
}

export const getStandardRatePage = (params: { current: number; size: number }) => {
  return request<PageResult<StandardRates>>({
    url: '/api/base/standardRates/page',
    method: 'get',
    params
  })
}

export const getStandardRateList = () => {
  return request<StandardRates[]>({
    url: '/api/base/standardRates/list',
    method: 'get'
  })
}

export const getStandardRateByCategoryId = (categoryId: number) => {
  return request<StandardRates[]>({
    url: `/api/base/standardRates/byCategory/${categoryId}`,
    method: 'get'
  })
}

export const getStandardRateByRouteId = (routeId: number) => {
  return request<StandardRates[]>({
    url: `/api/base/standardRates/byRoute/${routeId}`,
    method: 'get'
  })
}

export const getActiveStandardRates = () => {
  return request<StandardRates[]>({
    url: '/api/base/standardRates/active',
    method: 'get'
  })
}

export const searchStandardRates = (keyword: string) => {
  return request<StandardRates[]>({
    url: '/api/base/standardRates/search',
    method: 'get',
    params: { keyword }
  })
}

export const getStandardRateByNetTonnage = (minNetTonnage: number, maxNetTonnage: number) => {
  return request<StandardRates[]>({
    url: '/api/base/standardRates/byNetTonnage',
    method: 'get',
    params: { minNetTonnage, maxNetTonnage }
  })
}

export const getStandardRateByDateRange = (startDate: string, endDate: string) => {
  return request<StandardRates[]>({
    url: '/api/base/standardRates/byDateRange',
    method: 'get',
    params: { startDate, endDate }
  })
}

export const getStandardRateByCategoryName = (categoryName: string) => {
  return request<StandardRates[]>({
    url: '/api/base/standardRates/byCategoryName',
    method: 'get',
    params: { categoryName }
  })
}

export const getAllRateRuleTypes = () => {
  return request<RateRuleTypes[]>({
    url: '/api/base/standardRates/rules/types',
    method: 'get'
  })
}

export const getSystemRateRuleTypes = () => {
  return request<RateRuleTypes[]>({
    url: '/api/base/standardRates/rules/types/system',
    method: 'get'
  })
}