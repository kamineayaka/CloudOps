import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as authApi from '@/api/auth'
import type { UserProfile } from '@/api/types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserProfile | null>(null)
  const loading = ref(false)

  function setTokens(accessToken: string, refreshToken: string) {
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
  }

  function clearSession() {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    user.value = null
  }

  async function login(username: string, password: string) {
    loading.value = true
    try {
      const response = await authApi.login(username, password)
      if (!response.success || !response.data) {
        throw new Error(response.message)
      }
      setTokens(response.data.accessToken, response.data.refreshToken)
      user.value = response.data.user
      return response.data
    } finally {
      loading.value = false
    }
  }

  async function fetchMe() {
    const response = await authApi.fetchCurrentUser()
    if (!response.success || !response.data) {
      throw new Error(response.message)
    }
    user.value = response.data
    return response.data
  }

  async function logout() {
    try {
      await authApi.logout()
    } finally {
      clearSession()
    }
  }

  const isAuthenticated = () => Boolean(localStorage.getItem('accessToken'))

  return {
    user,
    loading,
    login,
    fetchMe,
    logout,
    clearSession,
    isAuthenticated,
  }
})
