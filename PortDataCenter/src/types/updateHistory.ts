import type { PaginatedResult } from '@/types/common'

export interface UpdateHistoryItem {
  id?: number
  version?: string
  title: string
  content?: string
  published?: number
  publishedAt?: string
  createdAt?: string
  updatedAt?: string
}

export interface UpdateHistoryQuery {
  current: number
  size: number
  keyword?: string
  published?: number
}

export type UpdateHistoryPage = PaginatedResult<UpdateHistoryItem>

