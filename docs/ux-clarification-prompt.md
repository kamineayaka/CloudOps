# ArchOps UX 澄清 — Cloud Agent Prompt

> 配套：[`ux-clarification-todo.md`](ux-clarification-todo.md)。  
> 复制对应代码块到 Cloud Agent。基线默认 `origin/main`。分支前缀 `cursor/`，后缀 `-71f3`。

---

## 硬约束（每次必带）

```text
硬约束：
- 思考深度分级保留：NONE/LOW/MEDIUM/HIGH/XHIGH/MAX（Anthropic 才允许 MAX）。
- 操作台 = Web 终端 + 右侧 AI 对话（≈ Cursor IDE）；Agent 窗口 = 左资产树 + 右对话，无 Web 终端。
- 禁止把「AI 侧轨」称作「Agent 窗口」。
- 不要用 Description 当 Architecture SSOT；不要明文打日志 API Key。
- 先读 docs/ux-clarification-todo.md，再改代码；完成后勾选该文件任务表。
```

---

## 一键：UX-AI 全波次

```text
仓库 ArchOps，基线 origin/main。
必读：docs/ux-clarification-todo.md「Wave UX-AI」；对照 OpsKat AIProviderForm（FetchAIModels + 选模型填默认 + GetModelDefaults）。

用户权威流程：
1) 填名称、API 地址、API Key →「获取模型」才可用；
2) 点击获取模型 → 模型框弹出列表；
3) 选中模型 → 下方 maxOutputTokens / contextWindow 自动填模型默认；
4) 思考深度仍用 ArchOps 分级，选模型不覆盖 reasoning。

请按 UX-AI-01→06 交付（可拆 PR）：
- UX-AI-01/02：表单与向导字段顺序 + canFetchModels 门控 + 拉模后列表；
- UX-AI-03：后端模型默认参数（fetch 响应字段和/或 model-defaults API + 单测）；
- UX-AI-04/05：选模型与手输 blur 自动填充 + toast；
- UX-AI-06：契约与文档勾选。

分支：cursor/ux-ai-provider-flow-71f3
验证：cd backend && ./mvnw verify ；cd frontend && npm run build
手工：三件套门控 → 获取 → 选模型参数变 → 改 reasoning → 保存 → 发消息。
不要重做 W3 已有字段；不要改 Architecture SSOT。
```

---

## 阶段 UX-AI-A — 门控与列表（UX-AI-01 + 02）

```text
仓库 ArchOps，基线 origin/main。
必读 docs/ux-clarification-todo.md UX-AI-01、UX-AI-02。

只改前端 AiProvidersView + AiProviderSetupWizard（及 i18n）：
- 字段顺序：类型 → 名称 → API 地址 → API Key → 获取模型 → 模型 → 参数区；
- canFetchModels：名称、地址、Key（编辑已存 Key 算满足）均有效才 enable；
- 获取成功后打开模型选项列表；失败 toast。

分支：cursor/ux-ai-01-02-fetch-gate-71f3
验证：npm run build；手工点门控。
不做后端 model-defaults（留给下一阶段）。
```

---

## 阶段 UX-AI-B — 默认参数 API + 自动填充（UX-AI-03…05）

```text
仓库 ArchOps，基线 origin/main（或已合入 UX-AI-01/02 的分支）。
必读 docs/ux-clarification-todo.md UX-AI-03…05；对照 OpsKat GetModelDefaults / AIModelInfo。

1) 后端：fetch models 返回可带 maxOutputTokens/contextWindow；新增 model-defaults 查询（内置表 + 未知空）。
2) 前端：选模型写入参数；手输 blur 调 defaults；toast；不覆盖 reasoningEffort。
3) 单测 + 契约短文。

分支：cursor/ux-ai-03-05-model-defaults-71f3
验证：./mvnw verify && npm run build
勾选 UX-AI-03…05；UX-AI-06 可同 PR 或随后。
```

---

## 一键：UX-LAYOUT 全波次

