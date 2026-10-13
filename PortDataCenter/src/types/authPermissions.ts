export interface AuthPermission {
  id?: number
  permCode: string
  permName: string
  permGroup?: string
  description?: string
  isSystem?: boolean
  sortOrder?: number
  createdAt?: string
  updatedAt?: string
}

