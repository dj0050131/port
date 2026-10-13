import request from '@/utils/request';
import type { Vessels } from '@/types/bases/vessels';
import type { PaginatedResult } from '@/types/common';

export const getVesselsList = (params: any) => {
  return request<PaginatedResult<Vessels>>({
    url: '/api/base/vessels/page',
    method: 'get',
    params
  });
};

export const fetchVesselList = () => {
  return request({
    url: '/api/base/vessels/list',
    method: 'get'
  })
}

export const getVesselsById = (id: number) => {
  return request<Vessels>({
    url: `/api/base/vessels/${id}`,
    method: 'get'
  });
};

export const createVessels = (data: Vessels) => {
  return request({
    url: '/api/base/vessels',
    method: 'post',
    data
  });
};

export const updateVessels = (id: number, data: Vessels) => {
  return request({
    url: `/api/base/vessels/${id}`,
    method: 'put',
    data
  });
};

export const deleteVessels = (id: number) => {
  return request({
    url: `/api/base/vessels/${id}`,
    method: 'delete'
  });
};