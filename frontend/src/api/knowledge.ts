import client from './client'
import type { ApiResponse } from './types'

export interface IndexStats {
  ragEnabled: boolean
  embeddingProvider: string
  embeddingDims: number | null
  totalChunks: number
  architectureChunks: number
  workLogChunks: number
  manualChunks: number
  reindexHint: string | null
}

export async function getIndexStats() {
  const { data } = await client.get<ApiResponse<IndexStats>>('/api/knowledge/index-stats')
  return data
}
