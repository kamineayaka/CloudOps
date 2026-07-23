import client from './client'
import type { ApiResponse } from './types'

export interface Approval {
  id: number
  requesterId: number
  approverId: number | null
  action: string
  resource: string | null
  riskLevel: string
  payload: string
  status: string
  reason: string | null
  createdAt: string
  decidedAt: string | null
}

export async function listPendingApprovals() {
  const { data } = await client.get<ApiResponse<Approval[]>>('/api/approvals/pending')
  return data
}

export async function decideApproval(
  id: number,
  decision: 'APPROVE' | 'REJECT',
  reason?: string,
  rememberForSession?: boolean,
) {
  const { data } = await client.post<ApiResponse<Approval>>(`/api/approvals/${id}/decide`, {
    decision,
    reason,
    rememberForSession: rememberForSession ?? false,
  })
  return data
}
