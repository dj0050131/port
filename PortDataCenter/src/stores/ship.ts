import { ElMessage } from 'element-plus'
import { defineStore } from 'pinia'
import { computed, reactive, ref } from 'vue'
import { fetchShipDetail, fetchShipList, fetchShipPositions } from '@/api/ship'
import type { ShipDetail, ShipListQuery, ShipSummary } from '@/types/ship'
import type { PaginatedResult } from '@/types/common'

const defaultQuery: ShipListQuery = {
  page: 1,
  pageSize: 10,
}

export const useShipStore = defineStore('ship', () => {
  const query = reactive<ShipListQuery>({ ...defaultQuery })
  const ships = ref<ShipSummary[]>([])
  const total = ref(0)
  const loading = ref(false)
  const selectedShip = ref<ShipDetail | null>(null)
  const positions = ref<Record<string, { lng: number; lat: number; speed: number; updatedAt: string }>>({})

  const fetchShips = async () => {
    loading.value = true
    try {
      const res = await fetchShipList(query)
      if (res.code !== 200) throw new Error(res.message)
      const payload = res.data as PaginatedResult<ShipSummary>
      ships.value = payload.items
      total.value = payload.total
    } catch (error) {
      console.error('[ship] list error', error)
      ElMessage.error('船舶信息加载失败')
    } finally {
      loading.value = false
    }
  }

  const fetchShipDetailById = async (shipId: string) => {
    try {
      const res = await fetchShipDetail(shipId)
      if (res.code !== 200) throw new Error(res.message)
      selectedShip.value = res.data
    } catch (error) {
      console.error('[ship] detail error', error)
      ElMessage.error('获取船舶详情失败')
    }
  }

  const fetchShipPositionsRealtime = async () => {
    try {
      const res = await fetchShipPositions()
      if (res.code !== 200) throw new Error(res.message)
      positions.value = res.data.reduce(
        (acc, item) => ({
          ...acc,
          [item.shipId]: { ...item.coordinate, speed: item.speed, updatedAt: item.updatedAt },
        }),
        {} as Record<string, { lng: number; lat: number; speed: number; updatedAt: string }>,
      )
    } catch (error) {
      console.error('[ship] positions error', error)
      ElMessage.warning('实时位置更新失败')
    }
  }

  const resetFilters = () => {
    Object.assign(query, { ...defaultQuery })
  }

  const setPage = (page: number) => {
    query.page = page
  }

  const setPageSize = (pageSize: number) => {
    query.pageSize = pageSize
  }

  const shipWithPosition = computed(() =>
    ships.value.map((ship) => ({
      ...ship,
      position: positions.value[ship.id],
    })),
  )

  return {
    query,
    ships,
    total,
    loading,
    selectedShip,
    positions,
    shipWithPosition,
    fetchShips,
    fetchShipDetailById,
    fetchShipPositionsRealtime,
    resetFilters,
    setPage,
    setPageSize,
  }
})

