import request from '@/utils/request';
import type { Pilots } from '@/types/bases/pilots';
import type { PaginatedResult } from '@/types/common';

export const getPilotsList = (params: any) => {
  return request<PaginatedResult<Pilots>>({
    url: '/api/base/pilots/page',
    method: 'get',
    params
  });
};
export const fetchPilotList = () => {
  return request({
    url: '/api/base/pilots/list',
    method: 'get'
  })
}

export const getPilotsById = (id: number) => {
  return request<Pilots>({
    url: `/api/base/pilots/${id}`,
    method: 'get'
  });
};

export const createPilots = (data: Pilots) => {
  return request({
    url: '/api/base/pilots',
    method: 'post',
    data
  });
};

export const updatePilots = (id: number, data: Pilots) => {
  return request({
    url: `/api/base/pilots/${id}`,
    method: 'put',
    data
  });
};

export const deletePilots = (id: number) => {
  return request({
    url: `/api/base/pilots/${id}`,
    method: 'delete'
  });
};