# Tomato App 功能修复与全面优化设计文档

日期：2026-06-06

## 概述

7项功能修复与优化任务，涵盖通知栏、专注项管理、底部栏重构、统计实时化等。

---

## 任务1：无限专注模式 — 通知栏实时数据

### 问题
进入无限专注时长后，Android 通知栏不显示实时已专注时长。

### 修改方案
- 通知标题：无限模式下改为 `"专注 · 已专注 23:45"` 格式（正计时）
- `setShortCriticalText`：已修复为显示 elapsed 正计时
- 通知每秒随 timer loop 自动更新（已有 `iterations == 0` 刷新逻辑）
- `TimerService.showTimerNotification()` 中，无限模式标题使用 `getString(R.string.focused_for, millisecondsToStr(elapsed))`

### 涉及文件
- `TimerService.kt` — 通知标题格式
- `strings.xml` / `values-zh-rCN/strings.xml` — 新增 `focused_for`

### 字符串资源
- `focused_for`：英文 `"Focused for %s"` / 中文 `"已专注 %s"`

---

## 任务2：时长记录 — 切换专注项后跳回默认

### 问题
选中自定义专注项后点击开始，选中状态自动跳回默认"专注"。

### 根因
`selectTimer()` 中，当 `serviceRunning = true` 时不更新 `timerState`，但 `RecordsState.activeTimerId` 始终更新。然而 `timerState.activeTimerId` 未同步，导致计时器运行中 chip 高亮与实际计时名称不一致。

### 修改方案
- `selectTimer()` 中：即使服务运行中，也更新 `timerState.activeTimerName` 和 `timerState.activeTimerId`（只改名称标签，不改时间参数）
- 计时器运行中切换专注项 = 只切换名称标签，不重置计时
- `RecordsState.activeTimerId` 始终与 `TimerState.activeTimerId` 同步
- 新增 `RecordsAction.UpdateActiveTimerName(timerId, name)` 用于运行中切换

### 涉及文件
- `RecordsViewModel.kt` — `selectTimer()` 逻辑
- `RecordsAction.kt` — 新增 action

---

## 任务3：专注项管理 — 长按删除/编辑

### 问题
自定义专注项 chip 只能选择，无法编辑或删除。

### 修改方案
- chip 增加 `combinedClickable(onLongClick = ...)` 交互
- 长按弹出 `DropdownMenu`，含「编辑名称」和「删除」选项
- 编辑：弹出 `AlertDialog` 含 `OutlinedTextField`，确认后调用 `customTimerRepository.updateName()`
- 删除：复用已有 `DeleteCustomTimer` action
- 新增 `RecordsAction.EditTimerName(timerId, newName)`
- `CustomTimerDao` 新增 `@Query UPDATE name` 方法

### 涉及文件
- `RecordsScreen.kt` — chip 长按交互 + DropdownMenu + AlertDialog
- `RecordsAction.kt` — 新增 `EditTimerName`
- `RecordsViewModel.kt` — 处理编辑 action
- `CustomTimerDao.kt` — 新增 updateName 查询
- `CustomTimerRepository.kt` — 新增 updateName 方法
- `strings.xml` / `values-zh-rCN/strings.xml` — 新增字符串

### 字符串资源
- `edit_name`：英文 `"Edit name"` / 中文 `"编辑名称"`
- `delete_timer`：英文 `"Delete timer"` / 中文 `"删除计时器"`
- `edit_timer_name`：英文 `"Edit timer name"` / 中文 `"编辑计时器名称"`

---

## 任务4：收集页面 — 编辑/新建时隐藏底部栏

### 问题
编辑/新建 Sheet 展开时底部导航栏仍然可见。

### 修改方案
- `AppScreen.kt` 中 `AnimatedVisibility` 条件增加：
  - `!recordsState.showAddTimerSheet && !recordsState.showAddCounterSheet`
  - collection 页面的 sheet 状态判断
- 需要将 `recordsState` 的 sheet 状态暴露到 `AppScreen` 层级（已通过 `collectAsStateWithLifecycle` 获取）
- collection 状态需确认 `CollectionState` 中的 sheet 字段名

### 涉及文件
- `AppScreen.kt` — `AnimatedVisibility` 条件扩展

---

## 任务5：计数器统计修正 — 不应计入"已完成"次数

### 问题
计数器 +1 时今日"已完成"数字也跟着 +1。

### 根因分析
当前 `todaySessionCount` 来自 `timerSessionRepository.getSessionCountByDate()`，计数器操作不写入 `TimerSession`，数据层已解耦。问题可能在 UI 层将 `sessionCount` 显示为"已完成"。

### 修改方案
- 今日统计区域区分三个维度：
  1. "专注轮次" — `todaySessionCount`（来自 TimerSession）
  2. "完成任务" — `todayCompletedTaskCount`（来自 Task 表，新增 DAO 查询）
  3. 计数器变化独立展示
