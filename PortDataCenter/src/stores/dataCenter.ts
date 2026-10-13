import { defineStore } from 'pinia'
import { reactive } from 'vue'

export interface TableState<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
  loading: boolean
}

export const useDataCenterStore = defineStore('data-center', () => {
  const tables = reactive<Record<string, TableState<unknown>>>({})

  const ensureTable = <T>(key: string, defaults: TableState<T>): TableState<T> => {
    if (!tables[key]) {
      tables[key] = { ...defaults }
    }
    return tables[key] as TableState<T>
  }

  return {
    tables,
    ensureTable,
  }
})
