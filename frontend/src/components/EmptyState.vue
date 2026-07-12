<script setup lang="ts">
defineProps<{
  message: string
  hint?: string
}>()
</script>

<template>
  <div class="empty-state" role="status">
    <p class="empty-state__message">{{ message }}</p>
    <p v-if="hint" class="empty-state__hint">{{ hint }}</p>
    <div v-if="$slots.action" class="empty-state__action">
      <slot name="action" />
    </div>
  </div>
</template>

<style scoped>
.empty-state {
  padding: var(--co-space-8) var(--co-space-4);
  text-align: center;
}

.empty-state__message {
  margin: 0;
  font-size: 0.9375rem;
  color: var(--co-text-secondary);
}

.empty-state__hint {
  margin: var(--co-space-2) 0 0;
  font-size: 0.8125rem;
  color: var(--co-text-muted);
}

.empty-state__action {
  margin-top: var(--co-space-4);
}
</style>
