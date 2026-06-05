# 产品迭代优化计划：首页计划任务+收集+记录+小组件迁移

## 概述

基于 Tomato（KMP + Jetpack Compose + Material 3 Expressive）现有架构，实施四大模块重构：
- 首页从专注计时替换为「计划任务」（参考 snaptick）
- 新增「收集」页面（Markdown 文档库）
- 新增「记录」页面（时长记录+次数记录+数据统计，合并原 Timer 和 Stats 功能）
- 小组件全量迁移适配

---

## 现状分析

### 技术栈
- **框架**: Kotlin Multiplatform + Jetpack Compose + Material 3 Expressive
- **导航**: Navigation 3 + `rememberNavBackStack`
- **数据库**: Room（entities: `Stat`, `IntPreference`, `BooleanPreference`, `StringPreference`），当前版本 2
- **DI**: Koin
- **小组件**: Glance（3个: `TimerAppWidget`, `TodayAppWidget`, `HistoryAppWidget`）
- **服务**: `TimerService`（前台服务，管理计时逻辑）

### 当前底部导航
3个标签页: Timer / Stats / Settings，使用 `HorizontalFloatingToolbar`

### 当前计时模式
`TimerMode` 枚举: `FOCUS`, `SHORT_BREAK`, `LONG_BREAK`, `BRAND`
- 固定配置: focusTime / shortBreakTime / longBreakTime / sessionLength
- 无使用次数统计，无自定义分类

### 关键文件索引
| 模块 | 文件路径 |
|------|----------|
| 底部导航 | `androidApp/.../ui/AppScreen.kt` |
| Screen定义 | `shared/.../ui/Screen.kt` |
| 导航配置 | `shared/.../Navigation.kt` |
| TimerState | `shared/.../ui/timerScreen/viewModel/TimerState.kt` |
| TimerAction | `shared/.../ui/timerScreen/viewModel/TimerAction.kt` |
| TimerViewModel | `shared/.../ui/timerScreen/viewModel/TimerViewModel.kt` (androidMain) |
| TimerScreen | `shared/.../ui/timerScreen/TimerScreen.kt` (androidMain) |
| TimerService | `androidApp/.../service/TimerService.kt` |
| StateRepository | `shared/.../data/StateRepository.kt` |
| SettingsState | `shared/.../ui/settingsScreen/viewModel/SettingsState.kt` |
| SettingsViewModel | `shared/.../ui/settingsScreen/viewModel/SettingsViewModel.kt` |
| StatsScreen | `shared/.../ui/statsScreen/StatsScreen.kt` |
| StatsViewModel | `shared/.../ui/statsScreen/viewModel/StatsViewModel.kt` |
| AppDatabase | `shared/.../data/AppDatabase.kt` |
| Stat Entity | `shared/.../data/Stat.kt` |
| StatDao | `shared/.../data/StatDao.kt` |
| StatRepository | `shared/.../data/StatRepository.kt` |
| PreferenceDao | `shared/.../data/PreferenceDao.kt` |
| PreferenceRepository | `shared/.../data/PreferenceRepository.kt` |
| DI模块 | `androidApp/.../di/androidModules.kt` |
| TimerAppWidget | `androidApp/.../widget/TimerAppWidget.kt` |
| TodayAppWidget | `androidApp/.../widget/TodayAppWidget.kt` |
| HistoryAppWidget | `androidApp/.../widget/HistoryAppWidget.kt` |
| StartServiceAction | `androidApp/.../widget/StartServiceAction.kt` |
| TimerWidgetReceiver | `androidApp/.../widget/TimerWidgetReceiver.kt` |
| TodayWidgetReceiver | `androidApp/.../widget/TodayWidgetReceiver.kt` |
| HistoryWidgetReceiver | `androidApp/.../widget/HistoryWidgetReceiver.kt` |
| shared build.gradle | `shared/build.gradle.kts` |

---

## 变更计划

### 一、底部导航重构

**目标**: 从 3 标签（Timer/Stats/Settings）改为 4 标签（计划任务/收集/记录/设置）

