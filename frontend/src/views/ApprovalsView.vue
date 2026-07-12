<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { NButton, NCard, NDataTable, NPopconfirm, NSpace, NTag, useMessage } from 'naive-ui'
import { decideApproval, listPendingApprovals, type Approval } from '@/api/approvals'
import EmptyState from '@/components/EmptyState.vue'
import PageHeader from '@/components/PageHeader.vue'

const { t } = useI18n()
const message = useMessage()

const approvals = ref<Approval[]>([])
const loading = ref(false)

function riskType(level: string) {
  if (level === 'HIGH') return 'error'
  if (level === 'MEDIUM') return 'warning'
  return 'info'
}

const columns = computed(() => [
  { title: t('common.id'), key: 'id', width: 60 },
  { title: t('approvals.action'), key: 'action' },
  {
    title: t('approvals.risk'),
    key: 'riskLevel',
    render: (row: Approval) => h(NTag, { type: riskType(row.riskLevel), size: 'small', round: true }, { default: () => row.riskLevel }),
  },
  { title: t('common.resource'), key: 'resource' },
  { title: t('common.payload'), key: 'payload', ellipsis: { tooltip: true } },
  {
    title: t('common.actions'),
    key: 'actions',
    width: 200,
    render: (row: Approval) =>
      h(NSpace, { size: 8 }, {
        default: () => [
          h(
            NButton,
            { size: 'small', type: 'success', onClick: () => handleDecide(row.id, 'APPROVE') },
            { default: () => t('approvals.approve') },
          ),
          h(
            NPopconfirm,
            { onPositiveClick: () => handleDecide(row.id, 'REJECT') },
            {
              trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => t('approvals.reject') }),
              default: () => t('approvals.confirmReject'),
            },
          ),
        ],
      }),
  },
])

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
    message.success(decision === 'APPROVE' ? t('approvals.approved') : t('approvals.rejected'))
    await load()
  }
}

onMounted(load)
</script>

<template>
  <NSpace vertical :size="16">
    <PageHeader :title="t('approvals.title')" :description="t('approvals.subtitle')">
      <template #extra>
        <NButton @click="load">{{ t('common.refresh') }}</NButton>
      </template>
    </PageHeader>

    <NCard class="page-card" :bordered="false">
      <NDataTable :columns="columns" :data="approvals" :loading="loading" :bordered="false" />
      <EmptyState v-if="!loading && approvals.length === 0" :message="t('approvals.empty')" />
    </NCard>
  </NSpace>
</template>
