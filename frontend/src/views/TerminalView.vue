<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { NButton, NCard, NSelect, NSpace } from 'naive-ui'
import { Terminal } from '@xterm/xterm'
import { FitAddon } from '@xterm/addon-fit'
import { listAssets, type Asset } from '@/api/assets'
import '@xterm/xterm/css/xterm.css'

const { t } = useI18n()
const route = useRoute()

const assets = ref<Asset[]>([])
const selectedAssetId = ref<number | null>(null)
const terminalRef = ref<HTMLDivElement | null>(null)
let term: Terminal | null = null
let fitAddon: FitAddon | null = null
let ws: WebSocket | null = null

const assetOptions = ref<{ label: string; value: number }[]>([])

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
  term = new Terminal({ cursorBlink: true, fontSize: 14, theme: { background: '#0f172a' } })
  fitAddon = new FitAddon()
  term.loadAddon(fitAddon)
  term.open(terminalRef.value)
  fitAddon.fit()
  term.writeln('[CloudOps] Select an asset and click Connect.')
}

function connect() {
  if (!selectedAssetId.value || !term) return
  disconnect()
  const token = localStorage.getItem('accessToken')
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  const host = window.location.host
  const url = `${protocol}://${host}/ws/terminal?token=${token}&assetId=${selectedAssetId.value}`
  ws = new WebSocket(url)
  ws.onopen = () => term?.writeln('\r\n[CloudOps] Connected.\r\n')
  ws.onmessage = (e) => term?.write(e.data)
  ws.onclose = () => term?.writeln('\r\n[CloudOps] Disconnected.\r\n')
  ws.onerror = () => term?.writeln('\r\n[CloudOps] Connection error.\r\n')

  term.onData((data) => {
    if (ws?.readyState === WebSocket.OPEN) ws.send(data)
  })
}

function disconnect() {
  ws?.close()
  ws = null
}

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
  <NCard :title="t('terminal.title')">
    <NSpace style="margin-bottom: 12px">
      <NSelect
        v-model:value="selectedAssetId"
        :options="assetOptions"
        :placeholder="t('terminal.selectAsset')"
        style="width: 320px"
        clearable
      />
      <NButton type="primary" :disabled="!selectedAssetId" @click="connect">{{ t('terminal.connect') }}</NButton>
    </NSpace>
    <div ref="terminalRef" class="terminal-container" />
  </NCard>
</template>

<style scoped>
.terminal-container {
  height: calc(100vh - 220px);
  border-radius: 6px;
  overflow: hidden;
}
</style>
