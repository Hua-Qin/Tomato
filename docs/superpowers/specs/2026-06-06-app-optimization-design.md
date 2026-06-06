# 应用功能优化与UI改进设计文档

日期: 2026-06-06

## 概述

对 Tomato 番茄钟应用进行5大模块的功能优化和问题修复，采用渐进式增强方案（方案A），在现有架构上扩展，最大程度复用已有代码。

## 技术决策

- **图表库**: Vico (Compose 原生图表库)
- **统计维度**: 本日 + 本周 + 本月
- **收集布局**: 双列网格 (`LazyVerticalGrid(GridCells.Fixed(2))`)
- **时间编辑**: 修复 `TimeInput` 居中显示

---

## 1. 数据统计功能修复与增强

### 问题

- `StatisticsTab` 是占位实现，硬编码 "0m" / "0"
- `RecordsViewModel` 未加载统计数据
- `TimerSessionDao` 已有 `getDurationByTimerName` 和 `getSessionsBetweenDates` 查询但未使用
- 今日记录数据不实时更新

### 修改文件

#### `RecordsState.kt`

扩展 `StatsPeriod` 枚举和 `RecordsState` 数据类：

```kotlin
enum class StatsPeriod { DAY, WEEK, MONTH }

data class RecordsState(
    // ... 现有字段
    val todayTotalFocus: Long = 0L,
    val todaySessionCount: Int = 0,
    val timerDurationStats: List<TimerDurationStat> = emptyList(),
    val periodSessions: List<TimerSession> = emptyList(),
    val todayStat: Stat? = null
)
```

#### `RecordsViewModel.kt`

在 `init` 的 `combine` 中新增数据源：

- `timerSessionRepository.getDurationByTimerName(today, today)` — 今日各计划时长
- `statRepository.getTodayStat()` — 今日 Stat 记录（含 Q1-Q4 分布）
- `timerSessionRepository.getSessionsBetweenDates(startDate, endDate)` — 周期内会话（根据 `statsPeriod` 动态计算日期范围）
- `timerSessionRepository.getSessionCountByDate(today)` — 今日完成次数

当 `statsPeriod` 变化时，重新计算 `startDate`/`endDate` 并更新 `periodSessions`。

#### `RecordsAction.kt`

无需新增 action，`SetStatsPeriod` 已存在。

#### `RecordsScreen.kt` — `StatisticsTab` 重写

布局结构：

1. **顶部**: `SingleChoiceSegmentedButtonRow` — "本日 / 本周 / 本月" 三段切换
2. **今日概览卡片**: 显示今日总专注时长 + 完成次数
3. **各计划今日时长列表**: `timerDurationStats` 渲染为 "计划名，今日 Xh Xm"
4. **时长统计图表**: Vico 柱状图，X轴为日期，Y轴为专注时长（分钟）
5. **次数统计图表**: Vico 柱状图，X轴为日期，Y轴为完成次数
6. **最高记录**: 从 `periodSessions` 找 `actualDuration` 最大值，显示 "最高记录: Xh Xm (MM月dd日 HH:mm)"

图表数据来源：
- "本日": 按小时分组（使用 Stat 的 Q1-Q4 分布）
- "本周": 按天分组（7天）
- "本月": 按天分组（当月天数）

### 依赖

- 新增 Vico 图表库依赖 (`com.patrykandpatrick.vico:compose-m3`)

---

## 2. 首页UI优化

### 修改文件

#### `TasksScreen.kt`

- `SmallFloatingActionButton` → `FloatingActionButton`
- 图标尺寸: `24.dp` → `32.dp`

#### `CollectionScreen.kt`

- `SmallFloatingActionButton` → `FloatingActionButton`
- 图标尺寸: `24.dp` → `32.dp`

---

## 3. 收集页面优化

### 修改文件

#### `CollectionScreen.kt`

将笔记列表从 `LazyColumn` 改为 `LazyVerticalGrid`:

