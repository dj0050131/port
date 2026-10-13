import type { ApiResponse } from '@/types/dashboard'
import type { TourismDashboardDataset } from '@/types/tourism'
import { createTourismDatasets } from '@/utils/mock'

const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

const wrapResponse = async <T>(factory: () => T, latency = 200): Promise<ApiResponse<T>> => {
  await delay(latency + Math.random() * 200)
  const data = factory()
  return {
    code: 0,
    message: 'ok',
    data,
    timestamp: new Date().toISOString(),
  }
}

export const getTourismDashboard = async (): Promise<ApiResponse<TourismDashboardDataset[]>> => {
  return wrapResponse(() => createTourismDatasets(), 260)
}

