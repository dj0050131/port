import request from '@/utils/request';
import type { Drivers } from '@/types/bases/drivers';
import type { PaginatedResult } from '@/types/common';

export const getDriversList = (params: any) => {
  return request<PaginatedResult<Drivers>>({
    url: '/api/base/drivers/page',
    method: 'get',
    params
  });
};
export const fetchDriverList = () => {
  return request({
    url: '/api/base/drivers/list',
    method: 'get'
  })
}

export const getDriversById = (id: number) => {
  return request<Drivers>({
    url: `/api/base/drivers/${id}`,
    method: 'get'
  });
};

export const createDrivers = (data: Drivers) => {
  return request({
    url: '/api/base/drivers',
    method: 'post',
    data
  });
};

export const updateDrivers = (id: number, data: Drivers) => {
  return request({
    url: `/api/base/drivers/${id}`,
    method: 'put',
    data
  });
};

export const deleteDrivers = (id: number) => {
  return request({
    url: `/api/base/drivers/${id}`,
    method: 'delete'
  });
};