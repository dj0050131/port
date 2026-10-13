import request from '@/utils/request'
import type { PilotageRoutes } from '@/types/bases/pilotageRoutes'
import type {PageResult} from "@/api/user.ts";

export const createPilotageRoute = (data: PilotageRoutes) => {
  return request<number>({
    url: '/api/base/pilotageRoutes',
    method: 'post',
    data
  })
}

export const updatePilotageRoute = (id: number, data: PilotageRoutes) => {
  return request<void>({
    url: `/api/base/pilotageRoutes/${id}`,
    method: 'put',
    data
  })
}

export const deletePilotageRoute = (id: number) => {
  return request<void>({
    url: `/api/base/pilotageRoutes/${id}`,
    method: 'delete'
  })
}

export const getPilotageRouteById = (id: number) => {
  return request<PilotageRoutes>({
    url: `/api/base/pilotageRoutes/${id}`,
    method: 'get'
  })
}

export const getPilotageRoutePage = (params: { current: number; size: number }) => {
  return request<PageResult<PilotageRoutes>>({
    url: '/api/base/pilotageRoutes/page',
    method: 'get',
    params
  })
}

export const getPilotageRouteList = () => {
  return request<PilotageRoutes[]>({
    url: '/api/base/pilotageRoutes/list',
    method: 'get'
  })
}

export const searchPilotageRoutes = (keyword: string) => {
  return request<PilotageRoutes[]>({
    url: '/api/base/pilotageRoutes/search',
    method: 'get',
    params: { keyword }
  })
}

export const getPilotageRouteByStartLocation = (startLocationType: string, startLocationId: number) => {
  return request<PilotageRoutes[]>({
    url: '/api/base/pilotageRoutes/byStartLocation',
    method: 'get',
    params: { startLocationType, startLocationId }
  })
}

export const getPilotageRouteByEndLocation = (endLocationType: string, endLocationId: number) => {
  return request<PilotageRoutes[]>({
    url: '/api/base/pilotageRoutes/byEndLocation',
    method: 'get',
    params: { endLocationType, endLocationId }
  })
}

export const getPilotageRouteByStatus = (status: string) => {
  return request<PilotageRoutes[]>({
    url: '/api/base/pilotageRoutes/byStatus',
    method: 'get',
    params: { status }
  })
}