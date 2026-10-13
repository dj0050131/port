import request from '@/utils/request';
import type { Anchorages } from '@/types/bases/anchorages';
import type { PaginatedResult } from '@/types/common';

export const getAnchoragesList = (params: any) => {
  return request<PaginatedResult<Anchorages>>({
    url: '/api/base/anchorages/page',
    method: 'get',
    params
  });
};
export const fetchAnchorageListByPortAreaId = (portAreaId?: number) => {
  return request({
    url: '/api/base/anchorages/list',
    method: 'get',
    params: { portAreaId }
  })
}

export const getAnchoragesById = (id: number) => {
  return request<Anchorages>({
    url: `/api/base/anchorages/${id}`,
    method: 'get'
  });
};

export const createAnchorages = (data: Anchorages) => {
  return request({
    url: '/api/base/anchorages',
    method: 'post',
    data
  });
};

export const updateAnchorages = (id: number, data: Anchorages) => {
  return request({
    url: `/api/base/anchorages/${id}`,
    method: 'patch',
    data
  });
};

export const deleteAnchorages = (id: number) => {
  return request({
    url: `/api/base/anchorages/${id}`,
    method: 'delete'
  });
};