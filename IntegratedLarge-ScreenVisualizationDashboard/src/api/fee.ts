import { get, post } from '@/utils/request';
import type { FeeCalculatePayload, FeeCalculateResult, FeeRateItem } from '@/types/fee';

export const getFeeRates = () => {
  return get<FeeRateItem[]>('/fee/rate/list');
};

export const createOrUpdateFeeRate = (payload: FeeRateItem) => {
  return post('/fee/rate/save', payload);
};

export const calculateFee = (payload: FeeCalculatePayload) => {
  return post<FeeCalculateResult>('/fee/calculate', payload);
};

