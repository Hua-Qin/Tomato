# Tomato App 功能修复与优化设计文档（第十轮）

日期：2026-06-06

## 概述

5 项功能修复与优化：跳过→结束本次、底部栏常驻、统计页中文化+日历、收集页FAB修复、设置页计时器管理。

---

## 任务一：「跳过」→「结束本次」

### 当前行为
- `skipTimer()` 保存当前轮数据 → cycles++ → 切换到下一轮继续计时
- UI 按钮文案为「跳过」，图标为 skip_next_large

### 修改内容

**UI 层：**
- RecordsScreen.kt：skip 按钮文案改为 `Res.string.end_session`（「结束本次」），图标改为 stop 图标
- 新增字符串资源：`end_session`（中文：结束本次，英文：End Session）

**TimerService：**
- 新增 `Actions.END_SESSION` 枚举值
- 新增 `endSession()` 方法：
  1. 如果计时器正在运行，先停止（`timerRunning = false`）
  2. 保存当前轮数据（`saveTimeToDb()` + `saveSessionToDb()`）
  3. 重置所有状态：cycles=0, startTime=0, sessionStartWallTime=0, pauseTime=0, pauseDuration=0, lastSavedDuration=0
  4. 回到初始 FOCUS 状态（timeStr 显示初始时间，不自动开始）
  5. 退出无限模式（`infiniteFocus = false`）
  6. 停止前台服务（`stopForegroundService()`）
- `onStartCommand` 中处理 `Actions.END_SESSION`

**RecordsViewModel：**
- 新增 `RecordsAction.EndSession`
- `endSession()` 通过 ServiceHelper 发送 END_SESSION action

**移除：**
- 删除 `Actions.SKIP` 枚举值
- 删除 `TimerAction.SkipTimer`
- 删除 `RecordsAction.SkipTimer`
- 删除 `skipTimer()` 方法（ViewModel 和 Service 中）

---

## 任务二：计时中底部栏保持可见

### 当前问题
- `FloatingToolbarExitDirection.Bottom` + `exitAlwaysScrollBehavior` 导致内容滚动时底部栏自动隐藏
- 计时界面用户无法在计时中切换页面

### 修改内容

**AppScreen.kt：**
- 将 `FloatingToolbarDefaults.exitAlwaysScrollBehavior(FloatingToolbarExitDirection.Bottom)` 改为不使用 scrollBehavior
- 移除 `toolbarScrollBehavior` 变量和 `nestedScroll` 修饰符
- `HorizontalFloatingToolbar` 的 `scrollBehavior` 参数设为 null 或移除
- 确保 `expanded = true` 固定展开

---

## 任务三：统计页优化

### 3.1 时间单位中文化

**修改 `formatSessionDuration()`：**
```kotlin
private fun formatSessionDuration(duration: Long): String {
    val minutes = duration / (60 * 1000)
    return if (minutes >= 60) "${minutes / 60}小时${minutes % 60}分钟"
    else "${minutes}分钟"
}
```

**图表 Y 轴中文化：**
- 新增 `CartesianValueFormatter` 用于 Y 轴，将分钟值格式化为中文（如 25→"25分", 120→"2时"）
- 在 `SimpleColumnChart` 的 `startAxis` 中应用此 formatter

### 3.2 统计页内嵌日历

**UI 结构（StatisticsTab 内，周期切换下方）：**
1. 日历卡片（Surface + Column）
2. 月历网格：7列（周一~周日），显示当月日期
3. 有记录的日期显示小圆点标记（primary 色）
4. 选中日期高亮
5. 选中日期下方展开当日专注明细列表

**当日专注明细列表：**
- 每行：任务名 | 时长（中文格式）| 次数
- 数据来自 `TimerSessionDao` 按日期+任务名分组查询

**新增 DAO 查询：**
```sql
-- 按日期+任务名分组查询时长和次数
SELECT timerName, SUM(actualDuration) as totalDuration, COUNT(*) as sessionCount
FROM timer_session
WHERE date = :date
GROUP BY timerName
ORDER BY totalDuration DESC
```

**新增数据类：**
```kotlin
data class DailyTaskStat(
    val timerName: String,
    val totalDuration: Long,
    val sessionCount: Int
)
```

