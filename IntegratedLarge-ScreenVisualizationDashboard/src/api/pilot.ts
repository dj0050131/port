import { get, post } from '@/utils/request';
import type { PageResult, PaginationQuery } from '@/types/common';
import type { PilotApply, PilotPlan, RealTimePilotData, ShipLocation } from '@/types/pilot';

export const getPilotApplyList = (params?: PaginationQuery) => {
  return get<PageResult<PilotApply>>('/pilot/apply/list', params);
};

export const getPilotRealTimeData = () => {
  return get<RealTimePilotData>('/pilot/plan/real-time');
};

export const getPilotPlans = () => {
  return get<PilotPlan[]>('/pilot/plan/list');
};

export const submitPilotApply = (payload: PilotApply) => {
  return post('/pilot/apply/submit', payload);
};

export const getShipLocations = () => {
  return get<ShipLocation[]>('/map/ship/location');
};

