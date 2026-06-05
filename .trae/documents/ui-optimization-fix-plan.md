# 应用功能优化与问题修复计划

## 概述
对 Tomato 应用进行 4 项功能优化和问题修复：底部栏动画、文本编辑修复、设置按钮尺寸生效、番茄钟无限专注模式增强。

## 现状分析

### 1. 底部栏动画
- **文件**: `androidApp/src/main/java/org/nsh07/pomodoro/ui/AppScreen.kt` (L257-323)
- **现状**: `HorizontalFloatingToolbar` 中 `ToggleButton` 使用 `Crossfade(tween(300))` 切换图标，`AnimatedVisibility(expandHorizontally/shrinkHorizontally + spring)` 展开文字
- **问题**: 图标切换使用 `Crossfade` 仅做淡入淡出，缺少缩放/位移过渡；文字展开的 spring 参数偏硬，视觉切换不够自然

### 2. 文本编辑功能
- **文件**: `shared/src/commonMain/kotlin/org/nsh07/pomodoro/ui/collectionScreen/NoteEditScreen.kt`
- **问题 A - 预览渲染**: `renderMarkdown()` (L305-314) 仅做正则替换剥离语法标记（如 `**bold**` → `bold`），不生成富文本样式，预览效果与编辑格式不对应
- **问题 B - 底部栏按钮**: `FormatButton` (L242-263) 使用 `Box + clickable`，没有触觉反馈和涟漪效果，点击体验差；`insertMarkdown()` (L297-299) 只在文本末尾追加语法，不处理光标位置
- **问题 C - 页面切换按钮**: NoteEditScreen 作为 `NavDisplay` 的独立 entry 显示，底部栏仍然可见，用户可能误点切换页面导致编辑内容丢失

### 3. 设置按钮尺寸
- **文件**: `SettingsState.kt` 定义了 `buttonSizeScale: Float = 1.0f`
- **问题**: `buttonSizeScale` 在整个 UI 代码中从未被引用/应用，设置滑块改变值后无任何按钮尺寸变化

### 4. 番茄钟无限专注模式
- **文件**: `shared/src/androidMain/kotlin/org/nsh07/pomodoro/ui/timerScreen/TimerScreen.kt`
- **原项目设计**: 首页 TimerScreen 的长按逻辑 (L385-398) — 长按时间区域触发 `TimerAction.SetInfiniteFocus(!infiniteFocus)`，有 `CircularProgressIndicator` → `AnimatedVisibility` 隐藏/显示切换，`animateFloatAsState` 控制时钟字号变化
- **当前 RecordsScreen**: `TimerDisplay` (L309-475) 已有长按播放按钮触发 `StartInfiniteMode`，但缺少：无限模式时进度环隐藏/显示动画、时间字号变化、"无限专注"标签显示

## 修改方案

### 修改 1: 底部栏动画优化
**文件**: `AppScreen.kt` (L296-321)

- 图标切换：`Crossfade` → `AnimatedContent` + `scaleIn/scaleOut`，添加缩放过渡
- 文字展开：调整 spring 参数为 `DampingRatioMediumBouncy + StiffnessLow`，更柔和
- 添加 `animateColorAsState` 给选中/未选中的容器颜色过渡

```kotlin
// 图标切换改为 AnimatedContent
AnimatedContent(
    selected,
    transitionSpec = {
        scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) +
            fadeIn(tween(200)) togetherWith
        scaleOut(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) +
            fadeOut(tween(200))
    }
) { isSelected ->
    Icon(
        painterResource(if (isSelected) item.selectedIcon else item.unselectedIcon),
        stringResource(item.label)
    )
}

// 文字展开调整 spring 参数
AnimatedVisibility(
    visible = selected || wide,
    enter = expandHorizontally(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)),
    exit = shrinkHorizontally(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
)
```

新增 imports: `AnimatedContent`, `scaleIn`, `scaleOut`, `fadeIn`, `fadeOut`, `spring`, `tween`

### 修改 2: 文本编辑功能修复

