import request from '@/utils/request';
import type { PilotBoats } from '@/types/bases/pilotboats';
import type { PaginatedResult } from '@/types/common';

export const getPilotBoatsList = (params: any) => {
  return request<PaginatedResult<PilotBoats>>({
    url: '/api/base/pilotBoats/page',
    method: 'get',
    params
  });
};
export const fetchPilotBoatList = () => {
  return request({
    url: '/api/base/pilotBoats/list',
    method: 'get'
  })
}

export const getPilotBoatsById = (id: number) => {
  return request<PilotBoats>({
    url: `/api/base/pilotBoats/${id}`,
    method: 'get'
  });
};

export const createPilotBoats = (data: PilotBoats) => {
  return request({
    url: '/api/base/pilotBoats',
    method: 'post',
    data
  });
};

export const updatePilotBoats = (id: number, data: PilotBoats) => {
  return request({
    url: `/api/base/pilotBoats/${id}`,
    method: 'put',
    data
  });
};

export const deletePilotBoats = (id: number) => {
  return request({
    url: `/api/base/pilotBoats/${id}`,
    method: 'delete'
  });
};