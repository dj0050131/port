import request from '@/utils/request'
import type { AgentInvoiceInfo } from '@/types/bases/agentInvoiceInfo'
import type { PageResult } from '@/types/base'

export const createAgentInvoiceInfo = (data: AgentInvoiceInfo) => {
  return request<number>({
    url: '/api/base/agentInvoiceInfo',
    method: 'post',
    data
  })
}

export const updateAgentInvoiceInfo = (id: number, data: AgentInvoiceInfo) => {
  return request<void>({
    url: `/api/base/agentInvoiceInfo/${id}`,
    method: 'put',
    data
  })
}

export const deleteAgentInvoiceInfo = (id: number) => {
  return request<void>({
    url: `/api/base/agentInvoiceInfo/${id}`,
    method: 'delete'
  })
}

export const getAgentInvoiceInfoById = (id: number) => {
  return request<AgentInvoiceInfo>({
    url: `/api/base/agentInvoiceInfo/${id}`,
    method: 'get'
  })
}

export const getAgentInvoiceInfoPage = (params: { current: number; size: number }) => {
  return request<PageResult<AgentInvoiceInfo>>({
    url: '/api/base/agentInvoiceInfo/page',
    method: 'get',
    params
  })
}

export const getAgentInvoiceInfoList = () => {
  return request<AgentInvoiceInfo[]>({
    url: '/api/base/agentInvoiceInfo/list',
    method: 'get'
  })
}

export const getAgentInvoiceInfoByAgentId = (agentId: number) => {
  return request<AgentInvoiceInfo[]>({
    url: `/api/base/agentInvoiceInfo/by-agent/${agentId}`,
    method: 'get'
  })
}

export const getAgentInvoiceInfoByVoyageId = (voyageId: number) => {
  return request<AgentInvoiceInfo[]>({
    url: `/api/base/agentInvoiceInfo/by-voyage/${voyageId}`,
    method: 'get'
  })
}