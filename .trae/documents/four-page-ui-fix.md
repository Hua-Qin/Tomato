# Tomato App 四页面 UI 优化 + 功能修复计划

## 概述
对计划任务、收集、记录、底部栏四个模块进行 UI 优化和功能修复。

---

## 1. 计划任务页面优化

### 1.1 FAB 按钮改为小型图标按钮
**文件**: `TasksScreen.kt` L295-312
- 将 `LargeExtendedFloatingActionButton` 改为 `SmallFloatingActionButton`
- 只保留加号图标，移除文字
- 图标改为 `Res.drawable.add`（当前错误使用了 `Res.drawable.check`）

### 1.2 消除顶部多余空白
**文件**: `TasksScreen.kt` L136-144
- `TopAppBar` 使用 `title` 显示年月，默认有较大 padding
- 改为使用 `Column` 布局替代 TopAppBar，减少上方空白
- 或使用 `TopAppBarDefaults.topAppBarColors()` 设置 `scrolledContainerColor = Color.Transparent` 并减小 padding

### 1.3 优先级改为文字标注
**文件**: `TasksScreen.kt` L496-509
- 移除旗子图标（`Res.drawable.flag`），改为文字标签
- 优先级显示为：低→灰色文字"低"，中→tertiary色"中"，高→error色"高"
- 使用 `Surface(shape = CircleShape, color = ...)` + `Text` 标签样式，与 category 标签一致

### 1.4 截止日期/时间/提醒默认并行显示
**文件**: `AddEditTaskSheet.kt` L171-231
- 当前逻辑：截止日期→截止时间（条件显示）→提醒（条件显示），层层嵌套
- 改为：三个选项始终并行显示，各自带 Switch 开关
- 截止日期 Switch：关闭时不显示日期选择
- 截止时间 Switch：关闭时不显示时间选择
- 提醒 Switch：关闭时不启用提醒
- 移除 `hasDueDate` 对 `hasDueTime` 和 `reminderEnabled` 的条件依赖

### 1.5 修复"自定义"重复选项无响应
**文件**: `AddEditTaskSheet.kt` L111-118, L239-248
- 当前 `repeat_custom` 选项点击后只设置 `repeatRule = "custom"`，无后续 UI
- 需要添加自定义重复设置 UI：当 `repeatRule == "custom"` 时显示额外输入字段
- 添加 `customRepeatDays` 状态变量，显示星期选择器（7个 Chip，多选）
- 在 `SingleChoiceSegmentedButtonRow` 下方，当选择"自定义"时展开 `AnimatedVisibility` 显示星期选择

---

## 2. 收集页面优化

### 2.1 FAB 按钮改为小型图标按钮
**文件**: `CollectionScreen.kt` L212-241
- 将 `LargeExtendedFloatingActionButton` 改为 `SmallFloatingActionButton`
- 只保留加号图标，移除文字

### 2.2 修复新建笔记流程
**文件**: `CollectionScreen.kt` L213-224, `CollectionAction.kt`, `CollectionViewModel.kt`
- 当前：FAB 点击直接创建空白 Note 并插入数据库（`CollectionAction.AddNote(Note(...))`）
- 修复：改为导航到 NoteEditScreen，让用户编辑后再保存
- 需要新增 `CollectionAction.NavigateToAddNote` action
- 在 `AppScreen.kt` 中添加 `Screen.Collection.AddNote` 的 NavDisplay entry，指向 `NoteEditScreen(noteId = null, ...)`
- FAB onClick 改为 `{ onAction(CollectionAction.NavigateToAddNote) }`，在 AppScreen 中处理导航

### 2.3 修复笔记点击编辑
**文件**: `CollectionScreen.kt` L261-262
- 当前 `NoteCard` 的 `onClick` 为空 `{}`
- 修复：点击后导航到 `NoteEditScreen(noteId = note.id)`
- 需要新增 `CollectionAction.NavigateToEditNote(val noteId: Long)` action
- 长按菜单"编辑笔记"也需要触发导航

### 2.4 补全笔记编辑功能
**文件**: `NoteEditScreen.kt`, `CollectionAction.kt`
- `NoteEditScreen` 已有完整的编辑 UI，但导航未接入
- 需要在 `AppScreen.kt` 中添加 `Screen.Collection.AddNote` 和 `Screen.Collection.EditNote` 的 entry
- `NoteEditScreen` 的 `saveNote` 函数已正确处理新增/更新逻辑

---

## 3. 记录页面功能修复

