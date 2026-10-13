import request from '@/utils/request';
import type { Wharves } from '@/types/bases/wharves';
import type { PaginatedResult } from '@/types/common';

export const getWharvesList = (params: any) => {
  return request<PaginatedResult<Wharves>>({
    url: '/api/base/wharves/page',
    method: 'get',
    params
  });
};

export const getWharvesById = (id: number) => {
  return request<Wharves>({
    url: `/api/base/wharves/${id}`,
    method: 'get'
  });
};

export const createWharves = (data: Wharves) => {
  return request({
    url: '/api/base/wharves',
    method: 'post',
    data
  });
};

export const updateWharves = (id: number, data: Wharves) => {
  return request({
    url: `/api/base/wharves/${id}`,
    method: 'patch',
    data
  });
};

export const deleteWharves = (id: number) => {
  return request({
    url: `/api/base/wharves/${id}`,
    method: 'delete'
  });
};