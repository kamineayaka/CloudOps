<script setup lang="ts">
import { computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  NConfigProvider,
  NDialogProvider,
  NMessageProvider,
  NNotificationProvider,
  darkTheme,
  dateEnUS,
  dateZhCN,
  enUS,
  zhCN,
} from 'naive-ui'
import { RouterView } from 'vue-router'
import { useTheme } from '@/composables/useTheme'
import { darkThemeOverrides, lightThemeOverrides } from '@/theme/overrides'

const { locale } = useI18n()
const { isDark } = useTheme()

const naiveLocale = computed(() => (locale.value === 'en-US' ? enUS : zhCN))
const naiveDateLocale = computed(() => (locale.value === 'en-US' ? dateEnUS : dateZhCN))
const themeOverrides = computed(() => (isDark.value ? darkThemeOverrides : lightThemeOverrides))

watch(
  isDark,
  (dark) => {
    document.documentElement.classList.toggle('dark', dark)
  },
  { immediate: true },
)
</script>

<template>
  <NConfigProvider
    :locale="naiveLocale"
    :date-locale="naiveDateLocale"
    :theme="isDark ? darkTheme : undefined"
    :theme-overrides="themeOverrides"
  >
    <NMessageProvider>
      <NDialogProvider>
        <NNotificationProvider>
          <RouterView />
        </NNotificationProvider>
      </NDialogProvider>
    </NMessageProvider>
  </NConfigProvider>
</template>
