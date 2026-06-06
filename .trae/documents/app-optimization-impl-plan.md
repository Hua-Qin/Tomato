# 应用功能优化与UI改进 - 实施计划

基于设计文档 `docs/superpowers/specs/2026-06-06-app-optimization-design.md`

## 实施步骤

### 步骤 1: 数据层 - RecordsState & RecordsAction 扩展

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/recordsScreen/viewModel/RecordsState.kt`

- `StatsPeriod` 枚举新增 `DAY`，变为 `{ DAY, WEEK, MONTH }`
- `RecordsState` 新增字段：
  - `todayTotalFocus: Long = 0L`
  - `todaySessionCount: Int = 0`
  - `timerDurationStats: List<TimerDurationStat> = emptyList()`
  - `periodSessions: List<TimerSession> = emptyList()`
  - `todayStat: Stat? = null`

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/recordsScreen/viewModel/RecordsAction.kt`

- 新增 `data object ExitInfiniteMode : RecordsAction`

### 步骤 2: RecordsViewModel 统计数据流增强

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/recordsScreen/viewModel/RecordsViewModel.kt`

- 注入 `StatRepository`（已有 `timerSessionRepository`）
- 在 `init` 的 `combine` 中新增数据源：
  - `timerSessionRepository.getDurationByTimerName(today, today)` → `timerDurationStats`
  - `statRepository.getTodayStat()` → `todayStat`，从中计算 `todayTotalFocus`
  - `timerSessionRepository.getSessionCountByDate(today)` → `todaySessionCount`
  - `timerSessionRepository.getSessionsBetweenDates(startDate, endDate)` → `periodSessions`（根据 `statsPeriod` 动态计算日期范围）
- 新增 `exitInfiniteMode()` 方法
- `onAction` 中处理 `ExitInfiniteMode`
- 当 `statsPeriod` 变化时，需要重新订阅 `periodSessions` 数据流。由于 `combine` 是静态的，使用 `flatMapLatest` 或在 `setStatsPeriod` 中手动触发更新

### 步骤 3: 字符串资源

**文件**: `shared/src/commonMain/composeResources/values/strings.xml`

新增：
```xml
<string name="today_tab">Today</string>
<string name="best_record">Best record</string>
<string name="enter_infinite_mode">Infinite mode</string>
<string name="exit_infinite_mode">Exit infinite</string>
<string name="focus_duration_chart">Focus duration</string>
<string name="session_count_chart">Session count</string>
<string name="today_focus_by_plan">Today's focus by plan</string>
```

**文件**: `shared/src/commonMain/composeResources/values-zh-rCN/strings.xml`

新增：
```xml
<string name="today_tab">本日</string>
<string name="best_record">最高记录</string>
<string name="enter_infinite_mode">无限模式</string>
<string name="exit_infinite_mode">退出无限</string>
<string name="focus_duration_chart">专注时长</string>
<string name="session_count_chart">完成次数</string>
<string name="today_focus_by_plan">今日各计划专注时长</string>
```

### 步骤 4: StatisticsTab 重写（Vico 图表）

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/recordsScreen/RecordsScreen.kt`

重写 `StatisticsTab` composable，布局：

1. `SingleChoiceSegmentedButtonRow` — "本日 / 本周 / 本月"（3段，索引对应 `StatsPeriod.DAY/WEEK/MONTH`）
2. 今日概览卡片：`todayTotalFocus` 格式化 + `todaySessionCount` 次数
3. 各计划今日时长列表：遍历 `timerDurationStats`，每项显示 "计划名，今日 Xh Xm"
4. 时长统计图表：Vico `CartesianChartHost` + `columnChart()`，数据按日期分组
5. 次数统计图表：Vico 柱状图，按日期统计完成次数
6. 最高记录：从 `periodSessions` 找 `actualDuration` 最大值

Vico 图表实现要点：
- 使用 `CartesianChartModel` 构建数据
- `CartesianChartHost` 渲染图表
- `rememberCartesianChartModel` 管理状态
- X轴使用 `DecimalAxisValueFormatter` 或自定义格式化器显示日期
- 图表颜色使用 `colorScheme.primary` / `colorScheme.tertiary`

