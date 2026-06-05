# 第三轮 UI 优化和功能修复计划

## 摘要

基于前序会话已完成的工作，本轮需要修复一个编译错误并验证所有4项功能优化是否正确实现。前序会话已完成了大部分代码修改，但最后一次 CI 构建因 `Unresolved reference 'Res'` 编译错误而失败。

## 当前状态分析

### 已完成的修改（代码已写入但未通过 CI 验证）

1. **底部栏动画优化** - AppScreen.kt 中已将 `Crossfade` 替换为 `AnimatedContent` + `scaleIn/scaleOut` + `fadeIn/fadeOut`，文字展开使用 `spring(DampingRatioMediumBouncy, StiffnessLow)` 弹性动画
2. **文本编辑功能修复** - NoteEditScreen.kt 中已实现 `renderMarkdownAnnotated()` 富文本渲染、`FormatButton` 改为 `TextButton` + 触觉反馈、编辑笔记时底部栏只显示返回按钮
3. **设置按钮尺寸** - SettingsState/SettingsAction/SettingsViewModel/AppearanceSettings/StateRepository 中已添加 `buttonSizeScale` 完整链路，RecordsScreen 中已应用 `buttonScale` 到控制按钮尺寸
4. **番茄钟无限专注模式** - RecordsScreen.kt 中已实现长按触发无限模式、`AnimatedVisibility` 隐藏进度环、`animateFloatAsState` 字号动画、`AnimatedContent` 显示"无限专注"标签

### 当前编译错误

- **文件**: [AppScreen.kt](file:///workspace/androidApp/src/main/java/org/nsh07/pomodoro/ui/AppScreen.kt#L267)
- **错误**: `Unresolved reference 'Res'` — 第267行使用了 `org.nsh07.pomodoro.ui.Res.drawable.arrow_back` 而非 `Res.drawable.arrow_back`
- **修复**: 将 `org.nsh07.pomodoro.ui.Res.drawable.arrow_back` 改为 `Res.drawable.arrow_back`（文件已有 `import tomato.shared.generated.resources.Res` 和 `import tomato.shared.generated.resources.arrow_back`）

## 修改计划

### 步骤 1：修复 AppScreen.kt 编译错误

**文件**: `/workspace/androidApp/src/main/java/org/nsh07/pomodoro/ui/AppScreen.kt`

**修改内容**:
- 第267行：`painterResource(org.nsh07.pomodoro.ui.Res.drawable.arrow_back)` → `painterResource(Res.drawable.arrow_back)`

**原因**: `org.nsh07.pomodoro.ui` 包下没有 `Res` 对象，`Res` 来自 `tomato.shared.generated.resources.Res`，已在文件顶部正确导入。

### 步骤 2：验证所有功能实现的完整性

检查以下文件确保所有修改正确：

1. **AppScreen.kt** - 底部栏动画、编辑笔记时隐藏导航 tabs
2. **NoteEditScreen.kt** - Markdown 富文本预览、格式按钮、预览切换
3. **RecordsScreen.kt** - 无限专注模式、buttonScale 应用
4. **AppearanceSettings.kt** - 按钮大小 Slider
5. **SettingsViewModel.kt** - `saveButtonSizeScale` 处理
6. **StateRepository.kt** - `buttonSizeScale` 加载

### 步骤 3：提交推送并触发 CI 构建

- 提交修复并推送到远程仓库
- 触发 GitHub Actions CI 构建验证
- 确认 APK 构建成功

## 假设与决策

- 所有4项功能的代码修改已在之前的会话中完成，只需修复编译错误
- 不需要新增功能或额外优化
- CI 构建使用已有的 GitHub Actions workflow
- 使用 `gh` CLI 或 REST API 触发构建

## 验证步骤

1. 本地确认 AppScreen.kt 第267行修复后无语法错误
2. 推送代码到远程仓库
3. 触发 CI 构建并监控构建状态
4. 确认构建成功，APK 可下载