- `TaskDao` 新增 `getCompletedTaskCountByDate(date: Long): Flow<Int>`
- `RecordsState` 新增 `todayCompletedTaskCount: Int`
- `RecordsViewModel` 的 combine 流增加 `TaskDao.getCompletedTaskCountByDate()` 数据源
- UI 中"已完成"改为"完成任务"标签，数值来自 Task 表

### 涉及文件
- `TaskDao.kt` — 新增查询
- `TaskRepository.kt` — 新增方法
- `RecordsState.kt` — 新增字段
- `RecordsViewModel.kt` — combine 流增加数据源
- `RecordsScreen.kt` — UI 标签修改
- `strings.xml` / `values-zh-rCN/strings.xml` — 新增/修改字符串

---

## 任务6：底部栏添加按钮全面重构

### 问题
各页面有独立添加按钮，不统一不直观。

### 修改方案
- 导航栏布局改为：`[任务] [收集] [FAB] [记录] [设置]`
- FAB 替换中间位置，使用 `FloatingActionButton` 凸起样式
- FAB 功能映射：
  - `Screen.Tasks.Main` → `TasksAction.ShowAddDialog`
  - `Screen.Collection.Main` → `CollectionAction.ShowAddSheet`
  - `Screen.Records.Main` → 根据 `recordsState.selectedTab`：0=添加计时器，1=添加计数器
  - `Screen.Settings.Main` → 隐藏 FAB（`AnimatedVisibility(visible = false)`）
- 移除各页面内独立添加按钮：
  - CounterTab 的 FloatingActionButton
  - DurationTab 的添加计时器按钮
  - CollectionScreen 的添加按钮
  - TasksScreen 的添加按钮
- FAB 切换动画：`AnimatedContent` + `AnimatedVisibility` 平滑过渡

### 涉及文件
- `AppScreen.kt` — 导航栏重构，FAB 集成
- `RecordsScreen.kt` — 移除 CounterTab FAB、DurationTab 添加按钮
- `TasksScreen.kt` — 移除独立添加按钮
- `CollectionScreen.kt` — 移除独立添加按钮
- `strings.xml` / `values-zh-rCN/strings.xml` — 新增 `add_item`

### 字符串资源
- `add_item`：英文 `"Add"` / 中文 `"添加"`

---

## 任务7：数据统计全面优化

### 7a. 今日统计实时化

#### 修改方案
- `RecordsState` 中 `todayTotalFocus` 已从 Stat 表读取
- 无限模式运行时实时累加：显示值 = `todayStat.totalFocusTime() + infiniteFocusElapsed`
- `infiniteFocusElapsed` 已在 combine 流中从 `timerState.elapsed` 实时更新
- 完成轮次时 TimerSession 插入自动触发 Flow 刷新
- UI 层计算：`val displayTotalFocus = recordsState.todayTotalFocus + recordsState.infiniteFocusElapsed`

#### 涉及文件
- `RecordsScreen.kt` — 今日统计显示逻辑

### 7b. 统计维度适配

#### 修改方案
- 今日摘要区域：总专注时长（含实时）、专注轮次数、完成任务数
- 图表数据：已有 `infiniteFocusElapsed` 累加逻辑，确认正确性
- 计数器统计独立展示：新增"今日计数器变化"概要
- `CounterRecordDao` 新增 `@Query SELECT SUM(count) FROM counter_entry WHERE date = :date`
- `CounterRecordRepository` 新增 `getTodayCounterTotalChange()` 方法
- `RecordsState` 新增 `todayCounterTotalChange: Int`
- `RecordsViewModel` combine 流增加数据源

#### 涉及文件
- `CounterRecordDao.kt` — 新增查询
- `CounterRecordRepository.kt` — 新增方法
- `RecordsState.kt` — 新增字段
- `RecordsViewModel.kt` — combine 流增加数据源
- `RecordsScreen.kt` — 统计 Tab UI 更新

### 7c. 无限专注模式统计规则

#### 规则
- 进入即实时记录（已实现：`infiniteFocusElapsed`）
- 退出时写入 TimerSession（`handleSetInfiniteFocus(false)` 中已调用 `saveSessionToDb()`）
- 停止但未退出：已专注时长保留在 `timerState.elapsed` 和 Stat 表中
- 无限专注不增加轮次计数，但时长正常累加到所有统计维度

#### 无需额外修改
当前实现已满足这些规则，只需确认 7a/7b 的 UI 层正确展示即可。

---

## 实施顺序

1. 任务1（通知栏）— 独立，无依赖
2. 任务2（专注项选中状态）— 独立
3. 任务3（长按编辑/删除）— 独立
4. 任务5（计数器统计修正）— 独立
5. 任务7（数据统计优化）— 依赖任务5的新字段
6. 任务4（隐藏底部栏）— 独立
7. 任务6（底部栏重构）— 最后执行，因为涉及移除各页面独立按钮，需确保其他任务先完成
