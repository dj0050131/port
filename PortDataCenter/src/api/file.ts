import request from '@/utils/request'
export const uploadAvatar = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request<string>({
    url: '/api/files/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
export const uploadFile = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request<string>({
    url: '/api/files/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

