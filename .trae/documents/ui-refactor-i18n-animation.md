# Tomato App 全面重构计划：UI + 中文化 + 动画优化

## 概述

修复上一轮 AI 生成的代码三大问题：(1) 新功能 UI 硬编码英文/中文，未适配语言包体系；(2) UI 布局粗糙，需全面重构对齐项目已有的 Material 3 Expressive 风格；(3) 动画卡顿优化。

---

## 当前状态分析

### 硬编码字符串问题（已确认的具体位置）

| 文件 | 行号 | 硬编码内容 | 类型 |
|------|------|-----------|------|
| RecordsScreen.kt | 234 | `"暂无记录"` | 中文硬编码 |
| RecordsScreen.kt | 439 | `"暂无计数器"` | 中文硬编码 |
| NoteEditScreen.kt | 187 | `"Start writing..."` | 英文硬编码 |
| AddCustomTimerSheet.kt | 120,131,147 | `"min"` 后缀 | 英文硬编码 |
| AddCustomTimerSheet.kt | 214 | `"专注"` 空白回退 | 中文硬编码 |

### 中文翻译缺失

`values-zh-rCN/strings.xml` 缺少以下所有新增 key 的翻译：tasks, collection, records, add_task, edit_task, task_title, task_description, due_date, due_time, reminder, repeat, repeat_none, repeat_daily, repeat_weekly, repeat_monthly, repeat_custom, priority, priority_none, priority_low, priority_medium, priority_high, category, completed_tasks, pending_tasks, no_tasks, add_note, edit_note, note_title, search_notes, no_notes, pin_note, unpin_note, duration_record, counter_record, statistics, add_custom_timer, add_counter, timer_name, counter_title, this_week, this_month, today_count, minutes_before, preview, delete, task_list_widget_desc, pending_tasks_count, sessions_count, no_pending_tasks

### UI 质量问题

- **TasksScreen**: 周历选择器功能完整但样式基础；任务项使用 Checkbox 而非圆形勾选框；无滑动手势；FAB 用 `LargeExtendedFloatingActionButton` 风格尚可
- **CollectionScreen**: NoteCard 无 elevation；搜索栏用 `OutlinedTextField` 而非 Material 3 SearchBar；置顶标识仅小图标无背景区分
- **RecordsScreen**: TimerDisplay 是简化版（无 wavy progress、无 shared transitions）；CounterCard 布局简单；StatisticsTab 几乎是占位符
- **AddEditTaskSheet**: 基本结构正确，但间距不统一（24dp padding vs 12dp spacing）
- **NoteEditScreen**: Markdown 渲染仅做语法剥离；格式化工具栏简陋

### 动画问题

- 底部导航栏 `HorizontalFloatingToolbar` 设置 `expanded = true`（始终展开），`Crossfade` 切换图标 + `AnimatedVisibility` 展开文字
- 缺少 `itemContentType` 和 `key` 在部分 LazyColumn 中
- 无任务完成动画、无滑动手势动画
- State 类已有 `@Immutable` 注解（RecordsState, TasksState, CollectionState）

---

## 修改计划

### 第一步：中文化 — 补全 strings.xml 翻译

**文件**: `shared/src/commonMain/composeResources/values-zh-rCN/strings.xml`

在文件末尾 `</resources>` 前追加所有缺失的中文翻译 key：