**新增 Repository 方法：**
- `TimerSessionDao.getDailyTaskStats(date: LocalDate): Flow<List<DailyTaskStat>>`
- `TimerSessionRepository.getDailyTaskStats(date: LocalDate): Flow<List<DailyTaskStat>>`

**RecordsState 新增字段：**
- `selectedCalendarDate: LocalDate = LocalDate.now()`
- `dailyTaskStats: List<DailyTaskStat> = emptyList()`
- `calendarDatesWithRecords: Set<LocalDate> = emptySet()`

**RecordsViewModel 新增逻辑：**
- 新增 combine 流监听 `selectedCalendarDate` 变化，查询当日任务统计
- 新增查询当月有记录的日期集合（用于日历标记）
- 新增 `RecordsAction.SelectCalendarDate(date: LocalDate)`

**日历组件实现：**
- 不引入第三方日历库，自行实现简易月历网格
- 使用 `LazyVerticalGrid` 或 `Row`+`Column` 布局
- 月份切换：左右箭头按钮

---

## 任务四：收集页 FAB 修复

### 问题排查
AppScreen FAB 的 Collection 分支调用 `collectionViewModel.onAction(CollectionAction.NavigateToAddNote)`，而 `collectionActionHandler` 将其转为 `backStack.add(Screen.Collection.AddNote)`。

### 修改内容
- 检查 FAB 中 `collectionViewModel` 实例是否与 CollectionScreen 一致
- 确保 `collectionActionHandler` 正确拦截 `NavigateToAddNote` 并导航
- 如果 viewModel 实例不一致，改为通过 onAction 回调传递

---

## 任务五：设置页专注计划管理

### 新增路由
- `Screen.Settings.TimerManager`

### 新增页面：TimerManagerScreen
- 列表展示所有自定义计时器（来自 `CustomTimerDao.getAllCustomTimers()`）
- 每个计时器卡片：名称 | 专注时长 | 短休息 | 长休息 | 轮次数
- 卡片右侧编辑/删除图标按钮
- 底部或顶部 FAB 添加新计时器

### 编辑/新增计时器弹窗（AlertDialog）
- 名称输入（OutlinedTextField）
- 专注时长滑块/输入（分钟）
- 短休息时长滑块/输入
- 长休息时长滑块/输入
- 轮次数滑块
- 闹钟开关
- 振动开关
- 自动开始下一轮开关

### 新增 ViewModel：TimerManagerViewModel
- 构造函数注入 `CustomTimerRepository`
- state: `StateFlow<TimerManagerState>`
- 方法：loadTimers(), addTimer(), updateTimer(), deleteTimer()

### SettingsMainScreen 入口
- 在设置列表中添加「专注计划管理」项，点击导航到 `Screen.Settings.TimerManager`

### 主题风格
- 使用 Material 3 Expressive 风格，与现有设置子页面一致
- 颜色：primaryContainer 卡片背景，primary 强调色
- 圆角：shapes.large

---

## 涉及文件清单

| 文件 | 修改类型 |
|------|---------|
| TimerService.kt | 新增 END_SESSION，删除 SKIP |
| AppScreen.kt | 移除 scrollBehavior，FAB 修复 |
| RecordsScreen.kt | 结束本次按钮，日历组件，中文化 |
| RecordsViewModel.kt | 新增日历/结束逻辑 |
| RecordsState.kt | 新增日历相关字段 |
| RecordsAction.kt | 新增 EndSession, SelectCalendarDate |
| TimerSessionDao.kt | 新增 getDailyTaskStats, getDatesWithRecordsInMonth |
| TimerSessionRepository.kt | 新增对应方法 |
| SettingsScreen.kt | 新增 TimerManager 路由 |
| SettingsMainScreen.kt | 新增入口项 |
| Screen.kt | 新增 Settings.TimerManager |
| CustomTimerDao.kt | 可能需要新增查询 |
| CustomTimerRepository.kt | 可能需要新增方法 |
| strings.xml / strings-zh-rCN.xml | 新增字符串资源 |
| 新文件：TimerManagerScreen.kt | 设置页计时器管理界面 |
| 新文件：TimerManagerViewModel.kt | 计时器管理 ViewModel |
| 新文件：TimerManagerState.kt | 计时器管理 State |
