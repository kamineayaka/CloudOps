---
name: cloudops-frontend
description: CloudOps 前端开发规范。在修改 frontend/ 目录下的 Vue 页面、布局、组件、样式或 i18n 时自动应用。约束技术栈选型、目录约定、API 契约与国际化要求。
paths:
  - "frontend/**"
---

# CloudOps Frontend

## 技术栈（不可更换）

- Vue 3 Composition API + `<script setup lang="ts">`
- Naive UI 组件库（不引入 Element Plus / Ant Design Vue）
- Pinia 状态管理
- vue-i18n 国际化（zh-CN / en-US 必须同步更新）
- Vite 构建
- 图标使用 `@vicons/ionicons5`

## 目录约定

| 路径 | 用途 |
|---|---|
| `src/views/` | 页面级组件 |
| `src/layouts/` | 布局 |
| `src/components/` | 可复用 UI 组件（优先抽取） |
| `src/api/` | API 调用（不要改接口契约） |
| `src/locales/` | 中英文文案 |
| `src/stores/` | Pinia 状态 |
| `src/router/` | 路由配置 |

## 硬性约束

1. 不修改后端 API 路径与响应结构
2. 所有用户可见文案走 `t('key')`，禁止硬编码中文/英文
3. 保持 `npm run build` 通过
4. 不引入重量级 UI 框架
5. 样式优先用 Naive UI 主题变量，其次 scoped CSS
6. 单文件组件不超过 300 行，超出则拆分到 `src/components/` 或 composables

## 页面清单

| 页面 | 文件 | 说明 |
|---|---|---|
| 登录 | `LoginView.vue` | 公开页，无 AppLayout |
| 控制台 | `DashboardView.vue` | 首页概览 |
| 资产管理 | `AssetsView.vue` | CRUD + SSH 凭证 |
| AI 对话 | `AiChatView.vue` | 聊天界面 |
| Web 终端 | `TerminalView.vue` | xterm.js + WebSocket |
| 审批中心 | `ApprovalsView.vue` | 待审批列表 |
| 审计日志 | `AuditView.vue` | 哈希链校验 |
| AI 设置 | `AiProvidersView.vue` | 仅 ADMIN 可见 |

## 开发流程

1. 先阅读目标页面与相关 `src/api/` 文件，理解现有逻辑
2. 优先复用 Naive UI 组件，避免重复造轮子
3. 抽取可复用部分到 `src/components/`
4. 修改文案时同步更新 `src/locales/zh-CN.ts` 与 `en-US.ts`
5. 完成后运行 `npm run build` 确认无 TypeScript 错误
