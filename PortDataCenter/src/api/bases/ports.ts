import request from '@/utils/request';
import type { PaginatedResult } from '@/types/common';
import type {Port, PortArea, Wharves} from "@/types/bases/ports.ts";
import type {Anchorages} from "@/types/bases/anchorages.ts";
import type {Berths} from "@/types/bases/berths.ts";

export const getPortsList = (params?: any) => {
  return request<PaginatedResult<Port>>({
    url: '/api/base/ports/page',
    method: 'get',
    params
  });
};

export const getPortsById = (id: number) => {
  return request<Port>({
    url: `/api/base/ports/${id}`,
    method: 'get'
  });
};

export const fetchPortAreasByPortId = (portId: number) => {
    return request<PortArea[]>({
        url: '/api/base/portAreas/list',
        method: 'get',
        params: {
            portId
        }
    })
}

export const fetchWharvesByPortAreaId = (portAreaId: number) => {
    return request<Wharves[]>({
        url: '/api/base/wharves/list',
        method: 'get',
        params: {
            portAreaId
        }
    })
}

export const fetchWharvesAll = () => {
    return request<Wharves[]>({
        url: '/api/base/wharves/list',
        method: 'get'
    })
}

export const fetchAnchoragesByPortAreaId = (portAreaId: number) => {
    return request<Anchorages[]>({
        url: '/api/base/anchorages/list',
        method: 'get',
        params: {
            portAreaId
        }
    })
}

export const fetchBerthsByWharvesId = (wharvesId: number) => {
    return request<Berths[]>({
        url: '/api/base/berths/list',
        method: 'get',
        params: {
            wharvesId
        }
    })
}

export const createPorts = (data: Port) => {
  return request({
    url: '/api/base/ports',
    method: 'post',
    data
  });
};

export const updatePorts = (id: number, data: Port) => {
  return request({
    url: `/api/base/ports/${id}`,
    method: 'patch',
    data
  });
};

export const deletePorts = (id: number) => {
  return request({
    url: `/api/base/ports/${id}`,
    method: 'delete'
  });
};