<script setup lang="ts">
import { h, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { NButton, NCard, NDataTable, NTag, useMessage } from 'naive-ui'
import client from '@/api/client'
import type { ApiResponse } from '@/api/types'

const { t } = useI18n()
const message = useMessage()

interface AuditLog {
  id: number
  actorName: string | null
  action: string
  resource: string | null
  riskLevel: string | null
  status: string
  createdAt: string
}

const logs = ref<AuditLog[]>([])
const loading = ref(false)

const columns = [
  { title: 'ID', key: 'id', width: 70 },
  { title: '操作者', key: 'actorName' },
  { title: '动作', key: 'action' },
  { title: '资源', key: 'resource' },
  { title: '风险', key: 'riskLevel' },
  { title: '状态', key: 'status', render: (row: AuditLog) => h(NTag, { type: row.status === 'SUCCESS' ? 'success' : 'error' }, { default: () => row.status }) },
  { title: '时间', key: 'createdAt' },
]

async function load() {
  loading.value = true
  try {
    const { data } = await client.get<ApiResponse<{ content: AuditLog[] }>>('/api/audit?page=0&size=50')
    if (data.success && data.data) {
      const page = data.data as { content?: AuditLog[] }
      logs.value = page.content ?? (data.data as unknown as AuditLog[])
    }
  } finally {
    loading.value = false
  }
}

async function verifyChain() {
  const { data } = await client.get<ApiResponse<boolean>>('/api/audit/verify')
  if (data.success) {
    message.info(data.data ? t('audit.chainValid') : t('audit.chainInvalid'))
  }
}

onMounted(load)
</script>

<template>
  <NCard :title="t('audit.title')">
    <template #header-extra>
      <NButton @click="verifyChain">{{ t('audit.verify') }}</NButton>
      <NButton style="margin-left:8px" @click="load">{{ t('common.refresh') }}</NButton>
    </template>
    <NDataTable :columns="columns" :data="logs" :loading="loading" :bordered="false" />
  </NCard>
</template>
