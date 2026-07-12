<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
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
  NPopconfirm,
  NSelect,
  NSpace,
  NTag,
  useMessage,
} from 'naive-ui'
import { createAsset, deleteAsset, listAssets, saveSshCredential, type Asset } from '@/api/assets'
import EmptyState from '@/components/EmptyState.vue'
import PageHeader from '@/components/PageHeader.vue'

const { t } = useI18n()
const message = useMessage()

const assets = ref<Asset[]>([])
const loading = ref(false)
const showCreate = ref(false)
const showCredential = ref(false)
const selectedAssetId = ref<number | null>(null)

const form = ref({ name: '', kind: 'SERVER', host: '', port: 22 })
const credForm = ref({ username: 'root', authType: 'PASSWORD', secret: '' })

const kindOptions = computed(() => [
  { label: t('assets.kindServer'), value: 'SERVER' },
  { label: t('assets.kindCluster'), value: 'CLUSTER' },
  { label: t('assets.kindService'), value: 'SERVICE' },
])

const columns = computed(() => [
  { title: t('common.id'), key: 'id', width: 60 },
  { title: t('assets.name'), key: 'name' },
  { title: t('assets.kind'), key: 'kind' },
  { title: t('assets.host'), key: 'host' },
  { title: t('assets.port'), key: 'port', width: 80 },
  {
    title: t('assets.credential'),
    key: 'hasSshCredential',
    render: (row: Asset) =>
      h(
        NTag,
        { size: 'small', round: true, type: row.hasSshCredential ? 'success' : 'warning' },
        { default: () => (row.hasSshCredential ? t('common.configured') : t('common.notConfigured')) },
      ),
  },
  {
    title: t('common.actions'),
    key: 'actions',
    width: 200,
    render: (row: Asset) =>
      h(NSpace, { size: 8 }, {
        default: () => [
          h(NButton, { size: 'small', onClick: () => openCredential(row.id) }, { default: () => t('assets.credential') }),
          h(
            NPopconfirm,
            { onPositiveClick: () => handleDelete(row.id) },
            {
              trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => t('common.delete') }),
              default: () => t('common.confirmDelete'),
            },
          ),
        ],
      }),
  },
])

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
    form.value = { name: '', kind: 'SERVER', host: '', port: 22 }
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
  <NSpace vertical :size="16">
    <PageHeader :title="t('assets.title')" :description="t('assets.subtitle')">
      <template #extra>
        <NSpace>
          <NButton @click="load">{{ t('common.refresh') }}</NButton>
          <NButton type="primary" @click="showCreate = true">{{ t('assets.addAsset') }}</NButton>
        </NSpace>
      </template>
    </PageHeader>

    <NCard class="page-card" :bordered="false">
      <NDataTable :columns="columns" :data="assets" :loading="loading" :bordered="false" />
      <EmptyState v-if="!loading && assets.length === 0" :message="t('assets.empty')" />
    </NCard>
  </NSpace>

  <NModal v-model:show="showCreate" preset="card" class="modal-md" :title="t('assets.addAsset')">
    <NForm :model="form" label-placement="top">
      <NFormItem :label="t('assets.name')"><NInput v-model:value="form.name" /></NFormItem>
      <NFormItem :label="t('assets.kind')"><NSelect v-model:value="form.kind" :options="kindOptions" /></NFormItem>
      <NFormItem :label="t('assets.host')">
        <NInput v-model:value="form.host" :placeholder="t('assets.hostPlaceholder')" />
      </NFormItem>
      <NFormItem :label="t('assets.port')"><NInputNumber v-model:value="form.port" :min="1" :max="65535" class="full-width" /></NFormItem>
      <NSpace justify="end">
        <NButton @click="showCreate = false">{{ t('common.cancel') }}</NButton>
        <NButton type="primary" @click="handleCreate">{{ t('common.create') }}</NButton>
      </NSpace>
    </NForm>
  </NModal>

  <NModal v-model:show="showCredential" preset="card" class="modal-md" :title="t('assets.credential')">
    <NForm :model="credForm" label-placement="top">
      <NFormItem :label="t('assets.sshUser')"><NInput v-model:value="credForm.username" spellcheck="false" /></NFormItem>
      <NFormItem :label="t('assets.authType')">
        <NSelect
          v-model:value="credForm.authType"
          :options="[
            { label: t('assets.password'), value: 'PASSWORD' },
            { label: t('assets.privateKey'), value: 'PRIVATE_KEY' },
          ]"
        />
      </NFormItem>
      <NFormItem :label="t('assets.sshSecret')">
        <NInput v-model:value="credForm.secret" type="password" show-password-on="click" />
      </NFormItem>
      <NSpace justify="end">
        <NButton @click="showCredential = false">{{ t('common.cancel') }}</NButton>
        <NButton type="primary" @click="handleSaveCredential">{{ t('common.save') }}</NButton>
      </NSpace>
    </NForm>
  </NModal>
</template>

<style scoped>
.full-width {
  width: 100%;
}
</style>
