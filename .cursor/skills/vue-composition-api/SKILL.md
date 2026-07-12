---
name: vue-composition-api
description: Vue 3 Composition API 编码规范。在新建或重构 .vue 组件、抽取 composables、定义 props/emits 类型时自动应用。
paths:
  - "frontend/**/*.vue"
  - "frontend/**/*.ts"
---

# Vue Composition API

## 组件结构

一律使用 `<script setup lang="ts">`，按以下顺序组织：

```vue
<script setup lang="ts">
// 1. imports
// 2. props / emits
// 3. composables / stores
// 4. reactive state
// 5. computed
// 6. watch
// 7. lifecycle hooks
// 8. methods / handlers
</script>

<template>...</template>

<style scoped>...</style>
```

## 类型安全

```ts
// props
const props = defineProps<{
  title: string
  loading?: boolean
}>()

// emits
const emit = defineEmits<{
  submit: [value: string]
  cancel: []
}>()
```

## Composables 抽取

将可复用的异步逻辑抽到 `src/composables/`：

| Composable | 用途 |
|---|---|
| `useAssets.ts` | 资产列表 CRUD |
| `useChat.ts` | AI 对话状态与发送 |
| `useTheme.ts` | 暗色模式切换 |
| `useLocale.ts` | 语言切换封装 |

命名规则：`use` + 功能名，返回 reactive 状态与方法。

## 表格列定义

复杂 `render` 函数可：

1. 抽成独立函数放在 `<script setup>` 底部
2. 或拆成小组件（如 `AssetActions.vue`）

避免在 `render` 中写超过 5 行的内联逻辑。

## 样式规范

- 避免在 template 写复杂内联 `style`，用 class 或 CSS 变量
- 使用 `scoped` 样式，全局样式仅放 `style.css`
- 颜色引用 CSS 变量（`var(--co-primary)`），不硬编码色值
- 响应式优先用 Naive UI 的 `responsive` 属性（如 `NGrid`）

## 性能注意

- 大列表用 `NDataTable` 自带分页，不一次渲染全部
- `v-for` 使用稳定 `:key`（优先 `id`，不用数组索引）
- 路由级页面用 `onMounted` 加载数据，避免在 setup 顶层直接 await

## 禁止事项

- 不使用 Options API（`export default { data, methods }`）
- 不在组件中直接操作 DOM（`document.getElementById`），用 `ref` + `template ref`
- 不在 `src/views/` 中写可复用逻辑，抽到 composables 或 components