```text
仓库 ArchOps，基线 origin/main。
必读：docs/ux-clarification-todo.md「§2 产品结构」与「Wave UX-LAYOUT」。

纠正隐喻（必须写进代码文案与文档）：
- 操作台（原 IDE）：Web 终端主区 + 右侧 AI 对话 = Cursor/VS Code IDE。
- Agent 窗口：左侧资产（≈ Cursor Workspaces）+ 右侧 Agent 对话；页面内禁止 Web 终端。
- 操作台上的 AI 侧轨 ≠ Agent 窗口。

请按 UX-LAYOUT-01→06 交付（可拆 PR）：
1) 文档/i18n 纠偏（LAYOUT-01，可先合）；
2) 操作台命名与侧轨文案（LAYOUT-02）；
3) Agent 窗口壳：左 AssetNavTree + 右 Chat，无 xterm（LAYOUT-03）；
4) 导航并列入口 + 深链资产上下文（LAYOUT-04）；
5) 窄屏折叠与空态（LAYOUT-05）；
6) 验收勾选（LAYOUT-06）。

分支：cursor/ux-layout-ops-agent-windows-71f3
验证：npm run build；手工走 docs/ux-clarification-todo.md §5 双窗口剧本。
不要在 Agent 窗口加终端；不要大改 Architecture 页。
```

---

## 阶段 UX-LAYOUT-DOC — 仅文档纠偏（UX-LAYOUT-01）

```text
仓库 ArchOps，基线 origin/main。
必读 docs/ux-clarification-todo.md §2。

只改文档与文案键（可含 zh/en 导航字符串若已有错误隐喻）：
- docs/workbench-gap-audit.md §4 与结论第 4 条；
- docs/mainline-implementation-prompt.md 中错误的「Agent = Agent Window / 侧轨」表述；
- 确保与 ux-clarification-todo.md 一致。

分支：cursor/ux-layout-01-docs-71f3
验证：文档互链正确；无代码行为变更亦可。
勾选 UX-LAYOUT-01。
```

---

## 阶段 UX-LAYOUT-SHELL — Agent 窗口壳（UX-LAYOUT-03 + 04）

```text
仓库 ArchOps，基线 origin/main（建议已合 LAYOUT-01/02）。
必读 docs/ux-clarification-todo.md UX-LAYOUT-03、04。

实现 Agent 窗口：
- 路由：改造 /ai 或新增 /agent（二选一，导航只保留一个主入口）；
- 布局：左资产树（Workspaces）+ 右现有 Chat；禁止引入 Terminal/xterm；
- 选中资产写入对话上下文（复用现有 target/uiContext 若有）；
- 操作台保留 /terminal + 右侧 AI 轨；轨上文案不是「Agent 窗口」；
- 可选：操作台「在 Agent 中打开」跳转并带 assetId。

分支：cursor/ux-layout-03-04-agent-window-71f3
验证：npm run build；Agent 页无终端；操作台仍有终端+侧轨。
```

---

## 并行派工（两 Agent）

```text
Agent A：只做 docs/ux-clarification-todo.md Wave UX-AI（Provider 流程）。
Agent B：只做 Wave UX-LAYOUT（操作台/Agent 窗口）；先合 LAYOUT-01 文档，再改壳。
禁止两人同时改 AiProvidersView 与 Agent 布局以外的同一文件。
二者都必读 ux-clarification-todo.md §0–§2；完成后勾选任务表并更新变更记录。
```

---

## 单任务模板

```text
仓库 ArchOps，基线 origin/main。
请仅执行 docs/ux-clarification-todo.md 中的 {TASK_ID}（{TITLE}）。
先读该任务「实现要点/完成标准」与 §0–§2 隐喻/流程。
分支：cursor/ux-{id-小写}-71f3
Commit：feat(ux): {TASK_ID} 简短说明
验证：按任务需要 ./mvnw verify 和/或 npm run build
勾选清单；Draft PR。
附带硬约束见 docs/ux-clarification-prompt.md。
```

---

## 变更记录

| 日期 | 说明 |
|------|------|
| 2026-07-24 | 初版：UX-AI / UX-LAYOUT 一键与分阶段 Prompt |
