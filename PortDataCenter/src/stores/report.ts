import { ElMessage } from 'element-plus'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { fetchReportPayload } from '@/api/report'
import type { ReportChartData, ReportPayload } from '@/types/report'

export const useReportStore = defineStore('report', () => {
  const payload = ref<ReportPayload | null>(null)
  const loading = ref(false)

  const fetchReports = async () => {
    loading.value = true
    try {
      const res = await fetchReportPayload()
      if (res.code !== 200) throw new Error(res.message)
      payload.value = res.data
    } catch (error) {
      console.error('[report] fetch error', error)
      ElMessage.error('统计报表数据加载失败')
    } finally {
      loading.value = false
    }
  }

  const chartAccessor = (key: keyof ReportPayload) =>
    computed<ReportChartData | null>(() => (payload.value ? payload.value[key] : null))

  return {
    payload,
    loading,
    fetchReports,
    trafficTrend: chartAccessor('trafficTrend'),
    feeTrend: chartAccessor('feeTrend'),
    pilotageTrend: chartAccessor('pilotageTrend'),
    vesselTypeDistribution: chartAccessor('vesselTypeDistribution'),
    utilizationRate: chartAccessor('utilizationRate'),
  }
})

