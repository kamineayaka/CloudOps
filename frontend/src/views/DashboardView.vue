<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NDescriptions, NDescriptionsItem, NGrid, NGridItem, NSpace, NStatistic, NTag } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'

const { t } = useI18n()
const authStore = useAuthStore()
const user = computed(() => authStore.user)
</script>

<template>
  <NSpace vertical size="large">
    <NCard :title="t('dashboard.title')">
      <p>{{ t('dashboard.description') }}</p>
    </NCard>

    <NGrid :cols="2" :x-gap="16" :y-gap="16" responsive="screen">
      <NGridItem>
        <NCard :title="t('dashboard.profile')">
          <NDescriptions v-if="user" :column="1" bordered>
            <NDescriptionsItem :label="t('common.username')">{{ user.username }}</NDescriptionsItem>
            <NDescriptionsItem :label="t('dashboard.rbacTier')">
              <NTag type="info">{{ user.rbacTier }}</NTag>
            </NDescriptionsItem>
            <NDescriptionsItem :label="t('dashboard.approvalPolicy')">
              <NTag type="warning">{{ user.approvalPolicy }}</NTag>
            </NDescriptionsItem>
            <NDescriptionsItem :label="t('dashboard.roles')">
              <NSpace>
                <NTag v-for="role in user.roles" :key="role" type="success">{{ role }}</NTag>
              </NSpace>
            </NDescriptionsItem>
          </NDescriptions>
        </NCard>
      </NGridItem>
      <NGridItem>
        <NCard :title="t('dashboard.modulesTitle')">
          <NSpace>
            <NStatistic :label="t('dashboard.modules.assets')" value="✓" />
            <NStatistic :label="t('dashboard.modules.ai')" value="✓" />
            <NStatistic :label="t('dashboard.modules.terminal')" value="✓" />
            <NStatistic :label="t('dashboard.modules.approvals')" value="✓" />
            <NStatistic :label="t('dashboard.modules.audit')" value="✓" />
            <NStatistic :label="t('dashboard.modules.knowledge')" value="✓" />
          </NSpace>
        </NCard>
      </NGridItem>
    </NGrid>
  </NSpace>
</template>
