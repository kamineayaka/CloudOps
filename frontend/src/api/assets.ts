import client from './client'
import type { ApiResponse } from './types'

export interface Asset {
  id: number
  name: string
  kind: string
  host: string | null
  port: number | null
  metadata: string | null
  parentId: number | null
  enabled: boolean
  hasSshCredential: boolean
  createdAt: string
  updatedAt: string
}

export interface AssetRequest {
  name: string
  kind: string
  host?: string
  port?: number
  metadata?: string
  parentId?: number
  enabled?: boolean
}

export async function listAssets() {
  const { data } = await client.get<ApiResponse<Asset[]>>('/api/assets')
  return data
}

export async function createAsset(payload: AssetRequest) {
  const { data } = await client.post<ApiResponse<Asset>>('/api/assets', payload)
  return data
}

export async function deleteAsset(id: number) {
  const { data } = await client.delete<ApiResponse<null>>(`/api/assets/${id}`)
  return data
}

export async function saveSshCredential(assetId: number, payload: { username: string; authType: string; secret: string }) {
  const { data } = await client.post<ApiResponse<null>>(`/api/assets/${assetId}/ssh-credential`, payload)
  return data
}
