<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { NButton, NCard, NForm, NFormItem, NInput, NSpace, useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const { t } = useI18n()
const message = useMessage()
const authStore = useAuthStore()

const form = ref({
  username: 'admin',
  password: 'admin123',
})

const rules = computed(() => ({
  username: { required: true, message: t('common.username'), trigger: 'blur' },
  password: { required: true, message: t('common.password'), trigger: 'blur' },
}))

async function handleSubmit() {
  try {
    await authStore.login(form.value.username, form.value.password)
    await router.push({ name: 'dashboard' })
  } catch (error) {
    message.error(t('common.loginFailed'))
    console.error(error)
  }
}
</script>

<template>
  <div class="login-page">
    <NCard class="login-card" :title="t('auth.title')">
      <p class="subtitle">{{ t('auth.subtitle') }}</p>
      <NForm :model="form" :rules="rules" @submit.prevent="handleSubmit">
        <NFormItem path="username" :label="t('common.username')">
          <NInput v-model:value="form.username" autocomplete="username" />
        </NFormItem>
        <NFormItem path="password" :label="t('common.password')">
          <NInput
            v-model:value="form.password"
            type="password"
            show-password-on="click"
            autocomplete="current-password"
            @keyup.enter="handleSubmit"
          />
        </NFormItem>
        <NSpace vertical>
          <NButton type="primary" block :loading="authStore.loading" attr-type="submit" @click="handleSubmit">
            {{ t('common.login') }}
          </NButton>
          <p class="hint">{{ t('auth.defaultAccount') }}</p>
        </NSpace>
      </NForm>
    </NCard>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #0f766e 100%);
  padding: 24px;
}

.login-card {
  width: 100%;
  max-width: 420px;
}

.subtitle {
  margin: 0 0 20px;
  color: #64748b;
}

.hint {
  margin: 0;
  font-size: 12px;
  color: #94a3b8;
  text-align: center;
}
</style>
