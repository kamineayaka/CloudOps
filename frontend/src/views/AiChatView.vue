<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { NButton, NCard, NInput, NSelect, NSpace, NSpin, useMessage } from 'naive-ui'
import { createConversation, getMessages, sendChat, type ChatMessage } from '@/api/ai'
import { listChatProviders, type AiProvider } from '@/api/ai-providers'
import EmptyState from '@/components/EmptyState.vue'
import PageHeader from '@/components/PageHeader.vue'
import { renderChatContent } from '@/utils/format'

const { t } = useI18n()
const message = useMessage()

const conversationId = ref<number | null>(null)
const input = ref('')
const loading = ref(false)
const messages = ref<ChatMessage[]>([])
const providers = ref<AiProvider[]>([])
const selectedProviderId = ref<number | undefined>(undefined)
const chatBottomRef = ref<HTMLDivElement | null>(null)

const providerOptions = computed(() =>
  providers.value.map((p) => ({
    label: p.defaultChat ? `${p.name} (${t('ai.providerDefault')})` : p.name,
    value: p.id,
  })),
)

function messageKey(msg: ChatMessage, index: number) {
  return `${msg.createdAt ?? index}-${msg.role}-${index}`
}

function roleLabel(role: string) {
  return role === 'user' ? t('ai.roleUser') : t('ai.roleAssistant')
}

async function loadProviders() {
  const res = await listChatProviders()
  if (res.success && res.data) {
    providers.value = res.data
  }
}

async function ensureConversation() {
  if (conversationId.value) return
  const res = await createConversation()
  if (res.success && res.data) conversationId.value = res.data.id
}

async function loadMessages() {
  if (!conversationId.value) return
  const res = await getMessages(conversationId.value)
  if (res.success && res.data) messages.value = res.data
}

async function scrollToBottom() {
  await nextTick()
  chatBottomRef.value?.scrollIntoView({ behavior: 'smooth' })
}

async function handleSend() {
  if (!input.value.trim() || loading.value) return
  loading.value = true
  const userMsg = input.value
  input.value = ''
  messages.value.push({ role: 'user', content: userMsg, createdAt: new Date().toISOString() })
  await scrollToBottom()
  try {
    await ensureConversation()
    const res = await sendChat(
      userMsg,
      conversationId.value ?? undefined,
      selectedProviderId.value ?? undefined,
    )
    if (res.success && res.data) {
      conversationId.value = res.data.conversationId
      messages.value.push({ role: 'assistant', content: res.data.answer, createdAt: new Date().toISOString() })
    }
  } catch {
    message.error(t('ai.requestFailed'))
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

async function handleNewChat() {
  conversationId.value = null
  messages.value = []
  await ensureConversation()
}

watch(messages, () => scrollToBottom(), { deep: true })

onMounted(async () => {
  await loadProviders()
  await ensureConversation()
  await loadMessages()
})
</script>

<template>
  <div class="chat-page">
    <PageHeader :title="t('ai.title')" :description="t('ai.subtitle')">
      <template #extra>
        <NSpace align="center" :size="12">
          <NSelect
            v-model:value="selectedProviderId"
            class="select-md"
            :options="providerOptions"
            :placeholder="t('ai.provider')"
            clearable
            :aria-label="t('ai.provider')"
          />
          <NButton @click="handleNewChat">{{ t('ai.newChat') }}</NButton>
        </NSpace>
      </template>
    </PageHeader>

    <NCard class="chat-card page-card" :bordered="false">
      <div class="chat-messages" aria-live="polite">
        <EmptyState
          v-if="!loading && messages.length === 0"
          :message="t('ai.emptyTitle')"
          :hint="t('ai.emptyHint')"
        />
        <div
          v-for="(msg, i) in messages"
          :key="messageKey(msg, i)"
          class="chat-row"
          :class="msg.role"
        >
          <div class="chat-bubble">
            <span class="chat-bubble__role">{{ roleLabel(msg.role) }}</span>
            <!-- eslint-disable-next-line vue/no-v-html -->
            <div class="chat-bubble__content" v-html="renderChatContent(msg.content)" />
          </div>
        </div>
        <div v-if="loading" class="chat-loading">
          <NSpin size="small" />
          <span>{{ t('ai.sending') }}</span>
        </div>
        <div ref="chatBottomRef" />
      </div>

      <div class="chat-input">
        <NInput
          v-model:value="input"
          type="textarea"
          :placeholder="t('ai.placeholder')"
          :autosize="{ minRows: 2, maxRows: 5 }"
          class="chat-input__field"
          @keyup.ctrl.enter="handleSend"
        />
        <div class="chat-input__actions">
          <span class="chat-input__hint">{{ t('ai.sendHint') }}</span>
          <NButton type="primary" :loading="loading" :disabled="!input.trim()" @click="handleSend">
            {{ t('ai.send') }}
          </NButton>
        </div>
      </div>
    </NCard>
  </div>
</template>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - var(--co-header-height) - var(--co-space-6) * 2);
  min-height: 480px;
}

.chat-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.chat-card :deep(.n-card__content) {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  padding-top: 0 !important;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: var(--co-space-4) 0;
  min-height: 0;
}

.chat-row {
  display: flex;
  margin-bottom: var(--co-space-4);
}

.chat-row.user {
  justify-content: flex-end;
}

.chat-row.assistant {
  justify-content: flex-start;
}

.chat-bubble {
  max-width: min(85%, 640px);
  padding: var(--co-space-3) var(--co-space-4);
  border-radius: var(--co-radius-lg);
  border: 1px solid var(--co-border);
}

.chat-row.user .chat-bubble {
  background: rgba(15, 118, 110, 0.12);
  border-color: rgba(15, 118, 110, 0.25);
}

.chat-row.assistant .chat-bubble {
  background: var(--co-bg-page);
}

.chat-bubble__role {
  display: block;
  font-size: 0.6875rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--co-text-muted);
  margin-bottom: var(--co-space-2);
}

.chat-bubble__content {
  font-size: 0.875rem;
  line-height: 1.6;
  color: var(--co-text);
  word-break: break-word;
}

.chat-loading {
  display: flex;
  align-items: center;
  gap: var(--co-space-2);
  padding: var(--co-space-2) 0;
  font-size: 0.8125rem;
  color: var(--co-text-secondary);
}

.chat-input {
  border-top: 1px solid var(--co-border);
  padding-top: var(--co-space-4);
  margin-top: auto;
}

.chat-input__field {
  width: 100%;
}

.chat-input__actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: var(--co-space-3);
  gap: var(--co-space-4);
}

.chat-input__hint {
  font-size: 0.75rem;
  color: var(--co-text-muted);
}
</style>
