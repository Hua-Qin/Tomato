# 产品迭代优化计划：首页计时+专注模块+底部导航+小组件迁移

## 概述

基于 Tomato（Kotlin Multiplatform + Jetpack Compose + Material 3 Expressive）现有架构，实施四大功能模块的迭代优化。

---

## 现状分析

### 技术栈
- **框架**: Kotlin Multiplatform (KMP) + Jetpack Compose + Material 3 Expressive
- **导航**: Navigation 3 + `rememberNavBackStack`
- **数据库**: Room（entities: `Stat`, `IntPreference`, `BooleanPreference`, `StringPreference`）
- **DI**: Koin
- **小组件**: Glance (3个: `TimerAppWidget`, `TodayAppWidget`, `HistoryAppWidget`)
- **服务**: `TimerService` (前台服务，管理计时逻辑)

### 当前底部导航
3个标签页: Timer / Stats / Settings，使用 `HorizontalFloatingToolbar` 实现

### 当前计时模式
`TimerMode` 枚举: `FOCUS`, `SHORT_BREAK`, `LONG_BREAK`, `BRAND`
- 固定配置: focusTime / shortBreakTime / longBreakTime / sessionLength
- 无使用次数统计，无自定义分类

### 当前数据模型
- `Stat`: 按日期+4个时段存储专注/休息时长
- `Preference`: 键值对存储设置
- 无任务/计划相关数据模型

---

## 变更计划

### 功能1: 首页计时模块 - 使用次数统计

**目标**: 新增计时使用次数统计，留存历史次数数据展示

#### 数据层变更

**新增 Entity: `TimerSession`**
- 文件: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/data/TimerSession.kt`
```kotlin
@Entity(tableName = "timer_session")
data class TimerSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timerType: String,        // "focus", "short_break", "long_break", 或自定义类型名
    val duration: Long,           // 实际时长(ms)
    val startedAt: Long,          // 开始时间戳
    val completedAt: Long?,       // 完成时间戳(未完成为null)
    val date: LocalDate           // 日期，便于按日查询
)
```

**新增 DAO: `TimerSessionDao`**
- 文件: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/data/TimerSessionDao.kt`
- 方法:
  - `insertSession(session)` - 插入记录
  - `getSessionsByDate(date): Flow<List<TimerSession>>` - 按日查询
  - `getSessionCountByDate(date): Flow<Int>` - 按日计数
  - `getSessionCountByDateAndType(date, type): Flow<Int>` - 按日+类型计数
  - `getLast30DaysSessionCounts(): Flow<List<SessionCount>>` - 近30天每日次数
  - `getTotalSessionCount(): Flow<Int>` - 总次数

**新增 Repository: `TimerSessionRepository`**
- 文件: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/data/TimerSessionRepository.kt`
- 接口 + `AppTimerSessionRepository` 实现

**数据库升级**
- 文件: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/data/AppDatabase.kt`
- 新增 `TimerSession` 到 entities，版本 2→3，添加 `AutoMigration(from=2, to=3)`

#### 业务逻辑变更

**TimerService 变更**
- 文件: `androidApp/src/main/java/org/nsh07/pomodoro/service/TimerService.kt`
- 在 `skipTimer()` 和计时完成时，调用 `timerSessionRepository.insertSession()` 记录本次计时

**TimerState 扩展**
- 文件: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/timerScreen/viewModel/TimerState.kt`
- 新增字段: `todaySessionCount: Int = 0`, `todayFocusCount: Int = 0`

**TimerViewModel 变更**
- 文件: `shared/src/androidMain/kotlin/org/nsh07/pomodoro/ui/timerScreen/viewModel/TimerViewModel.kt`
- 订阅 `todaySessionCount` 和 `todayFocusCount` Flow，更新 TimerState

#### UI 变更

**TimerScreen 变更**
- 文件: `shared/src/androidMain/kotlin/org/nsh07/pomodoro/ui/timerScreen/TimerScreen.kt`
- 在计时器下方（"Up Next" 区域上方）新增今日使用次数展示行
- 显示格式: "今日专注 X 次 · 共 Y 分钟"

**StatsScreen 变更**
- 文件: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/statsScreen/`
- 新增"使用次数"统计卡片，展示每日/每周/每月完成次数

