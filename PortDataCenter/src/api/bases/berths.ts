import request from '@/utils/request';
import type { Berths } from '@/types/bases/berths';
import type { PaginatedResult } from '@/types/common';

export const getBerthsList = (params: any) => {
  return request<PaginatedResult<Berths>>({
    url: '/api/base/berths/page',
    method: 'get',
    params
  });
};
export const fetchBerthListByWharfId = (wharvesId?: string) => {
  return request<Berths[]>({
    url: '/api/base/berths/list',
    method: 'get',
    params: { wharvesId }
  })
}
export const getBerthsById = (id: number) => {
  return request<Berths>({
    url: `/api/base/berths/${id}`,
    method: 'get'
  });
};

export const createBerths = (data: Berths) => {
  return request({
    url: '/api/base/berths',
    method: 'post',
    data
  });
};

export const updateBerths = (id: number, data: Berths) => {
  return request({
    url: `/api/base/berths/${id}`,
    method: 'patch',
    data
  });
};

export const deleteBerths = (id: number) => {
  return request({
    url: `/api/base/berths/${id}`,
    method: 'delete'
  });
};