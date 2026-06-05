/*
 * Copyright (c) 2025-2026 Nishant Mishra
 *
 * This file is part of Tomato - a minimalist pomodoro timer for Android.
 *
 * Tomato is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Tomato is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Tomato.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.tasksScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.nsh07.pomodoro.data.Task
import org.nsh07.pomodoro.ui.tasksScreen.viewModel.TasksAction
import org.nsh07.pomodoro.ui.tasksScreen.viewModel.TasksViewModel
import org.nsh07.pomodoro.ui.theme.CustomColors
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.add_task
import tomato.shared.generated.resources.alarm
import tomato.shared.generated.resources.check
import tomato.shared.generated.resources.clear
import tomato.shared.generated.resources.completed_tasks
import tomato.shared.generated.resources.delete
import tomato.shared.generated.resources.flag
import tomato.shared.generated.resources.no_tasks
import tomato.shared.generated.resources.no_tasks_hint
import tomato.shared.generated.resources.pending_tasks
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TasksScreen(
    contentPadding: PaddingValues,
    onAction: (TasksAction) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = koinViewModel()
) {
    val tasksState by viewModel.tasksState.collectAsStateWithLifecycle()

    val topBarColors = CustomColors.topBarColors
    val weekStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    val weekDays = remember {
        buildList {
            val cal = weekStart.clone() as Calendar
            repeat(7) {
                add(cal.timeInMillis)
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
    }

    val dayFormatter = remember { SimpleDateFormat("d", Locale.getDefault()) }
    val weekdayFormatter = remember { SimpleDateFormat("EEE", Locale.getDefault()) }
    val monthFormatter = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            TopAppBar(
                title = {
                    Text(
                        monthFormatter.format(Date(tasksState.selectedDate)),
                        style = typography.titleLarge
                    )
                },
                colors = topBarColors
            )

            // Week calendar
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(weekDays) { dayMillis ->
                    val isSelected = isSameDay(dayMillis, tasksState.selectedDate)
                    val isToday = isSameDay(dayMillis, System.currentTimeMillis())
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) colorScheme.primary
                        else if (isToday) colorScheme.primaryContainer
                        else colorScheme.surface,
                        tonalElevation = if (isSelected) 2.dp else 0.dp,
                        modifier = Modifier
                            .size(56.dp)
                            .clickable {
                                onAction(TasksAction.SelectDate(dayMillis))
                            }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = weekdayFormatter.format(Date(dayMillis)),
                                style = typography.labelSmall,
                                color = if (isSelected) colorScheme.onPrimary
                                else colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = dayFormatter.format(Date(dayMillis)),
                                style = typography.titleMedium,
                                color = if (isSelected) colorScheme.onPrimary
                                else colorScheme.onSurface
                            )
                            if (isToday && !isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(3.dp)
                                        .background(colorScheme.primary, CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Task list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Pending tasks
                if (tasksState.pendingTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(Res.string.pending_tasks),
                            style = typography.labelMedium,
                            color = colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(
                        tasksState.pendingTasks,
                        key = { it.id },
                        contentType = { "pending_task" }
                    ) { task ->
                        SwipeableTaskItem(
                            task = task,
                            onCheck = { onAction(TasksAction.CompleteTask(task.id)) },
                            onClick = { onAction(TasksAction.EditTask(task)) },
                            onDelete = { onAction(TasksAction.DeleteTask(task.id)) }
                        )
                        HorizontalDivider(
                            color = colorScheme.outlineVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Completed tasks
                if (tasksState.completedTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(Res.string.completed_tasks),
                            style = typography.labelMedium,
                            color = colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(
                        tasksState.completedTasks,
                        key = { it.id },
                        contentType = { "completed_task" }
                    ) { task ->
                        SwipeableTaskItem(
                            task = task,
                            onCheck = { onAction(TasksAction.UncompleteTask(task.id)) },
                            onClick = { onAction(TasksAction.EditTask(task)) },
                            onDelete = { onAction(TasksAction.DeleteTask(task.id)) }
                        )
                        HorizontalDivider(
                            color = colorScheme.outlineVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Empty state
                if (tasksState.pendingTasks.isEmpty() && tasksState.completedTasks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(Res.drawable.check),
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = colorScheme.outlineVariant.copy(alpha = 0.6f)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = stringResource(Res.string.no_tasks),
                                    style = typography.bodyLarge,
                                    color = colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = stringResource(Res.string.no_tasks_hint),
                                    style = typography.bodyMedium,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // FAB
        LargeExtendedFloatingActionButton(
            onClick = { onAction(TasksAction.ShowAddDialog) },
            containerColor = colorScheme.primaryContainer,
            contentColor = colorScheme.onPrimaryContainer,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 16.dp,
                    bottom = contentPadding.calculateBottomPadding() + ScreenOffset
                )
        ) {
            Icon(
                painter = painterResource(Res.drawable.check),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text(stringResource(Res.string.add_task))
        }
    }

    // Add task dialog
    if (tasksState.showAddDialog) {
        AddEditTaskSheet(
            task = null,
            onDismiss = { onAction(TasksAction.HideAddDialog) },
            onSave = { task -> onAction(TasksAction.AddTask(task)) }
        )
    }

    // Edit task dialog
    tasksState.editingTask?.let { editingTask ->
        AddEditTaskSheet(
            task = editingTask,
            onDismiss = { onAction(TasksAction.HideEditDialog) },
            onSave = { task -> onAction(TasksAction.UpdateTask(task)) }
        )
    }
}

@Composable
private fun SwipeableTaskItem(
    task: Task,
    onCheck: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val swipeThreshold = 150f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Background swipe indicators
        Row(
            modifier = Modifier
                .matchParentSize()
                .background(
                    color = if (offsetX.value < -swipeThreshold) colorScheme.error
                    else if (offsetX.value > swipeThreshold) colorScheme.primary
                    else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 24.dp),
            horizontalArrangement = if (offsetX.value < 0) Arrangement.Start else Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (offsetX.value < -swipeThreshold) {
                Icon(
                    painter = painterResource(Res.drawable.delete),
                    contentDescription = null,
                    tint = colorScheme.onError
                )
            } else if (offsetX.value > swipeThreshold) {
                Icon(
                    painter = painterResource(Res.drawable.check),
                    contentDescription = null,
                    tint = colorScheme.onPrimary
                )
            }
        }

        // Foreground task item
        TaskItem(
            task = task,
            onCheck = onCheck,
            onClick = onClick,
            onDelete = onDelete,
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val currentOffset = offsetX.value
                            scope.launch {
                                if (currentOffset < -swipeThreshold) {
                                    onDelete()
                                } else if (currentOffset > swipeThreshold) {
                                    onCheck()
                                }
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                                )
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                offsetX.snapTo((offsetX.value + dragAmount).coerceIn(-300f, 300f))
                            }
                        }
                    )
                }
        )
    }
}

@Composable
private fun TaskItem(
    task: Task,
    onCheck: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surfaceBright,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Circular check box
            Surface(
                shape = CircleShape,
                color = if (task.isCompleted) colorScheme.primary else Color.Transparent,
                border = if (!task.isCompleted) androidx.compose.foundation.BorderStroke(2.dp, colorScheme.outline) else null,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onCheck)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    androidx.compose.animation.AnimatedVisibility(visible = task.isCompleted) {
                        Icon(
                            painter = painterResource(Res.drawable.check),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = colorScheme.onPrimary
                        )
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted) colorScheme.onSurfaceVariant else colorScheme.onSurface
                )
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (task.dueDate != null) {
                        val dueTimeStr = task.dueTime?.let {
                            String.format(Locale.getDefault(), "%02d:%02d", it / 60, it % 60)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(Res.drawable.alarm),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = colorScheme.outline
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = dueTimeStr ?: "",
                                style = typography.labelSmall,
                                color = colorScheme.outline
                            )
                        }
                    }
                    if (task.priority > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(Res.drawable.flag),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = when (task.priority) {
                                    1 -> colorScheme.outline
                                    2 -> colorScheme.tertiary
                                    3 -> colorScheme.error
                                    else -> colorScheme.outline
                                }
                            )
                        }
                    }
                    if (task.category.isNotEmpty()) {
                        Surface(
                            shape = CircleShape,
                            color = colorScheme.secondaryContainer,
                            modifier = Modifier.padding(0.dp)
                        ) {
                            Text(
                                text = task.category,
                                style = typography.labelSmall,
                                color = colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(Res.drawable.clear),
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun isSameDay(millis1: Long, millis2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = millis1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = millis2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