#### DI 变更
- 文件: `androidApp/src/main/java/org/nsh07/pomodoro/di/androidModules.kt`
- 注册 `TimerSessionDao`, `TimerSessionRepository`

---

### 功能2: 专注计时自定义扩容

**目标**: 支持自主新建分类计时（工作、学习、休闲等），每项可单独配置时长、提醒、循环规则

#### 数据层变更

**新增 Entity: `CustomTimer`**
- 文件: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/data/CustomTimer.kt`
```kotlin
@Entity(tableName = "custom_timer")
data class CustomTimer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,              // 自定义名称，如"工作"、"学习"
    val iconKey: String,           // 图标标识
    val focusDuration: Long,       // 专注时长(ms)
    val shortBreakDuration: Long,  // 短休息时长(ms)
    val longBreakDuration: Long,   // 长休息时长(ms)
    val sessionLength: Int,        // 循环次数
    val alarmEnabled: Boolean,     // 是否提醒
    val alarmSoundUri: String?,    // 提醒铃声
    val vibrateEnabled: Boolean,   // 是否震动
    val autoStartNext: Boolean,    // 自动开始下一个
    val sortOrder: Int,            // 排序
    val isDefault: Boolean = false // 是否为默认(内置)类型
)
```

**新增 DAO: `CustomTimerDao`**
- 文件: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/data/CustomTimerDao.kt`
- CRUD 方法 + `getAllCustomTimers(): Flow<List<CustomTimer>>`

**新增 Repository: `CustomTimerRepository`**
- 文件: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/data/CustomTimerRepository.kt`

**数据库升级**: `AppDatabase` 新增 `CustomTimer` entity, 版本 3→4

#### 业务逻辑变更

**TimerMode 扩展**
- 文件: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/timerScreen/viewModel/TimerState.kt`
- `TimerMode` 新增 `CUSTOM(name: String)` 枚举值，或改为 sealed class
- `TimerState` 新增字段: `activeCustomTimerId: Long? = null`, `activeCustomTimerName: String? = null`

**TimerState 重构**
```kotlin
enum class TimerMode {
    FOCUS, SHORT_BREAK, LONG_BREAK, BRAND, CUSTOM
}

data class TimerState(
    // ... 现有字段
    val activeCustomTimerId: Long? = null,
    val activeCustomTimerName: String? = null,
    val customTimerList: List<CustomTimer> = emptyList()
)
```

**TimerService 变更**
- 支持根据 `activeCustomTimerId` 加载对应的时长配置
- `toggleTimer()` / `skipTimer()` 中使用自定义计时配置

**StateRepository 变更**
- 文件: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/data/StateRepository.kt`
- 加载自定义计时列表到 TimerState

#### UI 变更

**新增自定义计时选择器**
- 在 TimerScreen 顶部或计时器上方，新增水平滚动标签选择器
- 显示所有自定义计时类型（工作/学习/休闲/...），点击切换
- 选中项高亮，计时器自动切换对应配置

**新增自定义计时管理页面**
- 文件: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/timerScreen/CustomTimerManagerScreen.kt`
- 列表展示所有自定义计时类型
- 支持增删改操作
- 新建/编辑表单: 名称、图标、专注时长、短休时长、长休时长、循环次数、提醒设置

**SettingsScreen 变更**
- 在"Timer"设置子页面中新增"自定义计时"入口

---

### 功能3: 底部导航新增【计划任务】标签页

**目标**: 对标 snaptick 交互与布局，新增计划任务标签页，支持增删改查、周期重复、到期提醒

#### 数据层变更

