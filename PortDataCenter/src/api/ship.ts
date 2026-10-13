import request from '@/utils/request'
import type { PaginatedResult } from '@/types/common'
import type { ShipDetail, ShipListQuery, ShipPosition, ShipSummary } from '@/types/ship'

export const fetchShipList = (query: ShipListQuery) => {
  return request<PaginatedResult<ShipSummary>>({
    url: '/ship/list',
    method: 'get',
    params: {
      page: query.page,
      pageSize: query.pageSize,
      keyword: query.keyword,
      type: query.type,
      status: query.status,
    },
  })
}

export const fetchShipDetail = (shipId: string) => {
  return request<ShipDetail>({
    url: `/ship/detail/${shipId}`,
    method: 'get',
  })
}

export const fetchShipPositions = () => {
  return request<ShipPosition[]>({
    url: '/ship/positions',
    method: 'get',
  })
}