```xml
<string name="tasks">计划任务</string>
<string name="collection">收集</string>
<string name="records">记录</string>
<string name="add_task">新建任务</string>
<string name="edit_task">编辑任务</string>
<string name="task_title">任务标题</string>
<string name="task_description">描述</string>
<string name="due_date">截止日期</string>
<string name="due_time">截止时间</string>
<string name="reminder">提醒</string>
<string name="repeat">重复</string>
<string name="repeat_none">不重复</string>
<string name="repeat_daily">每天</string>
<string name="repeat_weekly">每周</string>
<string name="repeat_monthly">每月</string>
<string name="repeat_custom">自定义</string>
<string name="priority">优先级</string>
<string name="priority_none">无</string>
<string name="priority_low">低</string>
<string name="priority_medium">中</string>
<string name="priority_high">高</string>
<string name="category">分类</string>
<string name="completed_tasks">已完成</string>
<string name="pending_tasks">待处理</string>
<string name="no_tasks">暂无任务</string>
<string name="add_note">新建笔记</string>
<string name="edit_note">编辑笔记</string>
<string name="note_title">标题</string>
<string name="search_notes">搜索笔记</string>
<string name="no_notes">暂无笔记</string>
<string name="pin_note">置顶</string>
<string name="unpin_note">取消置顶</string>
<string name="duration_record">时长记录</string>
<string name="counter_record">次数记录</string>
<string name="statistics">数据统计</string>
<string name="add_custom_timer">新建计时</string>
<string name="add_counter">新建计数</string>
<string name="timer_name">计时名称</string>
<string name="counter_title">计数标题</string>
<string name="this_week">本周</string>
<string name="this_month">本月</string>
<string name="today_count">今日: %1$d</string>
<string name="minutes_before">分钟前</string>
<string name="preview">预览</string>
<string name="delete">删除</string>
<string name="task_list_widget_desc">今日待办任务</string>
<string name="pending_tasks_count">%1$d 项待办</string>
<string name="sessions_count">%1$d 次计时</string>
<string name="no_pending_tasks">暂无待办任务</string>
```

同时需要在英文 `values/strings.xml` 中新增硬编码字符串对应的 key：
- `no_records` → "No records yet"
- `no_counters` → "No counters yet"
- `start_writing` → "Start writing..."
- `minutes_suffix` → "min"
- `default_timer_name` → "Focus"

### 第二步：替换硬编码字符串

**文件: RecordsScreen.kt**
- L234: `"暂无记录"` → `stringResource(Res.string.no_records)`
- L439: `"暂无计数器"` → `stringResource(Res.string.no_counters)`
- 需要添加 import: `tomato.shared.generated.resources.no_records`, `tomato.shared.generated.resources.no_counters`

**文件: NoteEditScreen.kt**
- L187: `"Start writing..."` → `stringResource(Res.string.start_writing)`
- 需要添加 import: `tomato.shared.generated.resources.start_writing`

**文件: AddCustomTimerSheet.kt**
- L120,131,147: `suffix = { Text("min") }` → `suffix = { Text(stringResource(Res.string.minutes_suffix)) }`
- L214: `name = name.ifBlank { "专注" }` → `name = name.ifBlank { stringResource(Res.string.default_timer_name) }`（注意：这里不能在非 @Composable 上下文调用 stringResource，需要改为在 onClick 之前获取默认名）
- 需要添加 import: `tomato.shared.generated.resources.minutes_suffix`, `tomato.shared.generated.resources.default_timer_name`

### 第三步：TasksScreen UI 重构

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/tasksScreen/TasksScreen.kt`

1. **周历选择器优化**：
   - 选中日期改用 `Surface(shape = CircleShape, color = colorScheme.primary, tonalElevation = 2.dp)` 增加层次感
   - 今天标记：在日期数字下方加一个小圆点指示器（`Box` + `CircleShape` + `colorScheme.primary`，3dp 大小）
   - 增大点击区域：每个日期项 `Modifier.size(56.dp)` 确保满足 48dp 最小触摸目标

2. **任务列表项重构**：
   - 将 `Checkbox` 替换为自定义圆形勾选框：`Surface(shape = CircleShape)` + `AnimatedVisibility` 勾选图标
   - 任务项添加 `divider`：在 `LazyColumn` 的 `items` 后添加 `HorizontalDivider`
   - 右侧删除按钮改为 `IconButton`，使用 `Res.drawable.delete` 图标

3. **滑动手势**：
   - 使用 `Modifier.swipeToDismiss()` 或自定义 `Modifier.pointerInput` + `Animatable` 实现左滑删除/右滑完成
   - 由于 `swipeToDismiss` 是 Material 3 实验性 API，采用自定义实现：`Modifier.offset` + `Animatable` + `pointerInput`
   - 滑动背景：左滑红色（删除），右滑绿色（完成），使用 `Box` + `background` 层

4. **空状态优化**：
   - 增大图标到 80dp，降低 opacity
   - 添加副标题文字引导用户操作

5. **LazyColumn 优化**：
   - 添加 `itemContentType = { "task" }` 到 items 调用

### 第四步：AddEditTaskSheet UI 优化

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/tasksScreen/AddEditTaskSheet.kt`

