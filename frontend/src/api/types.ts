export interface ApiResponse<T> {
  success: boolean
  code: string
  message: string
  data: T
}

export interface UserProfile {
  id: number
  username: string
  displayName: string
  rbacTier: string
  approvalPolicy: string
  roles: string[]
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: UserProfile
}
