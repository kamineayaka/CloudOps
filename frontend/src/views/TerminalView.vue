<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { NButton, NCard, NSelect, NSpace, NTag, useMessage } from 'naive-ui'
import { Terminal } from '@xterm/xterm'
import { FitAddon } from '@xterm/addon-fit'
import { listAssets, type Asset } from '@/api/assets'
import { listSshPool, warmSshPool, type SshPoolEntry } from '@/api/sshPool'
import PageHeader from '@/components/PageHeader.vue'
import { useTheme } from '@/composables/useTheme'
import '@xterm/xterm/css/xterm.css'

const { t } = useI18n()
const route = useRoute()
const message = useMessage()
const { isDark } = useTheme()

type ConnStatus = 'disconnected' | 'connected' | 'error'

const assets = ref<Asset[]>([])
const poolEntries = ref<SshPoolEntry[]>([])
const selectedAssetId = ref<number | null>(null)
const connecting = ref(false)
const terminalRef = ref<HTMLDivElement | null>(null)
const connStatus = ref<ConnStatus>('disconnected')
let term: Terminal | null = null
let fitAddon: FitAddon | null = null
let ws: WebSocket | null = null
let resizeObserver: ResizeObserver | null = null

const assetOptions = ref<{ label: string; value: number }[]>([])

function isPooled(assetId: number) {
  return poolEntries.value.some((e) => e.assetId === assetId && e.alive)
}

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

async function refreshPool() {
  const res = await listSshPool()
  if (res.success && res.data) {
    poolEntries.value = res.data
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

  resizeObserver = new ResizeObserver(() => {
    fitAddon?.fit()
    sendResize()
  })
  resizeObserver.observe(terminalRef.value)
}

function sendResize() {
  if (!ws || ws.readyState !== WebSocket.OPEN || !term) return
  ws.send(JSON.stringify({ type: 'resize', cols: term.cols, rows: term.rows }))
}

async function connect() {
  if (!selectedAssetId.value || !term) return
  connecting.value = true
  try {
    await warmSshPool(selectedAssetId.value)
    await refreshPool()
    disconnect()
    const token = localStorage.getItem('accessToken')
    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
    const host = window.location.host
    const url = `${protocol}://${host}/ws/terminal?token=${token}&assetId=${selectedAssetId.value}`
    ws = new WebSocket(url)
    ws.onopen = () => {
      connStatus.value = 'connected'
      fitAddon?.fit()
      sendResize()
      term?.writeln(`\r\n[ArchOps] ${t('terminal.statusConnected')} (${t('terminal.pooled')}).\r\n`)
    }
    ws.onmessage = (e) => term?.write(e.data)
    ws.onclose = () => {
      connStatus.value = 'disconnected'
      term?.writeln(`\r\n[ArchOps] ${t('terminal.statusDisconnected')}.\r\n`)
      refreshPool()
    }
    ws.onerror = () => {
      connStatus.value = 'error'
      term?.writeln(`\r\n[ArchOps] ${t('terminal.statusError')}.\r\n`)
    }

    term.onData((data) => {
      if (ws?.readyState === WebSocket.OPEN) ws.send(data)
    })
  } catch {
    connStatus.value = 'error'
    message.error(t('terminal.connectFailed'))
  } finally {
    connecting.value = false
  }
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
  await Promise.all([loadAssets(), refreshPool()])
  initTerminal()
})

onBeforeUnmount(() => {
  disconnect()
  resizeObserver?.disconnect()
  term?.dispose()
})
</script>

<template>
  <NSpace vertical :size="16" class="terminal-page">
    <PageHeader :title="t('terminal.title')" :description="t('terminal.subtitle')">
      <template #extra>
        <NSpace align="center" :size="8">
          <NTag :type="statusType" size="small" round>{{ statusLabel }}</NTag>
          <NTag v-if="selectedAssetId && isPooled(selectedAssetId)" type="success" size="small" round>
            {{ t('terminal.pooled') }}
          </NTag>
        </NSpace>
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
        <NButton type="primary" :disabled="!selectedAssetId" :loading="connecting" @click="connect">
          {{ t('terminal.connect') }}
        </NButton>
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
