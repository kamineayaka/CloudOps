<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { NButton, NCard, NSelect, NSpace, NTag } from 'naive-ui'
import { Terminal } from '@xterm/xterm'
import { FitAddon } from '@xterm/addon-fit'
import { listAssets, type Asset } from '@/api/assets'
import PageHeader from '@/components/PageHeader.vue'
import { useTheme } from '@/composables/useTheme'
import '@xterm/xterm/css/xterm.css'

const { t } = useI18n()
const route = useRoute()
const { isDark } = useTheme()

type ConnStatus = 'disconnected' | 'connected' | 'error'

const assets = ref<Asset[]>([])
const selectedAssetId = ref<number | null>(null)
const terminalRef = ref<HTMLDivElement | null>(null)
const connStatus = ref<ConnStatus>('disconnected')
let term: Terminal | null = null
let fitAddon: FitAddon | null = null
let ws: WebSocket | null = null

const assetOptions = ref<{ label: string; value: number }[]>([])

const statusLabel = computed(() => {
  if (connStatus.value === 'connected') return t('terminal.statusConnected')
  if (connStatus.value === 'error') return t('terminal.statusError')
  return t('terminal.statusDisconnected')
})

const statusType = computed(() => {
  if (connStatus.value === 'connected') return 'success'
  if (connStatus.value === 'error') return 'error'
  return 'default'
})

function terminalTheme() {
  return {
    background: isDark.value ? '#0f172a' : '#1e293b',
    foreground: '#e2e8f0',
    cursor: '#14b8a6',
  }
}

async function loadAssets() {
  const res = await listAssets()
  if (res.success && res.data) {
    assets.value = res.data.filter((a) => a.hasSshCredential)
    assetOptions.value = assets.value.map((a) => ({ label: `${a.name} (${a.host})`, value: a.id }))
    const paramId = Number(route.params.assetId)
    if (paramId) selectedAssetId.value = paramId
  }
}

function initTerminal() {
  if (!terminalRef.value) return
  term = new Terminal({ cursorBlink: true, fontSize: 14, theme: terminalTheme(), fontFamily: 'ui-monospace, monospace' })
  fitAddon = new FitAddon()
  term.loadAddon(fitAddon)
  term.open(terminalRef.value)
  fitAddon.fit()
  term.writeln(t('terminal.hintSelect'))
}

function connect() {
  if (!selectedAssetId.value || !term) return
  disconnect()
  const token = localStorage.getItem('accessToken')
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  const host = window.location.host
  const url = `${protocol}://${host}/ws/terminal?token=${token}&assetId=${selectedAssetId.value}`
  ws = new WebSocket(url)
  ws.onopen = () => {
    connStatus.value = 'connected'
    term?.writeln(`\r\n[CloudOps] ${t('terminal.statusConnected')}.\r\n`)
  }
  ws.onmessage = (e) => term?.write(e.data)
  ws.onclose = () => {
    connStatus.value = 'disconnected'
    term?.writeln(`\r\n[CloudOps] ${t('terminal.statusDisconnected')}.\r\n`)
  }
  ws.onerror = () => {
    connStatus.value = 'error'
    term?.writeln(`\r\n[CloudOps] ${t('terminal.statusError')}.\r\n`)
  }

  term.onData((data) => {
    if (ws?.readyState === WebSocket.OPEN) ws.send(data)
  })
}

function disconnect() {
  ws?.close()
  ws = null
  connStatus.value = 'disconnected'
}

watch(isDark, () => {
  if (term) term.options.theme = terminalTheme()
})

onMounted(async () => {
  await loadAssets()
  initTerminal()
})

onBeforeUnmount(() => {
  disconnect()
  term?.dispose()
})
</script>

<template>
  <NSpace vertical :size="16" class="terminal-page">
    <PageHeader :title="t('terminal.title')" :description="t('terminal.subtitle')">
      <template #extra>
        <NTag :type="statusType" size="small" round>{{ statusLabel }}</NTag>
      </template>
    </PageHeader>

    <NCard class="page-card terminal-card" :bordered="false">
      <NSpace class="terminal-toolbar" align="center" :size="12">
        <NSelect
          v-model:value="selectedAssetId"
          class="select-lg"
          :options="assetOptions"
          :placeholder="t('terminal.selectAsset')"
          clearable
          :aria-label="t('terminal.selectAsset')"
        />
        <NButton type="primary" :disabled="!selectedAssetId" @click="connect">{{ t('terminal.connect') }}</NButton>
      </NSpace>
      <div ref="terminalRef" class="terminal-container" />
    </NCard>
  </NSpace>
</template>

<style scoped>
.terminal-page {
  height: calc(100vh - var(--co-header-height) - var(--co-space-6) * 2);
  min-height: 420px;
}

.terminal-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.terminal-card :deep(.n-card__content) {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
}

.terminal-toolbar {
  margin-bottom: var(--co-space-3);
  flex-shrink: 0;
}

.terminal-container {
  flex: 1;
  min-height: 320px;
  border-radius: var(--co-radius);
  overflow: hidden;
  border: 1px solid var(--co-border);
}
</style>