#### 1.1 Screen 枚举扩展

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/Screen.kt`

删除 `Screen.Timer`、`Screen.AOD`、`Screen.Stats`，新增：

```kotlin
sealed class Screen : NavKey {
    // 计划任务（新首页）
    @Serializable
    sealed class Tasks : Screen() {
        @Serializable
        object Main : Tasks()
        @Serializable
        object AddTask : Tasks()
        @Serializable
        data class EditTask(val taskId: Long) : Tasks()
    }

    // 收集
    @Serializable
    sealed class Collection : Screen() {
        @Serializable
        object Main : Collection()
        @Serializable
        object AddNote : Collection()
        @Serializable
        data class EditNote(val noteId: Long) : Collection()
    }

    // 记录
    @Serializable
    sealed class Records : Screen() {
        @Serializable
        object Main : Records()
        @Serializable
        object Timer : Records()          // 时长记录（原专注计时）
        @Serializable
        object Statistics : Records()     // 数据统计
    }

    // 设置（保留）
    @Serializable
    sealed class Settings : Screen() {
        @Serializable
        object Main : Settings()
        @Serializable
        object About : Settings()
        @Serializable
        object Alarm : Settings()
        @Serializable
        object Appearance : Settings()
        @Serializable
        object Backup : Settings()
        @Serializable
        object Timer : Settings()
    }
}
```

#### 1.2 AppScreen 底部导航变更

**文件**: `androidApp/src/main/java/org/nsh07/pomodoro/ui/AppScreen.kt`

- `mainScreens` 列表改为 4 项：
  1. `Screen.Tasks.Main` — 图标: task_list / task_list_filled，标签: "计划任务"
  2. `Screen.Collection.Main` — 图标: note_add / note_add_filled，标签: "收集"
  3. `Screen.Records.Main` — 图标: timer / timer_filled，标签: "记录"
  4. `Screen.Settings.Main` — 图标: settings / settings_filled，标签: "设置"
- 新增 `TasksViewModel`、`CollectionViewModel`、`RecordsViewModel` 实例
- NavDisplay 新增对应 entry
- 移除 Timer/Stats/AOD 相关 entry
- `HorizontalFloatingToolbar` 保持现有风格，4 个标签需调整布局适配

#### 1.3 图标资源

新增 drawable 资源（需在 `shared/src/commonMain/composeResources/drawable/` 中添加）：
- `task_list.svg` / `task_list_filled.svg`
- `note_add.svg` / `note_add_filled.svg`
- 保留 `timer.svg` / `timer_filled.svg`（用于记录页）
- 保留 `settings.svg` / `settings_filled.svg`

#### 1.4 字符串资源

新增多语言字符串资源（`shared/src/commonMain/composeResources/values/strings.xml` 等）：
- `tasks` / `collection` / `records`
- 各新页面标题、按钮文案等

---

### 二、计划任务页面（新首页）

**目标**: 参考 snaptick 交互模式，支持任务增删改查、周期重复、到期提醒

#### 2.1 数据层

**新增 Entity: `Task`**

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/data/Task.kt`

```kotlin
@Entity(tableName = "task")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val dueDate: Long?,              // 到期日期时间戳(epoch ms)
    val dueTime: Int?,               // 到期时间(一天内的秒数)
    val reminderEnabled: Boolean = false,
    val reminderMinutesBefore: Int = 0,
    val repeatRule: String = "none",  // none/daily/weekly/monthly/custom
    val repeatCustomDays: String = "", // 如 "1,3,5" 表示周一三五
    val priority: Int = 0,           // 0=无, 1=低, 2=中, 3=高
    val category: String = "",
    val createdAt: Long,
    val completedAt: Long? = null,
    val sortOrder: Int = 0
)
```

