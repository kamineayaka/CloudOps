import client from './client'
import type { ApiResponse, LoginResponse, UserProfile } from './types'

export async function login(username: string, password: string) {
  const { data } = await client.post<ApiResponse<LoginResponse>>('/api/auth/login', {
    username,
    password,
  })
  return data
}

export async function fetchCurrentUser() {
  const { data } = await client.get<ApiResponse<UserProfile>>('/api/auth/me')
  return data
}

export async function logout() {
  const { data } = await client.post<ApiResponse<null>>('/api/auth/logout')
  return data
}
