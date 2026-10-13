import { ElMessage } from 'element-plus'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { fetchFeeRates, fetchFeeSummary, fetchFeeTransactions } from '@/api/fee'
import type { FeeCategoryStatistic, FeeRate, FeeSummary, FeeTransaction, FeeTrendPoint } from '@/types/fee'
import type { PaginatedResult } from '@/types/common'

interface TransactionPagination {
  page: number
  pageSize: number
  total: number
}

export const useFeeStore = defineStore('fee', () => {
  const summary = ref<FeeSummary | null>(null)
  const trend = ref<FeeTrendPoint[]>([])
  const category = ref<FeeCategoryStatistic[]>([])
  const rates = ref<FeeRate[]>([])
  const transactions = ref<FeeTransaction[]>([])
  const pagination = ref<TransactionPagination>({ page: 1, pageSize: 10, total: 0 })
  const loading = ref(false)

  const fetchSummary = async () => {
    try {
      const res = await fetchFeeSummary()
      if (res.code !== 200) throw new Error(res.message)
      summary.value = res.data.summary
      trend.value = res.data.trend
      category.value = res.data.category
    } catch (error) {
      console.error('[fee] summary error', error)
      ElMessage.error('费收汇总数据加载失败')
    }
  }

  const fetchRates = async () => {
    try {
      const res = await fetchFeeRates()
      if (res.code !== 200) throw new Error(res.message)
      rates.value = res.data
    } catch (error) {
      console.error('[fee] rates error', error)
      ElMessage.error('费率配置数据加载失败')
    }
  }

  const fetchTransactionsList = async (page = pagination.value.page) => {
    loading.value = true
    try {
      const res = await fetchFeeTransactions(page, pagination.value.pageSize)
      if (res.code !== 200) throw new Error(res.message)
      const payload = res.data as PaginatedResult<FeeTransaction>
      transactions.value = payload.items
      pagination.value = {
        page: payload.page,
        pageSize: payload.pageSize,
        total: payload.total,
      }
    } catch (error) {
      console.error('[fee] transactions error', error)
      ElMessage.error('费用交易记录加载失败')
    } finally {
      loading.value = false
    }
  }

  const fetchAll = async () => {
    await Promise.all([fetchSummary(), fetchRates(), fetchTransactionsList(1)])
  }

  const trendOptionData = computed(() => ({
    xAxis: trend.value.map((item) => item.time),
    series: trend.value.map((item) => Number(item.value.toFixed(2))),
  }))

  const categoryOptionData = computed(() =>
    category.value.map((item) => ({
      name: item.category,
      value: Number(item.amount.toFixed(2)),
    })),
  )

  return {
    summary,
    trend,
    category,
    rates,
    transactions,
    pagination,
    loading,
    fetchAll,
    fetchTransactionsList,
    trendOptionData,
    categoryOptionData,
  }
})

