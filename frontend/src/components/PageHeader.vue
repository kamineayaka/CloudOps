<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<template>
  <header class="page-header">
    <div class="page-header__text">
      <h1 class="page-header__title">{{ title }}</h1>
      <p v-if="description" class="page-header__desc">{{ description }}</p>
    </div>
    <div v-if="$slots.extra" class="page-header__extra">
      <slot name="extra" />
    </div>
  </header>
</template>

<style scoped>
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--co-space-4);
  margin-bottom: var(--co-space-4);
}

.page-header__title {
  margin: 0;
  font-size: 1.375rem;
  font-weight: 600;
  line-height: 1.3;
  color: var(--co-text);
  text-wrap: balance;
}

.page-header__desc {
  margin: var(--co-space-2) 0 0;
  font-size: 0.875rem;
  line-height: 1.5;
  color: var(--co-text-secondary);
  max-width: 52rem;
}

.page-header__extra {
  flex-shrink: 0;
}

@media (max-width: 640px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