### 3.1 修复时长记录控制按钮
**文件**: `RecordsScreen.kt` L375-415, `RecordsAction.kt`, `RecordsViewModel.kt`
- 当前 `FilledIconToggleButton` 的 `onCheckedChange` 为空 `{ /* Toggle play/pause */ }`
- 需要新增 `RecordsAction.ToggleTimer` action
- 在 `RecordsViewModel` 中实现：调用 `stateRepository` 的计时器控制逻辑
- 同样修复重启按钮（`onClick = { /* Reset */ }`）和跳过按钮（`onClick = { /* Skip */ }`）
- 新增 actions：`RecordsAction.ToggleTimer`, `RecordsAction.ResetTimer`, `RecordsAction.SkipTimer`

### 3.2 实现长按进入无限时间模式
**文件**: `RecordsScreen.kt` TimerDisplay
- 在播放/暂停按钮上添加 `Modifier.pointerInput` 检测长按
- 长按触发无限时间模式：设置 `totalTime = 0`，计时器不倒计时
- 新增 `RecordsAction.StartInfiniteMode` action
- 在 `TimerState` 中添加 `isInfiniteMode` 字段（或通过 `totalTime == 0L` 判断）

### 3.3 修复加号按钮无响应
**文件**: `RecordsScreen.kt` L230-242
- 当前加号 FilterChip 的 `onClick = { onAction(RecordsAction.ShowAddTimerSheet) }` 逻辑正确
- 但 `RecordsScreen` 中没有处理 `showAddTimerSheet` 状态来显示 `AddCustomTimerSheet`
- 需要在 `RecordsScreen` 底部添加：
  ```kotlin
  if (state.showAddTimerSheet) {
      AddCustomTimerSheet(
          onDismiss = { onAction(RecordsAction.HideAddTimerSheet) },
          onAction = onAction
      )
  }
  ```

### 3.4 修复新建计数按钮
**文件**: `RecordsScreen.kt` L478-480, L504-518
- 空状态的"新建计数"按钮调用 `RecordsAction.ShowAddCounterSheet`，逻辑正确
- 但 `RecordsScreen` 中没有处理 `showAddCounterSheet` 状态来显示 `AddCounterSheet`
- 需要在 `RecordsScreen` 底部添加：
  ```kotlin
  if (state.showAddCounterSheet) {
      AddCounterSheet(
          onDismiss = { onAction(RecordsAction.HideAddCounterSheet) },
          onAction = onAction
      )
  }
  ```

---

## 4. 底部栏动画修复

**文件**: `AppScreen.kt` L226-310
- 当前 `HorizontalFloatingToolbar` 设置 `expanded = true`（始终展开），没有展开/收起动画
- 需要将 `expanded` 绑定到滚动状态，使底部栏在向下滚动时收起、向上滚动时展开
- 使用 `toolbarScrollBehavior` 已有的 `exitAlwaysScrollBehavior`，但需要将 `expanded` 绑定到滚动状态
- 修改：`expanded = toolbarScrollBehavior.state == FloatingToolbarScrollState.Expanded`
- 或更简单：保持 `expanded = true`，但确保 `scrollBehavior` 正确工作
- 关键问题：当前 `scrollBehavior` 已设置但 `expanded` 硬编码为 `true`，覆盖了滚动行为
- 修复：将 `expanded = true` 改为 `expanded = toolbarScrollBehavior.isExpanded`（使用 `FloatingToolbarScrollState`）

---

## 修改文件清单

| # | 文件 | 修改内容 |
|---|------|---------|
| 1 | `TasksScreen.kt` | FAB改小+顶部空白+优先级文字+import |
| 2 | `AddEditTaskSheet.kt` | 日期/时间/提醒并行+自定义重复UI |
| 3 | `CollectionScreen.kt` | FAB改小+修复笔记导航+onClick |
| 4 | `CollectionAction.kt` | 新增导航 actions |
| 5 | `CollectionViewModel.kt` | 处理导航 actions |
| 6 | `AppScreen.kt` | 添加笔记编辑导航entry+底部栏动画修复 |
| 7 | `RecordsScreen.kt` | 添加sheet显示+控制按钮功能+长按无限模式 |
| 8 | `RecordsAction.kt` | 新增计时器控制 actions |
| 9 | `RecordsViewModel.kt` | 实现计时器控制逻辑 |
| 10 | `Screen.kt` | 无需修改（已有 AddNote/EditNote 路由） |

---

## 验证步骤
1. 编译通过
2. 计划任务：FAB为小圆形加号按钮；优先级显示为文字标签；日期/时间/提醒并行显示；自定义重复可选星期
3. 收集：FAB为小圆形加号按钮；点击FAB进入编辑页；点击笔记进入编辑页
4. 记录：播放/暂停/重启/跳过按钮可用；长按播放进入无限模式；加号弹出新建计时sheet；新建计数弹出sheet
5. 底部栏：滚动时展开/收起动画流畅