### 步骤 5: TimerDisplay 调整

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/recordsScreen/RecordsScreen.kt`

- 将 `timerState.activeTimerName` 显示替换为 `stringResource(Res.string.focus)` 固定文字
- 将重置按钮替换为无限模式按钮：
  - `infiniteFocus == false`：显示无限模式图标（使用 `Res.drawable.infinite_focus` 或自定义图标），点击 `RecordsAction.StartInfiniteMode`
  - `infiniteFocus == true`：显示退出图标（使用 `Res.drawable.restart_large`），点击 `RecordsAction.ExitInfiniteMode`
- `TimerDisplay` 垂直内边距 `24.dp` → `16.dp`

### 步骤 6: 首页UI优化

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/tasksScreen/TasksScreen.kt`

- `SmallFloatingActionButton` → `FloatingActionButton`
- 图标尺寸 `Modifier.size(24.dp)` → `Modifier.size(32.dp)`
- `LazyColumn` 间距 `Arrangement.spacedBy(2.dp)` → `Arrangement.spacedBy(1.dp)`
- `TaskItem` 内边距 `horizontal = 16.dp, vertical = 12.dp` → `horizontal = 12.dp, vertical = 8.dp`

### 步骤 7: 收集页面优化

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/collectionScreen/CollectionScreen.kt`

- `SmallFloatingActionButton` → `FloatingActionButton`
- 图标尺寸 `Modifier.size(24.dp)` → `Modifier.size(32.dp)`
- 将 `LazyColumn` 替换为 `LazyVerticalGrid(GridCells.Fixed(2))`
- 间距 `Arrangement.spacedBy(8.dp)` → `Arrangement.spacedBy(6.dp)`
- `NoteCard` 适配网格：`modifier = Modifier.fillMaxWidth()` 已满足
- 新增 import: `LazyVerticalGrid`, `GridCells`

### 步骤 8: TimeInput 居中修复

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/tasksScreen/AddEditTaskSheet.kt`

在时间选择器对话框中，将 `TimeInput` 包裹在居中容器中：

```kotlin
// 原代码
TimeInput(state = timePickerState)

// 修改为
Box(
    modifier = Modifier.fillMaxWidth(),
    contentAlignment = Alignment.Center
) {
    TimeInput(state = timePickerState)
}
```

新增 import: `Box`, `Alignment`

### 步骤 9: 编译验证与 CI 构建

- 确认所有修改无编译错误
- 提交推送并触发 GitHub Actions CI 构建
- 验证 APK 构建成功

## 文件修改清单（按执行顺序）

| 步骤 | 文件 | 修改类型 |
|------|------|----------|
| 1 | `RecordsState.kt` | 扩展枚举和数据类 |
| 1 | `RecordsAction.kt` | 新增 ExitInfiniteMode |
| 2 | `RecordsViewModel.kt` | 增加统计数据流 + exitInfiniteMode |
| 3 | `strings.xml` | 新增字符串 |
| 3 | `strings.xml (zh-rCN)` | 新增中文字符串 |
| 4 | `RecordsScreen.kt` | 重写 StatisticsTab |
| 5 | `RecordsScreen.kt` | TimerDisplay 调整 |
| 6 | `TasksScreen.kt` | 放大FAB + 间距调整 |
| 7 | `CollectionScreen.kt` | 放大FAB + 双列网格 |
| 8 | `AddEditTaskSheet.kt` | TimeInput 居中 |
| 9 | CI | 编译验证 |

## 风险与注意事项

1. **Vico API 兼容性**: Vico 3.1.0-alpha.3 是 alpha 版本，API 可能不稳定，需参考最新文档
2. **combine 数据流**: 新增多个 Flow 到 combine 中，参数数量增加，需确保类型对应正确
3. **StatsPeriod.DAY 日期计算**: "本日"的图表数据需要从 `Stat` 的 Q1-Q4 字段提取，按时段分组
4. **LazyVerticalGrid 与 NoteCard**: 双列布局下 NoteCard 的长按菜单和点击事件需正常工作
5. **TimeInput 居中**: `DatePickerDialog` 的内容布局可能需要额外调整才能实现居中
