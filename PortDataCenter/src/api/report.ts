import request from '@/utils/request'
import type { ReportPayload } from '@/types/report'

export const fetchReportPayload = () => {
  return request<ReportPayload>({
    url: '/reports/analytics',
    method: 'get',
  })
}
