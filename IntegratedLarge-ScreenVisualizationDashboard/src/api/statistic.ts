import { get } from '@/utils/request';
import type { TrendChartData } from '@/types/pilot';

export interface RevenueOverview {
  total: number;
  monthOverMonth: number;
  categories: {
    name: string;
    value: number;
  }[];
}

export const getPilotTrend = (params: { startDate: string; endDate: string }) => {
  return get<TrendChartData>('/statistic/pilot/trend', params);
};

export const getRevenueOverview = () => {
  return get<RevenueOverview>('/statistic/fee/overview');
};

