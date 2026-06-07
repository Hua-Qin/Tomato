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

package org.nsh07.pomodoro.ui.settingsScreen.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.nsh07.pomodoro.data.CustomTimer
import org.nsh07.pomodoro.data.CustomTimerRepository
import org.nsh07.pomodoro.ui.mergePaddingValues
import org.nsh07.pomodoro.ui.theme.CustomColors.detailPaneTopBarColors
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.CustomColors.topBarColors
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.PANE_MAX_WIDTH
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.add
import tomato.shared.generated.resources.add_timer_plan
import tomato.shared.generated.resources.alarm_enabled
import tomato.shared.generated.resources.arrow_back
import tomato.shared.generated.resources.auto_start_next
import tomato.shared.generated.resources.back
import tomato.shared.generated.resources.cancel
import tomato.shared.generated.resources.delete
import tomato.shared.generated.resources.edit
import tomato.shared.generated.resources.edit_timer_plan
import tomato.shared.generated.resources.focus_duration
import tomato.shared.generated.resources.long_break_duration
import tomato.shared.generated.resources.no_custom_timers
import tomato.shared.generated.resources.session_length
import tomato.shared.generated.resources.short_break_duration
import tomato.shared.generated.resources.timer_filled
import tomato.shared.generated.resources.timer_manager
import tomato.shared.generated.resources.timer_name
import tomato.shared.generated.resources.vibrate_enabled

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimerManagerScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    timerRepository: CustomTimerRepository = koinInject()
) {
    val scope = rememberCoroutineScope()
    val timers by timerRepository.getAllCustomTimers().collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTimer by remember { mutableStateOf<CustomTimer?>(null) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val widthExpanded = currentWindowAdaptiveInfo()
        .windowSizeClass
        .isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)

    val barColors = if (widthExpanded) detailPaneTopBarColors else topBarColors

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(barColors.containerColor)
            .padding(bottom = contentPadding.calculateBottomPadding())
    ) {
        Scaffold(
            topBar = {
                LargeFlexibleTopAppBar(
                    title = {
                        Text(
                            stringResource(Res.string.timer_manager),
                            fontFamily = org.nsh07.pomodoro.ui.theme.LocalAppFonts.current.topBarTitle
                        )
                    },
                    navigationIcon = {
                        if (!widthExpanded)
                            FilledTonalIconButton(
                                onClick = onBack,
                                shapes = IconButtonDefaults.shapes(),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = listItemColors.containerColor
                                )
                            ) {
                                Icon(
                                    painterResource(Res.drawable.arrow_back),
                                    stringResource(Res.string.back)
                                )
                            }
                    },
                    colors = barColors,
                    scrollBehavior = scrollBehavior
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                ) {
                    Icon(
                        painterResource(Res.drawable.add),
                        contentDescription = stringResource(Res.string.add_timer_plan)
                    )
                }
            },
            containerColor = barColors.containerColor,
            modifier = modifier
                .widthIn(max = PANE_MAX_WIDTH)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { innerPadding ->
            // Bottom padding already applied to outer Box, so exclude it from contentPadding
            val contentPaddingWithoutBottom = PaddingValues(
                start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                top = contentPadding.calculateTopPadding(),
                end = contentPadding.calculateEndPadding(LocalLayoutDirection.current),
                bottom = 0.dp
            )
            val insets = mergePaddingValues(innerPadding, contentPaddingWithoutBottom)
            if (timers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(insets),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(Res.string.no_custom_timers),
                        style = typography.bodyLarge,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = insets,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    item { Spacer(Modifier.height(8.dp)) }
                    items(timers, key = { it.id }) { timer ->
                        TimerCard(
                            timer = timer,
                            onEdit = { editingTimer = timer },
                            onDelete = {
                                scope.launch {
                                    timerRepository.deleteCustomTimerById(timer.id)
                                }
                            }
                        )
                    }
                    item { Spacer(Modifier.height(88.dp)) }
                }
            }
        }
    }

    if (showAddDialog) {
        TimerEditDialog(
            timer = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { newTimer ->
                scope.launch {
                    timerRepository.insertCustomTimer(newTimer)
                }
                showAddDialog = false
            }
        )
    }

    editingTimer?.let { timer ->
        TimerEditDialog(
            timer = timer,
            onDismiss = { editingTimer = null },
            onConfirm = { updatedTimer ->
                scope.launch {
                    timerRepository.updateCustomTimer(updatedTimer)
                }
                editingTimer = null
            }
        )
    }
}

