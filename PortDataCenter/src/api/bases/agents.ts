import request from '@/utils/request';
import type { Agents } from '@/types/bases/agents';
import type { PaginatedResult } from '@/types/common';

export const getAgentsList = (params: any) => {
  return request<PaginatedResult<Agents>>({
    url: '/api/base/agents/page',
    method: 'get',
    params
  });
};
export const fetchAgentsList = () => {
  return request<Agents[]>({
    url: '/api/base/agents/list',
    method: 'get'
  })
}

export const getAgentsById = (id: number) => {
  return request<Agents>({
    url: `/api/base/agents/${id}`,
    method: 'get'
  });
};

export const createAgents = (data: Agents) => {
  return request({
    url: '/api/base/agents',
    method: 'post',
    data
  });
};

export const updateAgents = (id: number, data: Agents) => {
  return request({
    url: `/api/base/agents/${id}`,
    method: 'put',
    data
  });
};

export const deleteAgents = (id: number) => {
  return request({
    url: `/api/base/agents/${id}`,
    method: 'delete'
  });
};