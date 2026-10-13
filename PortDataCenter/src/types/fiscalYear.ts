export interface FiscalYear {
  id?: number
  year: string
  targetTonnage?: number | string
  targetRevenue?: number | string
  estimatedRevenue?: number | string
  actualTonnage?: number | string
  actualRevenue?: number | string
  forecastRevenue?: number | string
  lastCalculatedAt?: string
  status?: boolean
  createdAt?: string
  updatedAt?: string
}

export interface PageResult<T> {
  total: number
  current: number
  size: number
  records: T[]
}

