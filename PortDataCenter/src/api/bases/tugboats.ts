import request from '@/utils/request';
import type { Tugboats } from '@/types/bases/tugboats';
import type { PaginatedResult } from '@/types/common';

export const getTugboatsList = (params: any) => {
  return request<PaginatedResult<Tugboats>>({
    url: '/api/base/tugboats/page',
    method: 'get',
    params
  });
};

export const getTugboatsById = (id: number) => {
  return request<Tugboats>({
    url: `/api/base/tugboats/${id}`,
    method: 'get'
  });
};
export const fetchTugBoatList = () => {
  return request({
    url: '/api/base/tugboats/list',
    method: 'get'
  })
}
export const createTugboats = (data: Tugboats) => {
  return request({
    url: '/api/base/tugboats',
    method: 'post',
    data
  });
};

export const updateTugboats = (id: number, data: Tugboats) => {
  return request({
    url: `/api/base/tugboats/${id}`,
    method: 'put',
    data
  });
};

export const deleteTugboats = (id: number) => {
  return request({
    url: `/api/base/tugboats/${id}`,
    method: 'delete'
  });
};