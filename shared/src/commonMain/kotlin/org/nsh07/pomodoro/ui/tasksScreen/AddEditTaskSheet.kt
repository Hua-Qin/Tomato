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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.data.Task
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.add_task
import tomato.shared.generated.resources.cancel
import tomato.shared.generated.resources.category
import tomato.shared.generated.resources.due_date
import tomato.shared.generated.resources.due_time
import tomato.shared.generated.resources.edit_task
import tomato.shared.generated.resources.ok
import tomato.shared.generated.resources.priority
import tomato.shared.generated.resources.priority_high
import tomato.shared.generated.resources.priority_low
import tomato.shared.generated.resources.priority_medium
import tomato.shared.generated.resources.priority_none
import tomato.shared.generated.resources.reminder
import tomato.shared.generated.resources.repeat
import tomato.shared.generated.resources.repeat_custom
import tomato.shared.generated.resources.repeat_daily
import tomato.shared.generated.resources.repeat_monthly
import tomato.shared.generated.resources.repeat_none
import tomato.shared.generated.resources.repeat_weekly
import tomato.shared.generated.resources.task_description
import tomato.shared.generated.resources.task_title

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun AddEditTaskSheet(
    task: Task?,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var dueDate by remember { mutableLongStateOf(task?.dueDate ?: System.currentTimeMillis()) }
    var hasDueDate by remember { mutableStateOf(task?.dueDate != null) }
    var dueTime by remember { mutableIntStateOf(task?.dueTime ?: 540) } // 9:00 default
    var hasDueTime by remember { mutableStateOf(task?.dueTime != null) }
    var reminderEnabled by remember { mutableStateOf(task?.reminderEnabled ?: false) }
    var repeatRule by remember { mutableStateOf(task?.repeatRule ?: "none") }
    var priority by remember { mutableIntStateOf(task?.priority ?: 0) }
    var category by remember { mutableStateOf(task?.category ?: "") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val repeatOptions = listOf("none", "daily", "weekly", "monthly", "custom")
    val repeatLabels = listOf(
        stringResource(Res.string.repeat_none),
        stringResource(Res.string.repeat_daily),
        stringResource(Res.string.repeat_weekly),
        stringResource(Res.string.repeat_monthly),
        stringResource(Res.string.repeat_custom)
    )

    val priorityOptions = listOf(0, 1, 2, 3)
    val priorityLabels = listOf(
        stringResource(Res.string.priority_none),
        stringResource(Res.string.priority_low),
        stringResource(Res.string.priority_medium),
        stringResource(Res.string.priority_high)
    )

    val dateFormatter = remember {
        java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = if (task != null) stringResource(Res.string.edit_task)
                else stringResource(Res.string.add_task),
                style = typography.titleLarge
            )

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(Res.string.task_title)) },
                singleLine = true,
                shape = shapes.large,
                modifier = Modifier.fillMaxWidth()
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(Res.string.task_description)) },
                maxLines = 3,
                shape = shapes.large,
                modifier = Modifier.fillMaxWidth()
            )

            // Due date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Switch(
                    checked = hasDueDate,
                    onCheckedChange = { hasDueDate = it }
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.due_date))
                if (hasDueDate) {
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { showDatePicker = true }) {
                        Text(dateFormatter.format(java.util.Date(dueDate)))
                    }
                }
            }

            // Due time
            if (hasDueDate) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Switch(
                        checked = hasDueTime,
                        onCheckedChange = { hasDueTime = it }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.due_time))
                    if (hasDueTime) {
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { showTimePicker = true }) {
                            Text(
                                String.format(
                                    java.util.Locale.getDefault(),
                                    "%02d:%02d",
                                    dueTime / 60,
                                    dueTime % 60
                                )
                            )
                        }
                    }
                }
            }

            // Reminder
            if (hasDueDate && hasDueTime) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.reminder))
                }
            }

            // Repeat rule
            Text(
                text = stringResource(Res.string.repeat),
                style = typography.labelLarge,
                color = colorScheme.primary
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                repeatOptions.forEachIndexed { index, rule ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index, repeatOptions.size),
                        selected = repeatRule == rule,
                        onClick = { repeatRule = rule },
                        label = { Text(repeatLabels[index], style = typography.labelSmall) }
                    )
                }
            }

            // Priority
            Text(
                text = stringResource(Res.string.priority),
                style = typography.labelLarge,
                color = colorScheme.primary
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                priorityOptions.forEachIndexed { index, p ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index, priorityOptions.size),
                        selected = priority == p,
                        onClick = { priority = p },
                        label = { Text(priorityLabels[index], style = typography.labelSmall) }
                    )
                }
            }

            // Category
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text(stringResource(Res.string.category)) },
                singleLine = true,
                shape = shapes.large,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Save button
            Button(
                onClick = {
                    val now = System.currentTimeMillis()
                    onSave(
                        Task(
                            id = task?.id ?: 0,
                            title = title,
                            description = description,
                            isCompleted = task?.isCompleted ?: false,
                            dueDate = if (hasDueDate) dueDate else null,
                            dueTime = if (hasDueTime) dueTime else null,
                            reminderEnabled = reminderEnabled,
                            repeatRule = repeatRule,
                            priority = priority,
                            category = category,
                            createdAt = task?.createdAt ?: now,
                            completedAt = task?.completedAt,
                            sortOrder = task?.sortOrder ?: 0
                        )
                    )
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.ok))
            }
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { dueDate = it }
                        showDatePicker = false
                    }
                ) { Text(stringResource(Res.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time picker dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = dueTime / 60,
            initialMinute = dueTime % 60,
            is24Hour = true
        )
        DatePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dueTime = timePickerState.hour * 60 + timePickerState.minute
                        showTimePicker = false
                    }
                ) { Text(stringResource(Res.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        ) {
            TimeInput(state = timePickerState)
        }
    }
}