**新增 DAO: `TaskDao`**

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/data/TaskDao.kt`

```kotlin
@Dao
interface TaskDao {
    @Insert(onConflict = REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM task WHERE isCompleted = 0 ORDER BY sortOrder, dueDate")
    fun getPendingTasks(): Flow<List<Task>>

    @Query("SELECT * FROM task WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM task WHERE date(dueDate/1000, 'unixepoch') = date(:date/1000, 'unixepoch')")
    fun getTasksByDate(date: Long): Flow<List<Task>>

    @Query("SELECT * FROM task ORDER BY sortOrder, dueDate")
    fun getAllTasks(): Flow<List<Task>>

    @Query("DELETE FROM task WHERE id = :id")
    suspend fun deleteTaskById(id: Long)
}
```

**新增 Repository: `TaskRepository`**

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/data/TaskRepository.kt`

- 接口 `TaskRepository` + 实现 `AppTaskRepository`
- 包含重复任务逻辑：当重复任务被标记完成时，自动生成下一次实例
- `completeTask(taskId)`: 标记完成 + 如有重复规则则生成新实例
- `uncompleteTask(taskId)`: 撤销完成

#### 2.2 ViewModel

**新增 TasksState**

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/tasksScreen/viewModel/TasksState.kt`

```kotlin
@Immutable
data class TasksState(
    val pendingTasks: List<Task> = emptyList(),
    val completedTasks: List<Task> = emptyList(),
    val selectedDate: Long = System.currentTimeMillis(),
    val showAddDialog: Boolean = false,
    val editingTask: Task? = null,
    val filterCategory: String? = null
)
```

**新增 TasksViewModel**

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/tasksScreen/viewModel/TasksViewModel.kt`

- 注入 `TaskRepository`
- 管理 `TasksState`
- 处理 Actions: AddTask, EditTask, DeleteTask, CompleteTask, UncompleteTask, SelectDate, FilterCategory

**新增 TasksAction**

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/tasksScreen/viewModel/TasksAction.kt`

```kotlin
sealed interface TasksAction {
    data class AddTask(val task: Task) : TasksAction
    data class UpdateTask(val task: Task) : TasksAction
    data class DeleteTask(val taskId: Long) : TasksAction
    data class CompleteTask(val taskId: Long) : TasksAction
    data class UncompleteTask(val taskId: Long) : TasksAction
    data class SelectDate(val date: Long) : TasksAction
    data class SetFilter(val category: String?) : TasksAction
    data object ShowAddDialog : TasksAction
    data object HideAddDialog : TasksAction
}
```

#### 2.3 UI

**新增 TasksScreen**

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/tasksScreen/TasksScreen.kt`

布局对标 snaptick：
- **顶部**: 日期选择器（水平滚动周历）+ 今日概览（待办数/已完成数）
- **中部**: 任务列表
  - 未完成区：按优先级/时间排序
  - 已完成区：折叠展示
  - 任务项：勾选框 + 标题 + 时间 + 重复标识 + 优先级色标 + 分类标签
  - 滑动操作：左滑删除、右滑完成
- **底部**: FAB 添加按钮（`+` 图标）
- **空状态**: 引导文案 + 插图

**新增 AddEditTaskSheet**

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/tasksScreen/AddEditTaskSheet.kt`

BottomSheet 形式（参考 snaptick 新建任务交互）：
- 标题输入
- 描述输入
- 日期选择器
- 时间选择器
- 提醒开关 + 提前分钟数
- 重复规则选择器（不重复/每天/每周/每月/自定义星期）
- 优先级选择（无/低/中/高）
- 分类标签

#### 2.4 提醒功能

**新增 TaskReminderScheduler**

**文件**: `androidApp/src/main/java/org/nsh07/pomodoro/service/TaskReminderScheduler.kt`

- 使用 `AlarmManager` + `PendingIntent` 调度任务提醒
- 任务创建/更新时注册提醒
- 任务删除/完成时取消提醒

**新增 TaskReminderReceiver**

**文件**: `androidApp/src/main/java/org/nsh07/pomodoro/service/TaskReminderReceiver.kt`

- `BroadcastReceiver`，接收提醒广播
- 展示通知（点击跳转到 Tasks 页面）

---

### 三、收集页面

**目标**: Markdown 文档库，用于日常想法、灵感记录

#### 3.1 数据层

**新增 Entity: `Note`**

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/data/Note.kt`

```kotlin
@Entity(tableName = "note")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val content: String = "",        // Markdown 格式内容
    val createdAt: Long,
    val updatedAt: Long,
    val sortOrder: Int = 0,
    val isPinned: Boolean = false    // 置顶
)
```

**新增 DAO: `NoteDao`**

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/data/NoteDao.kt`

```kotlin
@Dao
interface NoteDao {
    @Insert(onConflict = REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM note ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM note WHERE id = :id")
    fun getNoteById(id: Long): Flow<Note?>

    @Query("SELECT * FROM note WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    fun searchNotes(query: String): Flow<List<Note>>
}
```

**新增 Repository: `NoteRepository`**

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/data/NoteRepository.kt`

- 接口 + `AppNoteRepository` 实现

#### 3.2 ViewModel

**新增 CollectionState**

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/collectionScreen/viewModel/CollectionState.kt`

```kotlin
@Immutable
data class CollectionState(
    val notes: List<Note> = emptyList(),
    val searchQuery: String = "",
    val isSearching: Boolean = false
)
```

**新增 CollectionViewModel**

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/collectionScreen/viewModel/CollectionViewModel.kt`

- 管理 `CollectionState`
- CRUD 操作 + 搜索 + 置顶

**新增 CollectionAction**

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/collectionScreen/viewModel/CollectionAction.kt`

#### 3.3 UI

**新增 CollectionScreen**

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/collectionScreen/CollectionScreen.kt`

- **顶部**: 搜索栏 + FAB 新建按钮
- **列表**: 瀑布流/列表布局，卡片展示
  - 卡片内容: 标题 + 内容预览 + 更新时间
  - 置顶标识
  - 长按菜单: 编辑/删除/置顶
- **空状态**: "记录你的灵感" 引导

**新增 NoteEditScreen**

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/collectionScreen/NoteEditScreen.kt`

- **顶部**: 返回按钮 + 标题输入
- **编辑区**: Markdown 编辑器
  - 工具栏: 加粗/斜体/标题/列表/代码块 等快捷操作
  - 实时预览切换按钮
- **预览区**: Markdown 渲染预览

**Markdown 渲染依赖**: 新增 `com.mikepenz:multiplatform-markdown-renderer` 或类似库

---

### 四、记录页面

**目标**: 合并原专注计时 + 统计功能，新增次数记录

#### 4.1 数据层

**新增 Entity: `TimerSession`**（计时使用记录）

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/data/TimerSession.kt`

```kotlin
@Entity(tableName = "timer_session")
data class TimerSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timerName: String,           // 计时名称（如"工作"、"学习"）
    val duration: Long,              // 设定时长(ms)
    val actualDuration: Long,        // 实际时长(ms)
    val startedAt: Long,             // 开始时间戳
    val completedAt: Long?,          // 完成时间戳
    val date: LocalDate              // 日期
)
```

**新增 Entity: `CounterRecord`**（次数记录）

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/data/CounterRecord.kt`

```kotlin
@Entity(tableName = "counter_record")
data class CounterRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,               // 记录标题
    val createdAt: Long,
    val sortOrder: Int = 0
)

@Entity(tableName = "counter_entry")
data class CounterEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val counterId: Long,             // 关联 CounterRecord.id
    val date: LocalDate,             // 日期
    val count: Int = 0               // 当日累计次数
)
```

**新增 Entity: `CustomTimer`**（自定义计时配置）

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/data/CustomTimer.kt`

```kotlin
@Entity(tableName = "custom_timer")
data class CustomTimer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,                // 自定义名称
    val iconKey: String = "timer",   // 图标标识
    val focusDuration: Long,         // 专注时长(ms)
    val shortBreakDuration: Long,    // 短休息(ms)
    val longBreakDuration: Long,     // 长休息(ms)
    val sessionLength: Int,          // 循环次数
    val alarmEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val autoStartNext: Boolean = false,
    val sortOrder: Int = 0
)
```

**新增 DAOs**

- `TimerSessionDao` — `shared/.../data/TimerSessionDao.kt`
- `CounterRecordDao` — `shared/.../data/CounterRecordDao.kt`
- `CustomTimerDao` — `shared/.../data/CustomTimerDao.kt`

**新增 Repositories**

- `TimerSessionRepository` — `shared/.../data/TimerSessionRepository.kt`
- `CounterRecordRepository` — `shared/.../data/CounterRecordRepository.kt`
- `CustomTimerRepository` — `shared/.../data/CustomTimerRepository.kt`

#### 4.2 数据库升级

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/data/AppDatabase.kt`

- 版本 2→3，新增 entities: `Task`, `Note`, `TimerSession`, `CounterRecord`, `CounterEntry`, `CustomTimer`
- 新增 DAOs: `taskDao()`, `noteDao()`, `timerSessionDao()`, `counterRecordDao()`, `customTimerDao()`
- AutoMigration: `AutoMigration(from = 2, to = 3)`

#### 4.3 ViewModel

**新增 RecordsState**

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/recordsScreen/viewModel/RecordsState.kt`

```kotlin
@Immutable
data class RecordsState(
    // 时长记录
    val customTimers: List<CustomTimer> = emptyList(),
    val activeTimerId: Long? = null,
    val timerState: TimerState = TimerState(),
    val todaySessions: List<TimerSession> = emptyList(),

    // 次数记录
    val counters: List<CounterRecord> = emptyList(),
    val counterEntries: Map<Long, Int> = emptyMap(), // counterId -> today's count

    // 统计
    val statsPeriod: StatsPeriod = StatsPeriod.WEEK,
    val weeklyStats: List<TimerSession> = emptyList(),
    val monthlyStats: List<TimerSession> = emptyList()
)

enum class StatsPeriod { WEEK, MONTH }
```

**新增 RecordsViewModel**

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/recordsScreen/viewModel/RecordsViewModel.kt`

- 管理 `RecordsState`
- 时长记录: 选择/创建自定义计时 → 启动计时 → 记录 session
- 次数记录: 增加/减少计数 → 记录 entry
- 统计: 按周/月切换，计算各类时长占比

**新增 RecordsAction**

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/recordsScreen/viewModel/RecordsAction.kt`

#### 4.4 TimerService 适配

**文件**: `androidApp/src/main/java/org/nsh07/pomodoro/service/TimerService.kt`

- 新增 `customTimerId` 支持：根据 `activeTimerId` 加载对应的 `CustomTimer` 配置
- `skipTimer()` 完成时记录 `TimerSession`
- 移除对 `Screen.Timer` / `Screen.AOD` 的直接引用
- 保留核心计时逻辑不变

#### 4.5 TimerState 扩展

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/timerScreen/viewModel/TimerState.kt`

```kotlin
data class TimerState(
    // 保留现有字段
    val timerMode: TimerMode = TimerMode.FOCUS,
    val timeStr: String = "25:00",
    val totalTime: Long = 25 * 60,
    val timerRunning: Boolean = false,
    val nextTimerMode: TimerMode = TimerMode.SHORT_BREAK,
    val nextTimeStr: String = "5:00",
    val showBrandTitle: Boolean = true,
    val currentFocusCount: Int = 1,
    val totalFocusCount: Int = 4,
    val alarmRinging: Boolean = false,
    val serviceRunning: Boolean = false,
    val infiniteFocus: Boolean = false,
    // 新增
    val activeTimerName: String = "专注",    // 当前计时名称
    val activeTimerId: Long? = null          // 当前自定义计时ID
)

enum class TimerMode {
    FOCUS, SHORT_BREAK, LONG_BREAK, BRAND
    // 不新增 CUSTOM，通过 activeTimerId + activeTimerName 标识自定义类型
}
```

#### 4.6 UI

**新增 RecordsScreen**

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/recordsScreen/RecordsScreen.kt`

整体布局分为 3 个 Tab（顶部 TabRow）：

**Tab 1: 时长记录**
- 自定义计时类型选择器（水平滚动 Chip 组：工作/学习/休闲/...）
- 计时器核心 UI（复用现有 TimerScreen 的圆形进度条+控制按钮）
- 今日时长记录列表

**Tab 2: 次数记录**
- 计数卡片列表（每个卡片: 标题 + 今日计数 + 加减按钮）
- FAB 新建计数项
- 长按编辑/删除

**Tab 3: 数据统计**（合并原 StatsScreen）
- 时间维度切换: 本周 / 本月
- 时长记录占比饼图（各类自定义计时的时长百分比）
- 每日专注时长柱状图（复用 Vico 图表库）
- 总计数据卡片

**新增 AddCustomTimerSheet**

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/recordsScreen/AddCustomTimerSheet.kt`

- BottomSheet 形式
- 字段: 名称、图标、专注时长、短休时长、长休时长、循环次数、提醒/震动开关

**新增 AddCounterSheet**

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/recordsScreen/AddCounterSheet.kt`

- BottomSheet 形式
- 字段: 标题

#### 4.7 原有 Stats 页面处理

- **删除** `Screen.Stats` 及其子路由
- **删除** `StatsScreen` / `StatsViewModel` / `StatsState` 独立页面
- 统计功能整合到 `RecordsScreen` 的 Tab 3
- 复用 `StatRepository` 的数据查询逻辑

#### 4.8 原有 Timer 页面处理

- **删除** `Screen.Timer` / `Screen.AOD` 路由
- **删除** 独立的 `TimerScreen` 页面
- 计时 UI 组件（圆形进度条、控制按钮）提取为可复用 Composable
- 在 `RecordsScreen` 的时长记录 Tab 中复用

---

### 五、小组件全量迁移

**目标**: 原有 3 个小组件功能完整迁移适配新版本，新增任务列表小组件

#### 5.1 现有小组件适配

**TimerAppWidget 适配**

**文件**: `androidApp/src/main/java/org/nsh07/pomodoro/widget/TimerAppWidget.kt`

- 显示当前自定义计时名称（`activeTimerName`）
- 从 `StateRepository.timerState` 读取新字段
- 点击跳转目标改为 `Screen.Records.Timer`

**TodayAppWidget 适配**

**文件**: `androidApp/src/main/java/org/nsh07/pomodoro/widget/TodayAppWidget.kt`

- 新增今日待办任务数量显示
- 新增今日使用次数显示
- 点击跳转目标改为 `Screen.Records.Main`

**HistoryAppWidget 适配**

**文件**: `androidApp/src/main/java/org/nsh07/pomodoro/widget/HistoryAppWidget.kt`

- 数据源不变（仍使用 `StatRepository`）
- 点击跳转目标改为 `Screen.Records.Statistics`

#### 5.2 新增小组件

**新增 TaskListAppWidget**

**文件**: `androidApp/src/main/java/org/nsh07/pomodoro/widget/TaskListAppWidget.kt`

- 展示今日待办任务列表（前 3-5 项）
- 每项: 勾选框 + 标题 + 时间
- 可勾选完成（通过 `StartServiceAction` 类似的回调机制）
- 点击跳转到 `Screen.Tasks.Main`

**新增 TaskListWidgetReceiver**

**文件**: `androidApp/src/main/java/org/nsh07/pomodoro/widget/TaskListWidgetReceiver.kt`

#### 5.3 AndroidManifest 变更

**文件**: `androidApp/src/main/AndroidManifest.xml`

- 注册 `TaskListWidgetReceiver`
- 注册 `TaskReminderReceiver`

---

### 六、DI 变更

**文件**: `androidApp/src/main/java/org/nsh07/pomodoro/di/androidModules.kt`

新增注册：
- `TaskDao` → `TaskRepository`
- `NoteDao` → `NoteRepository`
- `TimerSessionDao` → `TimerSessionRepository`
- `CounterRecordDao` → `CounterRecordRepository`
- `CustomTimerDao` → `CustomTimerRepository`
- `TasksViewModel`
- `CollectionViewModel`
- `RecordsViewModel`

移除注册：
- `StatsViewModel`（功能合并到 RecordsViewModel）

---

### 七、构建依赖变更

**文件**: `shared/build.gradle.kts`

新增依赖：
- Markdown 渲染库: `com.mikepenz:multiplatform-markdown-renderer:{version}`（或类似库）

---

## 实施顺序

### Phase 1: 数据层基础
1. 新增所有 Entity 文件（Task, Note, TimerSession, CounterRecord, CounterEntry, CustomTimer）
2. 新增所有 DAO 文件
3. 新增所有 Repository 文件
4. 更新 AppDatabase（entities + DAOs + 版本升级）
5. 更新 DI 模块注册

### Phase 2: 导航重构
1. 更新 Screen.kt（新增路由，移除旧路由）
2. 更新 AppScreen.kt（底部导航 4 标签，移除 Timer/Stats/AOD entry）
3. 新增图标和字符串资源

### Phase 3: 计划任务页面
1. 实现 TasksState / TasksAction / TasksViewModel
2. 实现 TasksScreen（任务列表 + 日期选择器 + FAB）
3. 实现 AddEditTaskSheet（新建/编辑表单）
4. 实现提醒功能（TaskReminderScheduler + TaskReminderReceiver）

### Phase 4: 收集页面
1. 实现 CollectionState / CollectionAction / CollectionViewModel
2. 实现 CollectionScreen（笔记列表 + 搜索）
3. 实现 NoteEditScreen（Markdown 编辑器 + 预览）
4. 集成 Markdown 渲染库

### Phase 5: 记录页面
1. 扩展 TimerState（新增 activeTimerName / activeTimerId）
2. 实现 RecordsState / RecordsAction / RecordsViewModel
3. 实现 RecordsScreen（3 个 Tab）
4. 提取计时 UI 为可复用组件
5. 实现 AddCustomTimerSheet / AddCounterSheet
6. 适配 TimerService（自定义计时配置 + session 记录）
7. 迁移 StatsScreen 统计功能到 RecordsScreen Tab 3
8. 删除独立 TimerScreen / StatsScreen / AOD 页面

### Phase 6: 小组件迁移
1. 适配 TimerAppWidget（自定义计时名称 + 跳转目标）
2. 适配 TodayAppWidget（任务数 + 次数 + 跳转目标）
3. 适配 HistoryAppWidget（跳转目标）
4. 新增 TaskListAppWidget + TaskListWidgetReceiver
5. 更新 AndroidManifest

### Phase 7: 集成验证
1. 全流程冒烟测试
2. 数据库迁移验证
3. 小组件功能验证

---

## 假设与决策

1. **TimerMode 不新增 CUSTOM 枚举值**: 通过 `activeTimerId` + `activeTimerName` 标识自定义类型，避免枚举扩展带来的大量 switch 分支改动
2. **重复任务实现**: 完成后自动生成下一次实例（snaptick 模式），而非运行时动态计算
3. **提醒实现**: 使用 `AlarmManager` + `BroadcastReceiver`，兼容 Android 12+ 精确闹钟限制
4. **Markdown 渲染**: 使用第三方库 `multiplatform-markdown-renderer`，支持 KMP
5. **数据库迁移**: Room AutoMigration 一步到位（v2→v3），新增表不影响现有数据
6. **计时 UI 复用**: 将 TimerScreen 中的圆形进度条+控制按钮提取为独立 Composable，在 RecordsScreen 中复用
7. **底部导航样式**: 保持现有 `HorizontalFloatingToolbar` 风格，4 标签自适应布局
8. **原有 Stats 数据兼容**: `Stat` 表和 `StatRepository` 保留，RecordsScreen 的统计 Tab 继续使用
9. **收集页面搜索**: 本地 SQL LIKE 查询，不引入全文检索引擎
10. **次数记录**: 每日每计数项一条 `CounterEntry`，支持手动增减

---

## 验证步骤

1. **数据库迁移**: 从 v2 升级到 v3，确保原有 Stat/Preference 数据无丢失
2. **计划任务 CRUD**: 创建→编辑→完成→删除，验证数据一致性
3. **重复任务**: 创建每周重复任务→完成→验证自动生成下一次
4. **提醒**: 设置到期提醒→验证通知触发
5. **收集 CRUD**: 新建笔记→编辑 Markdown→搜索→删除
6. **时长记录**: 选择自定义计时→启动→完成→验证 TimerSession 记录
7. **次数记录**: 新建计数项→增减计数→验证 CounterEntry 正确
8. **数据统计**: 切换周/月→验证图表数据正确
9. **小组件**: Timer 小组件显示自定义名称；Today 小组件显示任务数；TaskList 小组件展示待办
10. **旋转屏幕/进程恢复**: 验证各页面状态不丢失