#### 2A: Markdown 预览渲染增强
**文件**: `NoteEditScreen.kt` — `renderMarkdown()` 函数 + 预览区域

将预览模式从纯文本替换改为 `AnnotatedString` 富文本渲染：

```kotlin
@Composable
fun renderMarkdownAnnotated(content: String): AnnotatedString {
    return buildAnnotatedString {
        // 逐行处理
        content.lines().forEachIndexed { index, line ->
            if (index > 0) append("\n")
            when {
                line.startsWith("### ") -> withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)) { append(line.removePrefix("### ")) }
                line.startsWith("## ") -> withStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)) { append(line.removePrefix("## ")) }
                line.startsWith("# ") -> withStyle(SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)) { append(line.removePrefix("# ")) }
                line.startsWith("- ") -> { append("• "); processInlineStyles(line.removePrefix("- ")) }
                else -> processInlineStyles(line)
            }
        }
    }
}

fun BuildAnnotatedString.processInlineStyles(text: String) {
    // 处理 **bold**, *italic*, `code` 的内联样式
    val regex = Regex("""(\*\*(.+?)\*\*|\*(.+?)\*|`(.+?)`)""")
    var lastIndex = 0
    regex.findAll(text).forEach { match ->
        append(text.substring(lastIndex, match.range.first))
        when {
            match.groupValues[2].isNotEmpty() -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(match.groupValues[2]) }
            match.groupValues[3].isNotEmpty() -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(match.groupValues[3]) }
            match.groupValues[4].isNotEmpty() -> withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = colorScheme.surfaceVariant)) { append(match.groupValues[4]) }
        }
        lastIndex = match.range.last + 1
    }
    append(text.substring(lastIndex))
}
```

预览区域使用 `Text(text = renderMarkdownAnnotated(content))` 替换原来的 `Text(text = renderMarkdown(content))`。

#### 2B: 格式按钮修复
**文件**: `NoteEditScreen.kt` — `FormatButton` + `insertMarkdown`

- `FormatButton`: `Box + clickable` → `TextButton`，添加涟漪效果
- `insertMarkdown`: 改为在光标位置插入（当前实现在末尾追加，需要接收 TextField 的 selection state）

由于 `OutlinedTextField` 不暴露 selection state，简化方案：保持末尾追加但修复空文本时的格式（避免 `**` 前缀出现在空行开头），并给 FormatButton 添加触觉反馈。

```kotlin
@Composable
private fun FormatButton(
    label: String,
    style: SpanStyle = SpanStyle(),
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    TextButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = Modifier.size(48.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = buildAnnotatedString { withStyle(style) { append(label) } },
            style = typography.titleSmall,
            color = colorScheme.onSurfaceVariant
        )
    }
}
```

#### 2C: 编辑页面隐藏底部栏切换按钮
**文件**: `AppScreen.kt`

在 `NavDisplay` 的 `entryProvider` 中，NoteEditScreen 的 entry 需要通知 AppScreen 隐藏底部栏的页面切换功能。方案：通过 `backStack` 状态判断 — 当 `backStack.lastOrNull()` 是 `Screen.Collection.AddNote` 或 `Screen.Collection.EditNote` 时，底部栏只显示返回按钮，不显示页面切换 tabs。

实现方式：在 `HorizontalFloatingToolbar` 中，判断当前是否在笔记编辑页面，如果是则隐藏 tabs，只显示一个返回按钮。

```kotlin
val isEditingNote = backStack.lastOrNull() is Screen.Collection.AddNote ||
                    backStack.lastOrNull() is Screen.Collection.EditNote

HorizontalFloatingToolbar(...) {
    if (isEditingNote) {
        // 只显示返回按钮
        IconButton(onClick = { if (backStack.size > 1) backStack.removeLastOrNull() }) {
            Icon(painterResource(Res.drawable.arrow_back), "Back")
        }
    } else {
        // 正常显示 tabs
        mainScreens.fastForEach { ... }
    }
}
```

### 修改 3: 设置按钮尺寸生效
**文件**: `RecordsScreen.kt` — 将 `buttonSizeScale` 应用到 FAB 和控制按钮

