---
name: naive-ui-theme
description: Naive UI 主题与设计系统规范。在配置全局主题、暗色模式、色彩、间距、圆角、阴影或美化 AppLayout/LoginView 等页面样式时自动应用。
paths:
  - "frontend/**"
---

# Naive UI Theme & Design System

## 品牌色

| 用途 | 色值 |
|---|---|
| Primary | `#0f766e` (teal-700，与登录页、侧边栏 brand 一致) |
| 页面背景 | `#f8fafc` |
| 卡片背景 | `#ffffff` |
| 主文字 | `#0f172a` |
| 次要文字 | `#64748b` |
| 边框 | `#e2e8f0` |

暗色模式参考：

| 用途 | 色值 |
|---|---|
| 页面背景 | `#0f172a` |
| 卡片背景 | `#1e293b` |
| 主文字 | `#f1f5f9` |
| 次要文字 | `#94a3b8` |

## 实现方式

在 `App.vue` 的 `NConfigProvider` 中配置 `themeOverrides`：

```ts
const themeOverrides = {
  common: {
    primaryColor: '#0f766e',
    primaryColorHover: '#0d9488',
    primaryColorPressed: '#0f766e',
    borderRadius: '8px',
  },
  Card: { borderRadius: '12px' },
  Button: { borderRadius: '6px' },
}
```

在 `style.css` 中定义 CSS 变量，供 scoped 样式引用：

```css
:root {
  --co-primary: #0f766e;
  --co-radius: 8px;
  --co-spacing: 16px;
  --co-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}
```

## 暗色模式

1. 用 `NConfigProvider` 的 `theme` 属性切换 `light` / `dark`
2. 在 `AppLayout.vue` 顶栏添加切换按钮
3. `TerminalView` 中 xterm `theme.background` 随模式联动
4. 登录页可保持固定渐变，不受暗色模式影响

## 间距系统

| 场景 | 值 |
|---|---|
| 页面内边距 | `24px` |
| 卡片间距 | `16px` |
| 圆角（卡片） | `12px` |
| 圆角（按钮/输入框） | `6px` |
| 顶栏高度 | `56px` |
| 侧边栏宽度 | `220px` |

## 设计方向

- 风格：简洁、专业的 DevOps / 云原生控制台
- 参考：Grafana、Vercel Dashboard 的信息密度与留白
- 统一圆角、阴影、间距，消除各页面风格割裂感
- 表格页统一布局：PageHeader + 操作栏 + 表格 + EmptyState

## 推荐抽取的公共组件

- `PageHeader.vue` — 标题 + 描述 + 操作区
- `EmptyState.vue` — 空数据占位
- `StatusTag.vue` — 风险等级、审批状态等标签
