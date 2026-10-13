import { request } from '@/common/request'

export function login(data) {
  return request({
    url: '/api/auth/login',
    method: 'POST',
    data,
  })
}

