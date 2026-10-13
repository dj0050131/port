import request from '@/utils/request';
import type { Vehicles } from '@/types/bases/vehicles';
import type { PaginatedResult } from '@/types/common';

export const getVehiclesList = (params: any) => {
  return request<PaginatedResult<Vehicles>>({
    url: '/api/base/vehicles/page',
    method: 'get',
    params
  });
};
export const fetchVehicleList = () => {
  return request({
    url: '/api/base/vehicles/list',
    method: 'get'
  })
}

export const getVehiclesById = (id: number) => {
  return request<Vehicles>({
    url: `/api/base/vehicles/${id}`,
    method: 'get'
  });
};

export const createVehicles = (data: Vehicles) => {
  return request({
    url: '/api/base/vehicles',
    method: 'post',
    data
  });
};

export const updateVehicles = (id: number, data: Vehicles) => {
  return request({
    url: `/api/base/vehicles/${id}`,
    method: 'put',
    data
  });
};

export const deleteVehicles = (id: number) => {
  return request({
    url: `/api/base/vehicles/${id}`,
    method: 'delete'
  });
};