**新增 Entity: `Task`**
- 文件: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/data/Task.kt`
```kotlin
@Entity(tableName = "task")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,               // 任务标题
    val description: String = "",     // 任务描述
    val isCompleted: Boolean = false, // 是否完成
    val dueDate: Long?,              // 到期日期时间戳
    val dueTime: Long?,              // 到期时间(一天内的秒数)
    val reminderEnabled: Boolean = false, // 到期提醒
    val reminderMinutesBefore: Int = 0,   // 提前提醒(分钟)
    val repeatRule: String = "none",      // 重复规则: none/daily/weekly/monthly/custom
    val repeatCustomDays: String = "",    // 自定义重复日(如"1,3,5"表示周一三五)
    val priority: Int = 0,               // 优先级 0-2
    val category: String = "",           // 分类标签
    val createdAt: Long,                 // 创建时间
    val completedAt: Long? = null,       // 完成时间
    val sortOrder: Int = 0               // 排序
)
```

**新增 DAO: `TaskDao`**
- 文件: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/data/TaskDao.kt`
- 方法:
  - `getAllTasks(): Flow<List<Task>>` - 全部任务
  - `getTasksByDate(date): Flow<List<Task>>` - 按日查询
  - `getPendingTasks(): Flow<List<Task>>` - 未完成任务
  - `getCompletedTasks(): Flow<List<Task>>` - 已完成任务
  - `insertTask(task)`, `updateTask(task)`, `deleteTask(task)`

**新增 Repository: `TaskRepository`**
- 文件: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/data/TaskRepository.kt`
- 包含重复任务生成逻辑: 当重复任务被完成后，自动生成下一次实例

**数据库升级**: `AppDatabase` 新增 `Task` entity, 版本 4→5

#### 导航变更

**Screen 扩展**
- 文件: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/Screen.kt`
- 新增:
```kotlin
@Serializable
sealed class Tasks : Screen() {
    @Serializable
    object Main : Tasks()

    @Serializable
    object AddTask : Tasks()

    @Serializable
    data class EditTask(val taskId: Long) : Tasks()
}
```

**AppScreen 变更**
- 文件: `androidApp/src/main/java/org/nsh07/pomodoro/ui/AppScreen.kt`
- `mainScreens` 列表新增 Tasks 标签页（插入到 Stats 和 Settings 之间）
- 新增 Task 相关图标资源 (如 `task_list` / `task_list_filled`)
- 新增 `TasksViewModel` 实例
- NavDisplay 新增 `entry<Screen.Tasks.Main>`, `entry<Screen.Tasks.AddTask>`, `entry<Screen.Tasks.EditTask>`

#### UI 变更

**新增 TasksScreen**
- 文件: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/tasksScreen/TasksScreen.kt`
- 布局对标 snaptick:
  - 顶部: 日期选择器 + 今日概览
  - 中部: 任务列表（按优先级/时间排序）
  - 底部: FAB 添加按钮
  - 任务项: 勾选框 + 标题 + 时间 + 重复标识 + 分类标签
  - 滑动操作: 左滑删除、右滑完成

**新增 AddEditTaskScreen**
- 文件: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/tasksScreen/AddEditTaskScreen.kt`
- 表单字段: 标题、描述、日期、时间、提醒、重复规则、优先级、分类
- 重复规则选择器: 每天/每周/每月/自定义星期
- 提醒配置: 提前 N 分钟提醒

**新增 TasksViewModel**
- 文件: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/tasksScreen/viewModel/TasksViewModel.kt`
- 管理任务列表状态、筛选、排序
- 处理任务增删改查操作
- 管理重复任务生成逻辑

**新增 TasksState**
- 文件: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/tasksScreen/viewModel/TasksState.kt`
- 状态: 任务列表、筛选条件、选中日期等

#### 提醒功能

**新增 TaskReminderScheduler**
- 文件: `androidApp/src/main/java/org/nsh07/pomodoro/service/TaskReminderScheduler.kt`
- 使用 `AlarmManager` + `BroadcastReceiver` 实现到期提醒
- 任务创建/更新时调度提醒
- 提醒触发时发送通知

**新增 TaskReminderReceiver**
- 文件: `androidApp/src/main/java/org/nsh07/pomodoro/service/TaskReminderReceiver.kt`
- 接收提醒广播，展示通知

#### DI 变更
- 注册 `TaskDao`, `TaskRepository`, `TasksViewModel`

---

### 功能4: 小组件全量迁移落地

