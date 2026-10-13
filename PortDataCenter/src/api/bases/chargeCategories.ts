import request from '@/utils/request'
import type { ChargeCategory } from '@/types/bases/chargeCategories'

export const getChargeCategoryList = () => {
  return request<ChargeCategory[]>({
    url: '/api/base/chargeCategories/list',
    method: 'get'
  })
}

