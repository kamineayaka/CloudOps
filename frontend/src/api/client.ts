import axios, { type AxiosError } from 'axios'
import type { ApiResponse } from './types'

const client = axios.create({
  baseURL: '/',
  timeout: 15000,
})

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

client.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiResponse<unknown>>) => {
    const code = error.response?.data?.code
    if (code === 'SESSION_KICKED' || error.response?.status === 401) {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  },
)

export default client