1. **统一间距**：将 `verticalArrangement = Arrangement.spacedBy(12.dp)` 改为 `spacedBy(16.dp)`
2. **输入框样式**：统一使用 `shapes.large` 圆角（当前 OutlinedTextField 无 shape 参数）
3. **日期/时间选择**：已使用 DatePickerDialog/TimeInput，保持不变
4. **SegmentedButton**：已使用 SingleChoiceSegmentedButtonRow，保持不变
5. **保存按钮**：改为 `FilledTonalButton` + `ButtonDefaults.shapes()` 保持与项目一致

### 第五步：CollectionScreen UI 重构

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/collectionScreen/CollectionScreen.kt`

1. **NoteCard 重构**：
   - 添加 `tonalElevation = 1.dp` 增加层次感
   - 置顶笔记：添加 `border = BorderStroke(1.dp, colorScheme.primary)` 或背景色区分
   - 置顶标识：在卡片左上角添加小钉子图标 + 浅色背景条

2. **搜索栏升级**：
   - 将 `OutlinedTextField` 替换为 Material 3 `SearchBar` 组件
   - 使用 `SearchBar` + `SearchBarDefaults` 标准样式

3. **卡片间距**：将 `Arrangement.spacedBy(2.dp)` 改为 `spacedBy(8.dp)` 增加呼吸感

4. **LazyColumn 优化**：
   - 添加 `itemContentType = { "note" }`

### 第六步：NoteEditScreen UI 优化

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/collectionScreen/NoteEditScreen.kt`

1. **格式化工具栏优化**：
   - 将 `TextButton` 改为 `IconButton` + `Surface(shape = CircleShape)` 包裹
   - 增大点击区域到 48dp
   - 添加选中状态高亮

2. **编辑器样式**：
   - 内容区域添加 `verticalScroll(rememberScrollState())`
   - placeholder 颜色使用 `colorScheme.onSurfaceVariant`

### 第七步：RecordsScreen UI 重构

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/recordsScreen/RecordsScreen.kt`

1. **时长记录 Tab 优化**：
   - Timer 类型选择器改为 `LazyRow` + `FilterChip` 水平滚动
   - TimerDisplay：保持当前简化版（不引入 wavy progress，避免与 TimerScreen 耦合），但优化间距和布局
   - 控制按钮：使用 `FilledIconToggleButton` + `IconButtonDefaults.toggleableShapes()` 对齐 TimerScreen 风格

2. **次数记录 Tab 优化**：
   - CounterCard 改为网格布局（`LazyVerticalGrid`，2列）
   - 每个卡片内：大字号计数居中 + 下方加减按钮行
   - 添加 `AnimatedContent` 切换计数数字动画

3. **数据统计 Tab 优化**：
   - 周期切换使用 `SingleChoiceSegmentedButtonRow` 替代两个 `FilledTonalButton`
   - 添加实际的统计数据展示（复用已有数据）
   - SummaryCard 添加 `tonalElevation = 1.dp`

4. **Tab 切换动画**：
   - 已使用 `HorizontalPager` + `PrimaryTabRow`，保持不变
   - 添加 `pageContent` 的 `key` 和 `contentType`

### 第八步：AddCustomTimerSheet / AddCounterSheet 优化

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/recordsScreen/AddCustomTimerSheet.kt`

1. 统一间距为 16dp
2. 输入框统一 `shapes.large`
3. 替换硬编码字符串（见第二步）

