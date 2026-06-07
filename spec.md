# 选课系统前端设计规范（Spec）

## 概述
- 目的：为基于 `shadcn/ui` + `Tailwind` 的大学生选课系统提供可执行的 UI 设计规范。侧重轻量的 side-rail 风格仪表盘、组件可复用性、响应式与多端适配。
- 风格基调：扁平、简洁、中性色调 + 品牌色点缀；侧轨（Side Rail）采用浅灰背景以弱化视觉权重；主卡片突出内容。
- 图表：使用 ECharts，side-rail 使用迷你图（sparkline/mini bar），详情页使用完整图表。

---

## 设计代号与色彩（Design Tokens）
```
--color-primary: #6366F1;  /* indigo-500/600 */
--color-success: #10B981;  /* emerald-500 */
--color-warning: #F59E0B;  /* amber-500 */
--color-danger:  #EF4444;  /* red-500 */
--color-bg:      #F3F4F6;
--color-surface: #FFFFFF;
--color-muted:   #6B7280;
--radius-sm: 6px;
--radius-md:10px;
--radius-lg:14px;
```
- 间距以 4px 基数使用 Tailwind spacing。
- 阴影使用 Tailwind 的 `shadow-sm` / `shadow` / `shadow-lg`。

---

## 排版
- 主字体：系统 UI stack（`font-sans`）。
- 字号体系（示意）：`xs(12)`, `sm(14)`, `md(16)`, `lg(18)`, `xl(20+)`。
- 行高：正文 1.5，标题 1.25。

---

## 断点与布局
- 使用 Tailwind 默认断点：`sm` (640px), `md` (768px), `lg` (1024px), `xl` (1280px)。
- 桌面：主内容（2/3 或 3/4）+ 侧轨（1/3 或 1/4）。
- 平板：侧轨折叠为可展开面板或抽屉。移动：侧轨隐藏，提供底部浮动触发。
- 卡片响应：`sm` 单列、`md` 两列、`lg` 三列。

---

## 组件语言与命名
- 统一使用 shadcn/ui 风格语义：`Card`, `Badge`, `Button`, `Dialog`, `Toast`, `ChartCard`, `SideRail`。
- 语义类名（Tailwind 为主）: `card`, `card-rail`, `badge`, `btn`, `dialog`, `toast`, `chart-card`。
- 组件目录结构建议：
  - `/src/components/ui/` — 通用 UI 组件（Card, Button, Badge, Dialog, Toast）
  - `/src/components/layout/` — Header, Footer, SideRail, Grid
  - `/src/components/charts/` — ChartCard, MiniSparkline
  - `/src/components/course/` — CourseCard, CourseList, Timetable

---

## 主要组件规范
### Card
- 用途：承载主内容。样式：`bg-white rounded-lg shadow-sm p-4`。
- 变体：`card-compact`（紧凑）、`card-rail`（浅灰背景，内边距小）。
- Props（示意）：`title`, `subTitle`, `actions`（slot）, `compact:Boolean`。

### Badge
- 用途：状态或类别标识（必修/选修/已满）。样式：`inline-flex px-2 py-0.5 rounded-full text-xs font-medium`。

### Button
- 变体：`primary`（CTA）、`secondary`（outline）、`ghost`（minimal）。
- 状态：`hover`、`active`、`disabled`。需提供 `aria-label`。

### Dialog
- 用途：冲突提示、确认、重要表单。遮罩：`bg-black/40`。要求支持键盘 `Esc` 关闭并管理焦点。

### Toast
- 用途：短提示，位置：右上（桌面）/底部（移动）。默认 3-5 秒自动关闭，支持手动关闭。

### Form Controls
- 所有输入需有明确标签与辅助提示（`aria-describedby`），错误提示近控件展示并使用 `aria-invalid`。

### Side Rail（统计侧轨）
- 风格：`card-rail` 组块，浅灰背景，内置小卡片与迷你图表。
- 交互：可展开详情（右侧抽屉或 Dialog），移动端折叠。

### Chart Card
- 侧轨小图：隐藏轴线、图例，保持简洁（sparkline/mini bar）。
- 详情图：启用 tooltip、legend、toolbox（导出/缩放）。
- 响应：容器大小变化时调用 `chart.resize()`。

### CourseCard
- 必含信息：课程名、教师、时间/地点、学分、剩余名额、选课按钮。
- 主要动作：`选课`（primary）、`详情`（secondary）。
- 冲突：点击选课时如检测到冲突触发 `Dialog`，显示冲突课程和建议方案。

---

## 交互与动效
- 过渡时长：120–200ms。
- 列表加载优先使用 Skeleton，而非 spinner。
- 错误：字段级错误在字段下方，系统/网络错误使用 `Toast` 或 `Dialog`。
- 无障碍：键盘可达、焦点管理、文本替代（图表数据表导出）。颜色对比 ≥ 4.5:1（正文）。

---

## ECharts 使用细则
- 侧轨小图：禁用轴与网格，使用简洁的 `area` 或 `line`。
- 详情图：启用 tooltip、legend、toolbox（导出、saveAsImage）。
- 初始化：避免在隐藏容器初始化图表，显示时再渲染或调用 `resize()`。
- 颜色：使用 design tokens，支持深/浅主题切换。

---

## Tailwind 配置建议
- 在 `tailwind.config.js` 中扩展 `colors`（`primary`/`success` 等）、`borderRadius`、并启用 JIT 模式。
- 将 shadcn/ui 组件与 Tailwind tokens 映射一致。

---

## 可复用性与模块化
- 将通用组件放在 `/src/components/ui`，业务组件通过组合实现。
- 数据层统一在 `/src/api` 管理，使用 Fetch/Axios 封装错误与重试策略。
- 前端校验为第一道防线，关键校验（如选课冲突）以后端为最终判定。

---

## 测试与性能
- 测试：为关键组件（CourseCard、冲突 Dialog、Chart 渲染）写单元测试；E2E 流程覆盖登录→选课→查看成绩。
- 性能：列表分页/虚拟滚动；图表按需加载；避免同时渲染大量图表。

---

## 交付清单（建议）
- `spec.md`（本文件）
- 基础 UI 组件实现（Vue）: `Card`, `Button`, `Dialog`, `Toast`, `Badge`
- 业务组件示例：`CourseCard.vue`, `SideRail.vue`, `ChartCard.vue`
- Tailwind 配置片段与 ECharts 初始化示例
- 轻量 Demo 页面（Vue 单文件组件）

---

## 下一步建议（工作流）
1. 若你确认本规范，我将把规范写入仓库（已写入）。
2. 生成基础 `ui` 组件的 Vue 初版（`Card`, `Button`, `Dialog`, `Toast`），并提供 `CourseCard` 与 `SideRail` 示例组件。
3. 将示例集成到一个小型 demo 页面，并提供运行说明。

如需调整配色/断点/语义命名，请指出具体项，我会在生成组件前先更新 `spec.md`。