```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(2),
    verticalArrangement = Arrangement.spacedBy(6.dp),
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    modifier = Modifier.fillMaxSize()
) {
    items(
        collectionState.notes,
        key = { it.id },
        contentType = { "note" }
    ) { note ->
        NoteCard(note = note, onAction = onAction)
    }
}
```

`NoteCard` 适配网格单元格，内容精简：标题（1行）+ 摘要（2行）+ 日期。

---

## 4. 时长记录功能调整

### 修改文件

#### `RecordsScreen.kt` — `TimerDisplay`

**默认显示"专注"**: 将 `timerState.activeTimerName` 显示替换为固定文字 `stringResource(Res.string.focus)`，不受 `activeTimerId` 影响。

**重置按钮 → 无限模式按钮**:

- 当 `infiniteFocus == false`: 显示无限模式图标，点击触发 `RecordsAction.StartInfiniteMode`
- 当 `infiniteFocus == true`: 显示退出图标，点击触发重置逻辑（退出无限模式 + 重置计时器）

新增 `RecordsAction.ExitInfiniteMode`:

```kotlin
data object ExitInfiniteMode : RecordsAction
```

`RecordsViewModel` 处理:

```kotlin
private fun exitInfiniteMode() {
    stateRepository.timerState.update { it.copy(infiniteFocus = false) }
    serviceHelper.startService(TimerAction.ResetTimer)
}
```

---

## 5. UI细节优化

### 修改文件

#### `TasksScreen.kt`

- `LazyColumn` 间距: `2.dp` → `1.dp`
- `TaskItem` 内边距: `horizontal = 16.dp, vertical = 12.dp` → `horizontal = 12.dp, vertical = 8.dp`

#### `CollectionScreen.kt`

- `LazyVerticalGrid` 间距: `8.dp` → `6.dp`

#### `RecordsScreen.kt`

- `TimerDisplay` 垂直内边距: `24.dp` → `16.dp`

#### `AddEditTaskSheet.kt`

`TimeInput` 居中修复:

```kotlin
Box(
    modifier = Modifier.fillMaxWidth(),
    contentAlignment = Alignment.Center
) {
    TimeInput(state = timePickerState)
}
```

---

## 文件修改清单

| 文件 | 修改内容 |
|------|----------|
| `shared/.../recordsScreen/viewModel/RecordsState.kt` | 扩展 StatsPeriod、新增统计字段 |
| `shared/.../recordsScreen/viewModel/RecordsAction.kt` | 新增 ExitInfiniteMode |
| `shared/.../recordsScreen/viewModel/RecordsViewModel.kt` | 增加统计数据流、exitInfiniteMode |
| `shared/.../recordsScreen/RecordsScreen.kt` | 重写 StatisticsTab、TimerDisplay 调整 |
| `shared/.../tasksScreen/TasksScreen.kt` | 放大FAB、间距调整 |
| `shared/.../collectionScreen/CollectionScreen.kt` | 放大FAB、双列网格布局 |
| `shared/.../tasksScreen/AddEditTaskSheet.kt` | TimeInput 居中修复 |
| `shared/build.gradle.kts` | 新增 Vico 依赖 |
| `shared/.../composeResources/values/strings.xml` | 新增字符串资源 |
| `shared/.../composeResources/values-zh-rCN/strings.xml` | 新增中文字符串 |

## 验证步骤

1. 统计页面切换"本日/本周/本月"，确认数据实时更新
2. 完成一次专注后，确认今日记录和统计数据立即刷新
3. 各计划今日时长列表正确显示
4. 图表正确渲染时长和次数数据
5. 最高记录显示具体时间点
6. 首页和收集页面FAB放大后视觉正常
7. 收集页面双列网格布局正常，卡片内容不溢出
8. 时长记录默认显示"专注"，不受选中自定义计时器影响
9. 重置按钮替换为无限模式按钮，功能正常
10. TimeInput 居中显示
11. 整体间距紧凑，视觉协调