**目标**: 原有3个小组件功能完整迁移适配新版本，同步联动自定义计时和计划任务数据

#### 现有小组件适配

**TimerAppWidget 适配**
- 文件: `androidApp/src/main/java/org/nsh07/pomodoro/widget/TimerAppWidget.kt`
- 显示当前自定义计时类型名称（如"工作中"、"学习中"）
- 从 `StateRepository.timerState` 读取 `activeCustomTimerName`

**TodayAppWidget 适配**
- 文件: `androidApp/src/main/java/org/nsh07/pomodoro/widget/TodayAppWidget.kt`
- 新增今日使用次数显示
- 新增今日待办任务数量显示

**HistoryAppWidget 适配**
- 文件: `androidApp/src/main/java/org/nsh07/pomodoro/widget/HistoryAppWidget.kt`
- 新增使用次数趋势数据

#### 新增小组件

**新增 TaskListAppWidget**
- 文件: `androidApp/src/main/java/org/nsh07/pomodoro/widget/TaskListAppWidget.kt`
- 展示今日待办任务列表（前3-5项）
- 可勾选完成
- 点击跳转到 Tasks 页面

**新增 TaskListWidgetReceiver**
- 文件: `androidApp/src/main/java/org/nsh07/pomodoro/widget/TaskListWidgetReceiver.kt`

**新增 CustomTimerWidget**（可选，视复杂度）
- 展示自定义计时类型快捷入口
- 一键启动对应计时

#### AndroidManifest 变更
- 注册 `TaskListWidgetReceiver`
- 注册 `TaskReminderReceiver`

---

## 实施顺序

1. **Phase 1 - 数据层基础** (功能1+2+3的数据模型)
   - 新增所有 Entity/DAO/Repository
   - 数据库升级迁移
   - DI 注册

2. **Phase 2 - 功能1: 使用次数统计**
   - TimerService 记录逻辑
   - TimerScreen UI 展示
   - StatsScreen 统计卡片

3. **Phase 3 - 功能2: 自定义计时**
   - TimerMode 扩展
   - 自定义计时选择器 UI
   - 自定义计时管理页面
   - TimerService 适配

4. **Phase 4 - 功能3: 计划任务**
   - 底部导航新增标签页
   - TasksScreen + AddEditTaskScreen
   - 重复任务逻辑
   - 提醒功能

5. **Phase 5 - 功能4: 小组件迁移**
   - 现有小组件适配
   - 新增 TaskListAppWidget
   - 数据联动验证

---

## 假设与决策

1. **TimerMode 扩展方式**: 选择在枚举中新增 `CUSTOM` 值，通过 `activeCustomTimerId` 关联具体配置，而非将 TimerMode 改为 sealed class（改动最小化）
2. **重复任务实现**: 采用 snaptick 模式——完成任务后自动生成下一次实例，而非运行时动态计算
3. **提醒实现**: 使用 `AlarmManager` + `BroadcastReceiver`，兼容 Android 12+ 的精确闹钟限制
4. **数据库迁移**: 采用 Room AutoMigration，每步版本递增
5. **小组件框架**: 继续使用 Glance，保持与现有架构一致
6. **自定义计时图标**: 使用 Material Icons 内置图标集，通过 iconKey 映射，不引入额外图标库
7. **底部导航样式**: 保持现有 `HorizontalFloatingToolbar` 风格，新增标签页融入现有设计

---

## 验证步骤

1. 数据库迁移测试: 从 v2 升级到 v5，确保无数据丢失
2. 计时次数统计: 完成一次专注后，验证 TimerSession 记录正确，UI 显示次数正确
3. 自定义计时: 新建自定义类型→切换→启动计时→验证时长配置生效
4. 计划任务 CRUD: 创建→编辑→完成→删除，验证数据一致性
5. 重复任务: 创建每周重复任务→完成→验证自动生成下一次
6. 提醒: 设置到期提醒→验证通知触发
7. 小组件: Timer 小组件显示自定义类型名；Today 小组件显示次数和待办；TaskList 小组件展示任务列表
8. 旋转屏幕/进程恢复: 验证状态不丢失
