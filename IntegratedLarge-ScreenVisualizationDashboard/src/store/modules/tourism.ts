import { ElMessage } from 'element-plus'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { TourismDashboardDataset } from '@/types/tourism.ts'
import { getTourismDashboard } from '@/api/tourism.ts'

export const useTourismStore = defineStore('tourism', () => {
  const datasets = ref<TourismDashboardDataset[]>([])
  const loading = ref(false)

  const fetchDashboard = async () => {
    loading.value = true
    try {
      const response = await getTourismDashboard()
      datasets.value = response.data
    } catch (error) {
      console.error('[tourism:fetchDashboard]', error)
      ElMessage.error('数据加载失败，请稍后重试')
    } finally {
      loading.value = false
    }
  }

  const getDatasetByTab = (tab: string) =>
    datasets.value.find((item) => item.tab === tab) ?? datasets.value[0] ?? null

  return {
    datasets,
    loading,
    fetchDashboard,
    getDatasetByTab,
    tabs: computed(() => datasets.value.map((item) => item.tab)),
  }
})