**文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/recordsScreen/AddCounterSheet.kt`

1. 统一间距为 16dp
2. 输入框统一 `shapes.large`

### 第九步：底部导航栏动画优化

**文件**: `androidApp/src/main/java/org/nsh07/pomodoro/ui/AppScreen.kt`

1. **动画参数调优**：
   - `Crossfade` 图标切换：添加 `animationSpec = tween(300, FastOutSlowInEasing)` 确保流畅
   - `AnimatedVisibility` 文字展开：当前使用 `motionScheme.defaultSpatialSpec()`，改为显式 `spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)` 提供弹性效果
   - 底部栏整体 `AnimatedVisibility`：当前使用 `motionScheme.slowSpatialSpec()`，保持不变（已合理）

2. **避免 recomposition**：
   - 将 `animateColorAsState` 的颜色计算移到 `remember` 块中，避免在动画 lambda 中读取 state
   - `derivedStateOf` 已用于 `selected` 状态，保持不变

3. **ToggleButton 优化**：
   - 添加 `Modifier.height(56.dp)` 确保触摸区域
   - 文字 `fontSize = 16.sp` 保持不变

### 第十步：通用动画性能优化

**涉及文件**: 所有新功能页面

1. **LazyColumn 优化**：
   - 所有 `items()` 调用添加 `key = { it.id }`（已有部分）
   - 添加 `itemContentType = { "item_type" }`
   - 示例：TasksScreen 的 pendingTasks items 添加 `itemContentType = { "pending_task" }`

2. **列表项动画**：
   - 任务完成动画：在 TaskItem 中使用 `AnimatedContent` 切换勾选/未勾选状态
   - 计数器数字变化：在 CounterCard 中使用 `AnimatedContent(targetState = count)` 切换数字

3. **页面切换**：
   - 已使用 `NavDisplay` + `fadeIn/fadeOut`，保持不变

4. **BottomSheet**：
   - 已使用 `ModalBottomSheet` 标准动画，保持不变

---

## 修改文件清单

| # | 文件路径 | 修改类型 |
|---|---------|---------|
| 1 | `shared/src/commonMain/composeResources/values-zh-rCN/strings.xml` | 追加中文翻译 |
| 2 | `shared/src/commonMain/composeResources/values/strings.xml` | 新增缺失 key |
| 3 | `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/recordsScreen/RecordsScreen.kt` | 替换硬编码 + UI 重构 |
| 4 | `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/collectionScreen/NoteEditScreen.kt` | 替换硬编码 + UI 优化 |
| 5 | `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/recordsScreen/AddCustomTimerSheet.kt` | 替换硬编码 + 间距统一 |
| 6 | `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/recordsScreen/AddCounterSheet.kt` | 间距统一 |
| 7 | `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/tasksScreen/TasksScreen.kt` | UI 重构 |
| 8 | `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/tasksScreen/AddEditTaskSheet.kt` | UI 优化 |
| 9 | `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/collectionScreen/CollectionScreen.kt` | UI 重构 |
| 10 | `androidApp/src/main/java/org/nsh07/pomodoro/ui/AppScreen.kt` | 动画优化 |

---

## 不修改的文件

- TimerScreen.kt — 原有稳定代码
- SettingsScreen.kt — 原有稳定代码
- 所有 ViewModel/State/Action 文件 — 已有 @Immutable 注解，逻辑不变
- 所有 DAO/Repository/Entity 文件 — 数据层不变
- Screen.kt — 导航结构不变

---

## 验证步骤

1. 编译检查：`./gradlew :androidApp:assembleDebug` 确保无编译错误
2. 字符串完整性：grep 所有新页面文件，确认无硬编码字符串残留
3. 中文翻译完整性：对比 `values/strings.xml` 和 `values-zh-rCN/strings.xml`，确认所有 key 都有对应翻译
4. UI 视觉检查：在模拟器上运行，检查各页面布局、间距、动画是否流畅
5. Compose Resources 引用：确认所有新增的 `Res.string.xxx` 和 `Res.drawable.xxx` 都有正确的 import

---

## 假设与决策

1. **不引入新依赖**：所有修改使用现有 Material 3 + Compose API
2. **不修改原有功能**：TimerScreen、SettingsScreen 等保持不变
3. **增量修改**：不删除文件重写，而是 patch 修改
4. **滑动手势实现**：使用自定义 `Modifier.pointerInput` + `Animatable`，而非引入 `material3-wear` 的 `SwipeToDismissBox`
5. **SearchBar 组件**：使用 Material 3 `SearchBar`（`ExperimentalMaterial3Api`），需要 `@OptIn` 注解
6. **CounterTab 网格布局**：使用 `LazyVerticalGrid` 替代 `LazyColumn`，2列布局
7. **AddCustomTimerSheet 的 "专注" 回退值**：改为在 Composable 作用域获取默认名字，再传入 onClick lambda