```kotlin
// 在 RecordsScreen 中读取 settingsState
val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()
val scale = settingsState.buttonSizeScale

// 应用到 SmallFloatingActionButton
SmallFloatingActionButton(
    modifier = Modifier.size((40 * scale).dp),  // 默认40dp * scale
    ...
)

// 应用到 TimerDisplay 的控制按钮
FilledIconToggleButton(
    modifier = Modifier.size((72 * scale).dp),
    ...
)
```

需要在 `RecordsScreen` 和 `TimerDisplay` 中传入 `buttonSizeScale` 参数。

**文件**: `SettingsViewModel.kt` — 确认 `buttonSizeScale` 保存/加载逻辑正确
**文件**: `StateRepository.kt` — 确认 `buttonSizeScale` 从 PreferenceRepository 正确读取

### 修改 4: 番茄钟无限专注模式增强
**文件**: `RecordsScreen.kt` — `TimerDisplay` composable

参考原项目 TimerScreen 的设计，增强 RecordsScreen 中的无限专注模式：

1. **无限模式时隐藏进度环**：用 `AnimatedVisibility` 包裹 `CircularProgressIndicator`，当 `infiniteFocus` 时隐藏
2. **时间字号变化**：用 `animateFloatAsState` 控制时间显示字号，无限模式时更大
3. **显示"无限专注"标签**：当 `infiniteFocus` 时，将模式标签替换为"无限专注"
4. **长按触觉反馈**：长按时添加 haptic feedback

```kotlin
// 进度环动画隐藏
AnimatedVisibility(
    !timerState.infiniteFocus,
    enter = fadeIn() + scaleIn(initialScale = 4f),
    exit = fadeOut() + scaleOut(targetScale = 4f)
) {
    CircularProgressIndicator(...)
}

// 时间字号动画
val clockFontSize by animateFloatAsState(
    if (timerState.infiniteFocus) 72f else 57f,
    label = "clockFontSize"
)
Text(
    text = timerState.timeStr,
    style = TextStyle(fontSize = clockFontSize.sp),
    fontWeight = FontWeight.Bold
)

// 无限专注标签
AnimatedContent(timerState.infiniteFocus) { infinite ->
    Text(
        text = if (infinite) stringResource(Res.string.infinite_focus)
        else when(timerState.timerMode) { ... },
        ...
    )
}

// 长按触觉反馈
onLongClick = {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    isInfiniteLongPressed = true
    onAction(RecordsAction.StartInfiniteMode)
}
```

新增 imports: `AnimatedContent`, `scaleIn`, `scaleOut`, `animateFloatAsState`, `LocalHapticFeedback`, `HapticFeedbackType`

## 假设与决策

1. **Markdown 渲染**：使用 `AnnotatedString` 而非引入第三方 Markdown 库，保持轻量且避免额外依赖
2. **按钮尺寸**：`buttonSizeScale` 仅应用于 RecordsScreen 的 FAB 和控制按钮，不全局应用（避免影响其他页面布局）
3. **底部栏编辑模式**：通过 `backStack` 状态判断，不引入新的状态管理机制
4. **无限模式动画**：参考原项目 TimerScreen 的设计模式，但适配 RecordsScreen 的布局
5. **FormatButton**：使用 `TextButton` 替代 `Box + clickable`，添加 haptic feedback 而非修改光标位置逻辑（后者需要重构 TextField 状态管理）

## 验证步骤

1. 底部栏：切换 tab 时图标应有缩放+淡入淡出过渡，文字展开应更柔和
2. 文本编辑：预览模式下加粗文本应显示为粗体、标题应显示为大号字、代码应显示为等宽字体
3. 格式按钮：点击应有涟漪效果和触觉反馈
4. 编辑页面底部栏：进入笔记编辑后底部栏只显示返回按钮
5. 设置按钮尺寸：拖动滑块后 RecordsScreen 的 FAB 和控制按钮尺寸应相应变化
6. 无限专注：长按播放按钮后进度环应动画隐藏，时间字号变大，显示"无限专注"标签
