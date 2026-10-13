import request from '@/utils/request'
import type { AnnualReportDTO, MonthlyReportDTO } from '@/types/statistics'

export const getAnnualReport = (year: number) => {
  return request<AnnualReportDTO>({
    url: '/api/statistics/annualReport',
    method: 'get',
    params: { year }
  })
}

export const getMonthlyReport = (yearMonth: string) => {
  return request<MonthlyReportDTO>({
    url: '/api/statistics/monthlyReport',
    method: 'get',
    params: { yearMonth }
  })
}