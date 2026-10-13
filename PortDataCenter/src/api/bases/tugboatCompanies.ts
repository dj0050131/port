import request from '@/utils/request'
import type { PaginatedResult } from '@/types/common'
import type { TugboatCompany } from '@/types/bases/tugboatCompanies'

export const getTugboatCompaniesPage = (params: any) => {
  return request<PaginatedResult<TugboatCompany>>({
    url: '/api/base/tugboatCompanies/page',
    method: 'get',
    params
  })
}

export const fetchTugboatCompaniesList = (status?: string) => {
  return request<TugboatCompany[]>({
    url: '/api/base/tugboatCompanies/list',
    method: 'get',
    params: status ? { status } : undefined
  })
}

export const createTugboatCompany = (data: TugboatCompany) => {
  return request({
    url: '/api/base/tugboatCompanies',
    method: 'post',
    data
  })
}

export const updateTugboatCompany = (id: number, data: TugboatCompany) => {
  return request({
    url: `/api/base/tugboatCompanies/${id}`,
    method: 'put',
    data
  })
}

export const deleteTugboatCompany = (id: number) => {
  return request({
    url: `/api/base/tugboatCompanies/${id}`,
    method: 'delete'
  })
}