@Composable
private fun TimerCard(
    timer: CustomTimer,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(timer.name)
        },
        supportingContent = {
            Text(
                "${timer.focusDuration}m / ${timer.shortBreakDuration}m / ${timer.longBreakDuration}m · ${timer.sessionLength} rounds"
            )
        },
        leadingContent = {
            Icon(
                painterResource(Res.drawable.timer_filled),
                contentDescription = null,
                tint = colorScheme.primary
            )
        },
        trailingContent = {
            Row {
                FilledTonalIconButton(onClick = onEdit) {
                    Icon(
                        painterResource(Res.drawable.edit),
                        contentDescription = stringResource(Res.string.edit_timer_plan)
                    )
                }
                FilledTonalIconButton(onClick = onDelete) {
                    Icon(
                        painterResource(Res.drawable.delete),
                        contentDescription = stringResource(Res.string.delete)
                    )
                }
            }
        },
        colors = listItemColors
    )
}

@Composable
private fun TimerEditDialog(
    timer: CustomTimer?,
    onDismiss: () -> Unit,
    onConfirm: (CustomTimer) -> Unit
) {
    var name by remember { mutableStateOf(timer?.name ?: "") }
    var focusDuration by remember { mutableStateOf(timer?.focusDuration?.toInt() ?: 25) }
    var shortBreakDuration by remember { mutableStateOf(timer?.shortBreakDuration?.toInt() ?: 5) }
    var longBreakDuration by remember { mutableStateOf(timer?.longBreakDuration?.toInt() ?: 15) }
    var sessionLength by remember { mutableStateOf(timer?.sessionLength ?: 4) }
    var alarmEnabled by remember { mutableStateOf(timer?.alarmEnabled ?: true) }
    var vibrateEnabled by remember { mutableStateOf(timer?.vibrateEnabled ?: true) }
    var autoStartNext by remember { mutableStateOf(timer?.autoStartNext ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (timer == null) stringResource(Res.string.add_timer_plan)
                else stringResource(Res.string.edit_timer_plan)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(Res.string.timer_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = focusDuration.toString(),
                    onValueChange = { focusDuration = it.toIntOrNull() ?: focusDuration },
                    label = { Text(stringResource(Res.string.focus_duration)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = shortBreakDuration.toString(),
                    onValueChange = { shortBreakDuration = it.toIntOrNull() ?: shortBreakDuration },
                    label = { Text(stringResource(Res.string.short_break_duration)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = longBreakDuration.toString(),
                    onValueChange = { longBreakDuration = it.toIntOrNull() ?: longBreakDuration },
                    label = { Text(stringResource(Res.string.long_break_duration)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "${stringResource(Res.string.session_length)}: $sessionLength",
                    style = typography.bodyMedium
                )
                Slider(
                    value = sessionLength.toFloat(),
                    onValueChange = { sessionLength = it.toInt() },
                    valueRange = 1f..10f,
                    steps = 8,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(Res.string.alarm_enabled))
                    Switch(checked = alarmEnabled, onCheckedChange = { alarmEnabled = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(Res.string.vibrate_enabled))
                    Switch(checked = vibrateEnabled, onCheckedChange = { vibrateEnabled = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(Res.string.auto_start_next))
                    Switch(checked = autoStartNext, onCheckedChange = { autoStartNext = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val result = CustomTimer(
                        id = timer?.id ?: 0,
                        name = name.ifBlank { "Focus" },
                        focusDuration = focusDuration.toLong(),
                        shortBreakDuration = shortBreakDuration.toLong(),
                        longBreakDuration = longBreakDuration.toLong(),
                        sessionLength = sessionLength,
                        alarmEnabled = alarmEnabled,
                        vibrateEnabled = vibrateEnabled,
                        autoStartNext = autoStartNext,
                        sortOrder = timer?.sortOrder ?: 0
                    )
                    onConfirm(result)
                }
            ) {
                Text(
                    if (timer == null) stringResource(Res.string.add_timer_plan)
                    else stringResource(Res.string.edit_timer_plan)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}
