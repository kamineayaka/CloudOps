<script setup lang="ts">
import { h, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  NButton,
  NCard,
  NDataTable,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NModal,
  NSelect,
  NSpace,
  useMessage,
} from 'naive-ui'
import { createAsset, deleteAsset, listAssets, saveSshCredential, type Asset } from '@/api/assets'

const { t } = useI18n()
const message = useMessage()

const assets = ref<Asset[]>([])
const loading = ref(false)
const showCreate = ref(false)
const showCredential = ref(false)
const selectedAssetId = ref<number | null>(null)

const form = ref({ name: '', kind: 'SERVER', host: '', port: 22 })
const credForm = ref({ username: 'root', authType: 'PASSWORD', secret: '' })

const kindOptions = [
  { label: 'SERVER', value: 'SERVER' },
  { label: 'CLUSTER', value: 'CLUSTER' },
  { label: 'SERVICE', value: 'SERVICE' },
]

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: t('assets.name'), key: 'name' },
  { title: t('assets.kind'), key: 'kind', render: (row: Asset) => row.kind },
  { title: t('assets.host'), key: 'host' },
  { title: t('assets.port'), key: 'port', width: 80 },
  {
    title: t('assets.credential'),
    key: 'hasSshCredential',
    render: (row: Asset) => (row.hasSshCredential ? t('common.configured') : t('common.notConfigured')),
  },
  {
    title: t('common.actions'),
    key: 'actions',
    render: (row: Asset) => [
      h(NButton, { size: 'small', onClick: () => openCredential(row.id) }, { default: () => t('assets.credential') }),
      h(NButton, { size: 'small', type: 'error', style: 'margin-left:8px', onClick: () => handleDelete(row.id) }, { default: () => t('common.delete') }),
    ],
  },
]

async function load() {
  loading.value = true
  try {
    const res = await listAssets()
    if (res.success && res.data) assets.value = res.data
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  const res = await createAsset({ ...form.value, kind: form.value.kind as 'SERVER' })
  if (res.success) {
    message.success(t('assets.created'))
    showCreate.value = false
    await load()
  }
}

function openCredential(assetId: number) {
  selectedAssetId.value = assetId
  showCredential.value = true
}

async function handleSaveCredential() {
  if (!selectedAssetId.value) return
  const res = await saveSshCredential(selectedAssetId.value, credForm.value)
  if (res.success) {
    message.success(t('assets.credentialSaved'))
    showCredential.value = false
    await load()
  }
}

async function handleDelete(id: number) {
  const res = await deleteAsset(id)
  if (res.success) {
    message.success(t('assets.deleted'))
    await load()
  }
}

onMounted(load)
</script>

<template>
  <NCard :title="t('assets.title')">
    <template #header-extra>
      <NSpace>
        <NButton @click="load">{{ t('common.refresh') }}</NButton>
        <NButton type="primary" @click="showCreate = true">{{ t('assets.addAsset') }}</NButton>
      </NSpace>
    </template>
    <NDataTable :columns="columns" :data="assets" :loading="loading" :bordered="false" />
  </NCard>

  <NModal v-model:show="showCreate" preset="card" :title="t('assets.addAsset')" style="width: 480px">
    <NForm :model="form">
      <NFormItem :label="t('assets.name')"><NInput v-model:value="form.name" /></NFormItem>
      <NFormItem :label="t('assets.kind')"><NSelect v-model:value="form.kind" :options="kindOptions" /></NFormItem>
      <NFormItem :label="t('assets.host')"><NInput v-model:value="form.host" placeholder="192.168.1.10" /></NFormItem>
      <NFormItem :label="t('assets.port')"><NInputNumber v-model:value="form.port" :min="1" :max="65535" /></NFormItem>
      <NButton type="primary" @click="handleCreate">{{ t('common.create') }}</NButton>
    </NForm>
  </NModal>

  <NModal v-model:show="showCredential" preset="card" :title="t('assets.credential')" style="width: 480px">
    <NForm :model="credForm">
      <NFormItem :label="t('assets.sshUser')"><NInput v-model:value="credForm.username" /></NFormItem>
      <NFormItem :label="t('assets.authType')">
        <NSelect v-model:value="credForm.authType" :options="[{ label: t('assets.password'), value: 'PASSWORD' }, { label: t('assets.privateKey'), value: 'PRIVATE_KEY' }]" />
      </NFormItem>
      <NFormItem :label="t('assets.sshSecret')"><NInput v-model:value="credForm.secret" type="password" show-password-on="click" /></NFormItem>
      <NButton type="primary" @click="handleSaveCredential">{{ t('common.save') }}</NButton>
    </NForm>
  </NModal>
</template>
