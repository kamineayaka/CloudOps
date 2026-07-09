<script setup lang="ts">
import { h, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { NButton, NCard, NDataTable, NTag, useMessage } from 'naive-ui'
import { decideApproval, listPendingApprovals, type Approval } from '@/api/approvals'

const { t } = useI18n()
const message = useMessage()

const approvals = ref<Approval[]>([])
const loading = ref(false)

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: t('approvals.action'), key: 'action' },
  { title: t('approvals.risk'), key: 'riskLevel', render: (row: Approval) => h(NTag, { type: riskType(row.riskLevel) }, { default: () => row.riskLevel }) },
  { title: 'Resource', key: 'resource' },
  { title: 'Payload', key: 'payload', ellipsis: { tooltip: true } },
  {
    title: t('common.actions'),
    key: 'actions',
    render: (row: Approval) => [
      h(NButton, { size: 'small', type: 'success', onClick: () => handleDecide(row.id, 'APPROVE') }, { default: () => t('approvals.approve') }),
      h(NButton, { size: 'small', type: 'error', style: 'margin-left:8px', onClick: () => handleDecide(row.id, 'REJECT') }, { default: () => t('approvals.reject') }),
    ],
  },
]

function riskType(level: string) {
  if (level === 'HIGH') return 'error'
  if (level === 'MEDIUM') return 'warning'
  return 'info'
}

async function load() {
  loading.value = true
  try {
    const res = await listPendingApprovals()
    if (res.success && res.data) approvals.value = res.data
  } finally {
    loading.value = false
  }
}

async function handleDecide(id: number, decision: 'APPROVE' | 'REJECT') {
  const res = await decideApproval(id, decision)
  if (res.success) {
    message.success(decision === 'APPROVE' ? '已批准' : '已拒绝')
    await load()
  }
}

onMounted(load)
</script>

<template>
  <NCard :title="t('approvals.title')">
    <template #header-extra>
      <NButton @click="load">{{ t('common.refresh') }}</NButton>
    </template>
    <NDataTable :columns="columns" :data="approvals" :loading="loading" :bordered="false" />
    <p v-if="!loading && approvals.length === 0" style="color:#94a3b8;text-align:center;padding:24px">
      {{ t('approvals.noPending') }}
    </p>
  </NCard>
</template>
