export interface TugboatCompany {
  id?: number
  companyCode?: string
  companyName?: string
  contactName?: string
  contactPhone?: string
  status?: 'ACTIVE' | 'INACTIVE' | string
  notes?: string
  createdAt?: string
  updatedAt?: string
}

