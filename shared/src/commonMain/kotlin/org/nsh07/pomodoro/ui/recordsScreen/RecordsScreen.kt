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

package org.nsh07.pomodoro.ui.recordsScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.nsh07.pomodoro.data.CounterRecord
import org.nsh07.pomodoro.data.DailyTaskStat
import org.nsh07.pomodoro.data.TimerSession
import org.nsh07.pomodoro.ui.recordsScreen.viewModel.RecordsAction
import org.nsh07.pomodoro.ui.recordsScreen.viewModel.RecordsState
import org.nsh07.pomodoro.ui.recordsScreen.viewModel.StatsPeriod
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsViewModel
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerMode
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerState
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.arrow_back
import tomato.shared.generated.resources.arrow_forward_big
import tomato.shared.generated.resources.autoplay
import tomato.shared.generated.resources.add
import tomato.shared.generated.resources.add_counter
import tomato.shared.generated.resources.cancel
import tomato.shared.generated.resources.check
import tomato.shared.generated.resources.completed
import tomato.shared.generated.resources.counter_record
import tomato.shared.generated.resources.duration_record
import tomato.shared.generated.resources.enter_infinite_mode
import tomato.shared.generated.resources.exit_infinite_mode
import tomato.shared.generated.resources.focus
import tomato.shared.generated.resources.focus_breakdown
import tomato.shared.generated.resources.focus_breakdown_desc
import tomato.shared.generated.resources.infinite_focus
import tomato.shared.generated.resources.long_break
import tomato.shared.generated.resources.no_counters
import tomato.shared.generated.resources.no_records
import tomato.shared.generated.resources.ok
import tomato.shared.generated.resources.pause
import tomato.shared.generated.resources.pause_large
import tomato.shared.generated.resources.play
import tomato.shared.generated.resources.play_large
import tomato.shared.generated.resources.restart
import tomato.shared.generated.resources.restart_large
import tomato.shared.generated.resources.short_break
import tomato.shared.generated.resources.end_session
import tomato.shared.generated.resources.stop
import tomato.shared.generated.resources.statistics
import tomato.shared.generated.resources.best_record
import tomato.shared.generated.resources.today_tab
import tomato.shared.generated.resources.focus_duration_chart
import tomato.shared.generated.resources.session_count_chart
import tomato.shared.generated.resources.today_focus_by_plan
import tomato.shared.generated.resources.edit_name
import tomato.shared.generated.resources.delete_timer
import tomato.shared.generated.resources.edit_timer_name
import tomato.shared.generated.resources.focused_for
import tomato.shared.generated.resources.add_item
import tomato.shared.generated.resources.completed_tasks_count
import tomato.shared.generated.resources.counter_total_change
import tomato.shared.generated.resources.this_month
import tomato.shared.generated.resources.this_week
import tomato.shared.generated.resources.today
import tomato.shared.generated.resources.today_count
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberFadingEdges
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RecordsScreen(
    contentPadding: PaddingValues,
    onAction: (RecordsAction) -> Unit,
    state: RecordsState,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val settingsViewModel: SettingsViewModel = koinViewModel()
    val settingsState by settingsViewModel.settingsState.collectAsStateWithLifecycle()
    val buttonScale = settingsState.buttonSizeScale

    val tabTitles = listOf(
        stringResource(Res.string.duration_record),
        stringResource(Res.string.counter_record),
        stringResource(Res.string.statistics)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(index) }
                        onAction(RecordsAction.SelectTab(index))
                    },
                    text = { Text(title) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> DurationTab(
                    state = state,
                    onAction = onAction,
                    buttonScale = buttonScale
                )
                1 -> CounterTab(
                    state = state,
                    onAction = onAction,
                    contentPadding = contentPadding,
                    buttonScale = buttonScale
                )
                2 -> StatisticsTab(
                    state = state,
                    onAction = onAction
                )
                else -> {}
            }
        }
    }

    // Add custom timer sheet
    if (state.showAddTimerSheet) {
        AddCustomTimerSheet(
            onDismiss = { onAction(RecordsAction.HideAddTimerSheet) },
            onAction = onAction
        )
    }

    // Add counter sheet
    if (state.showAddCounterSheet) {
        AddCounterSheet(
            onDismiss = { onAction(RecordsAction.HideAddCounterSheet) },
            onAction = onAction
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DurationTab(
    state: RecordsState,
    onAction: (RecordsAction) -> Unit,
    buttonScale: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Timer type chips
        item(contentType = "timer_chips") {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                // Default timer chip
                item(contentType = "timer_chip") {
                    val defaultSelected = state.activeTimerId == null
                    FilterChip(
                        selected = defaultSelected,
                        onClick = { /* Select default timer */ },
                        label = { Text(state.timerState.activeTimerName) },
                        leadingIcon = if (defaultSelected) {
                            {
                                Icon(
                                    painterResource(Res.drawable.check),
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else null
                    )
                }

                items(state.customTimers, key = { it.id }, contentType = { "timer_chip" }) { timer ->
                    val selected = state.activeTimerId == timer.id
                    var showMenu by remember { mutableStateOf(false) }
                    var showEditDialog by remember { mutableStateOf(false) }
                    var editName by remember { mutableStateOf(timer.name) }
                    val haptic = LocalHapticFeedback.current

                    Box {
                        FilterChip(
                            selected = selected,
                            onClick = { onAction(RecordsAction.SelectTimer(timer.id)) },
                            label = { Text(timer.name) },
                            leadingIcon = if (selected) {
                                {
                                    Icon(
                                        painterResource(Res.drawable.check),
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            } else null,
                            modifier = Modifier.combinedClickable(
                                onClick = { onAction(RecordsAction.SelectTimer(timer.id)) },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showMenu = true
                                }
                            )
                        )

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.edit_name)) },
                                onClick = {
                                    showMenu = false
                                    editName = timer.name
                                    showEditDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.delete_timer)) },
                                onClick = {
                                    showMenu = false
                                    onAction(RecordsAction.DeleteCustomTimer(timer.id))
                                }
                            )
                        }
                    }

                    if (showEditDialog) {
                        AlertDialog(
                            onDismissRequest = { showEditDialog = false },
                            title = { Text(stringResource(Res.string.edit_timer_name)) },
                            text = {
                                OutlinedTextField(
                                    value = editName,
                                    onValueChange = { editName = it },
                                    singleLine = true
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    if (editName.isNotBlank()) {
                                        onAction(RecordsAction.EditTimerName(timer.id, editName))
                                    }
                                    showEditDialog = false
                                }) { Text(stringResource(Res.string.ok)) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showEditDialog = false }) { Text(stringResource(Res.string.cancel)) }
                            }
                        )
                    }
                }
            }
        }

        // Timer display
        item(contentType = "timer_display") {
            TimerDisplay(
                timerState = state.timerState,
                onAction = onAction,
                buttonScale = buttonScale
            )
        }

        // Today's sessions
        item(contentType = "session_header") {
            Text(
                stringResource(Res.string.today),
                style = typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
            )
        }

        if (state.todaySessions.isEmpty()) {
            item(contentType = "session") {
                Text(
                    stringResource(Res.string.no_records),
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(state.todaySessions, key = { it.id }, contentType = { "session" }) { session ->
                SessionItem(session)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
private fun TimerDisplay(
    timerState: TimerState,
    onAction: (RecordsAction) -> Unit,
    buttonScale: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val clockFontSize by animateFloatAsState(if (timerState.infiniteFocus) 72f else 57f, label = "clockFontSize")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Circular progress + time display
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .aspectRatio(1f)
        ) {
            val progress = if (timerState.infiniteFocus) {
                0f // 无限模式不显示进度
            } else if (timerState.totalTime > 0) {
                (timerState.elapsed.toFloat() / timerState.totalTime).coerceIn(0f, 1f)
            } else 0f

            val trackColor by animateColorAsState(
                when (timerState.timerMode) {
                    TimerMode.FOCUS -> colorScheme.primaryContainer
                    TimerMode.SHORT_BREAK -> colorScheme.tertiaryContainer
                    TimerMode.LONG_BREAK -> colorScheme.secondaryContainer
                    TimerMode.BRAND -> colorScheme.surfaceVariant
                },
                label = "trackColor"
            )

            val indicatorColor by animateColorAsState(
                when (timerState.timerMode) {
                    TimerMode.FOCUS -> colorScheme.primary
                    TimerMode.SHORT_BREAK -> colorScheme.tertiary
                    TimerMode.LONG_BREAK -> colorScheme.secondary
                    TimerMode.BRAND -> colorScheme.outline
                },
                label = "indicatorColor"
            )

            androidx.compose.animation.AnimatedVisibility(
                !timerState.infiniteFocus,
                enter = fadeIn() + scaleIn(initialScale = 4f),
                exit = fadeOut() + scaleOut(targetScale = 4f)
            ) {
                CircularProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    color = indicatorColor,
                    trackColor = trackColor,
                    strokeWidth = 8.dp,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(Res.string.focus),
                    style = typography.labelLarge,
                    color = colorScheme.onSurfaceVariant
                )
                Text(
                    text = timerState.timeStr,
                    style = TextStyle(fontSize = clockFontSize.sp, fontWeight = FontWeight.Bold),
                )
                AnimatedContent(timerState.infiniteFocus, label = "modeLabel") { infinite ->
                    Text(
                        text = if (infinite) stringResource(Res.string.infinite_focus)
                        else when (timerState.timerMode) {
                            TimerMode.FOCUS -> stringResource(Res.string.focus)
                            TimerMode.SHORT_BREAK -> stringResource(Res.string.short_break)
                            TimerMode.LONG_BREAK -> stringResource(Res.string.long_break)
                            TimerMode.BRAND -> ""
                        },
                        style = typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                if (timerState.totalFocusCount > 1) {
                    Text(
                        text = "${timerState.currentFocusCount} / ${timerState.totalFocusCount}",
                        style = typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Control buttons
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 无限模式/退出按钮
            FilledTonalIconButton(
                onClick = {
                    if (timerState.infiniteFocus) onAction(RecordsAction.ExitInfiniteMode)
                    else onAction(RecordsAction.StartInfiniteMode)
                },
                shapes = IconButtonDefaults.shapes(),
                modifier = Modifier.size((48 * buttonScale).dp)
            ) {
                Icon(
                    painterResource(
                        if (timerState.infiniteFocus) Res.drawable.restart_large
                        else Res.drawable.autoplay
                    ),
                    contentDescription = stringResource(
                        if (timerState.infiniteFocus) Res.string.exit_infinite_mode
                        else Res.string.enter_infinite_mode
                    )
                )
            }

            Spacer(Modifier.width(16.dp))

            var isInfiniteLongPressed by remember { mutableStateOf(false) }
            val infiniteButtonColor by animateColorAsState(
                if (isInfiniteLongPressed) colorScheme.tertiaryContainer else colorScheme.primary,
                label = "infiniteButtonColor"
            )

            FilledIconToggleButton(
                checked = timerState.timerRunning,
                onCheckedChange = { onAction(RecordsAction.ToggleTimer) },
                shapes = IconButtonDefaults.toggleableShapes(),
                colors = IconButtonDefaults.filledIconToggleButtonColors(
                    containerColor = infiniteButtonColor,
                    checkedContainerColor = colorScheme.primary
                ),
                modifier = Modifier
                    .size((72 * buttonScale).dp)
                    .combinedClickable(
                        onClick = { onAction(RecordsAction.ToggleTimer) },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isInfiniteLongPressed = true
                            onAction(RecordsAction.StartInfiniteMode)
                        }
                    )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painterResource(
                            if (timerState.timerRunning) Res.drawable.pause_large
                            else Res.drawable.play_large
                        ),
                        contentDescription = if (timerState.timerRunning) stringResource(Res.string.pause)
                        else stringResource(Res.string.play),
                        modifier = Modifier.size(36.dp)
                    )
                    if (isInfiniteLongPressed) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(72.dp),
                            color = colorScheme.tertiary,
                            strokeWidth = 3.dp
                        )
                        LaunchedEffect(isInfiniteLongPressed) {
                            delay(1500)
                            isInfiniteLongPressed = false
                        }
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            FilledTonalIconButton(
                onClick = { onAction(RecordsAction.EndSession) },
                shapes = IconButtonDefaults.shapes(),
                modifier = Modifier.size((48 * buttonScale).dp)
            ) {
                Icon(
                    painterResource(Res.drawable.stop),
                    contentDescription = stringResource(Res.string.end_session)
                )
            }
        }
    }
}

@Composable
private fun SessionItem(
    session: TimerSession,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = shapes.large,
        color = colorScheme.surfaceBright,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column {
                Text(
                    text = session.timerName,
                    style = typography.titleSmall
                )
                Text(
                    text = formatSessionDuration(session.actualDuration),
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = formatSessionTime(session.startedAt),
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CounterTab(
    state: RecordsState,
    onAction: (RecordsAction) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    buttonScale: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (state.counters.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Text(
                    stringResource(Res.string.no_counters),
                    style = typography.bodyLarge,
                    color = colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                FilledTonalButton(onClick = { onAction(RecordsAction.ShowAddCounterSheet) }) {
                    Text(stringResource(Res.string.add_counter))
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp)
            ) {
                items(state.counters, key = { it.id }, contentType = { "counter" }) { counter ->
                    CounterCard(
                        counter = counter,
                        count = state.counterCounts[counter.id] ?: 0,
                        onIncrement = { onAction(RecordsAction.IncrementCounter(counter.id)) },
                        onDecrement = { onAction(RecordsAction.DecrementCounter(counter.id)) },
                        onDelete = { onAction(RecordsAction.DeleteCounter(counter.id)) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CounterCard(
    counter: CounterRecord,
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = shapes.large,
        color = colorScheme.surfaceBright,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = counter.title,
                style = typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            AnimatedContent(
                targetState = count,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInVertically { -it } togetherWith slideOutVertically { it }
                    } else {
                        slideInVertically { it } togetherWith slideOutVertically { -it }
                    }
                },
                label = "counterAnimation"
            ) { targetCount ->
                Text(
                    text = "$targetCount",
                    style = typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = onDecrement,
                    shapes = IconButtonDefaults.shapes(),
                    modifier = Modifier.size(40.dp)
                ) {
                    Text("-", style = typography.titleLarge)
                }

                FilledTonalIconButton(
                    onClick = onIncrement,
                    shapes = IconButtonDefaults.shapes(),
                    modifier = Modifier.size(40.dp)
                ) {
                    Text("+", style = typography.titleLarge)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StatisticsTab(
    state: RecordsState,
    onAction: (RecordsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val countModelProducer = remember { CartesianChartModelProducer() }

    // 根据周期计算图表数据
    val chartData = remember(state.periodSessions, state.statsPeriod, state.infiniteFocusElapsed) {
        when (state.statsPeriod) {
            StatsPeriod.DAY -> {
                // 按小时分组 (Q1: 0-6, Q2: 6-12, Q3: 12-18, Q4: 18-24)
                val stat = state.todayStat
                val labels = listOf("0-6h", "6-12h", "12-18h", "18-24h")
                val durations = if (stat != null) listOf(
                    stat.focusTimeQ1, stat.focusTimeQ2, stat.focusTimeQ3, stat.focusTimeQ4
                ) else listOf(0L, 0L, 0L, 0L)
                // 累加无限模式实时时长到当前季度
                val adjustedDurations = if (state.infiniteFocusElapsed > 0) {
                    val currentQuarter = java.time.LocalTime.now().hour / 6
                    durations.mapIndexed { index, d ->
                        if (index == currentQuarter) d + state.infiniteFocusElapsed else d
                    }
                } else durations
                val counts = MutableList(4) { 0 }
                state.periodSessions.groupBy {
                    Instant.ofEpochMilli(it.startedAt).atZone(ZoneId.systemDefault()).hour / 6
                }.forEach { (quarter, sessions) ->
                    if (quarter in 0..3) counts[quarter] = sessions.size
                }
                ChartData(labels, adjustedDurations, counts)
            }
            StatsPeriod.WEEK -> {
                val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                val durations = MutableList(7) { 0L }
                val counts = MutableList(7) { 0 }
                state.periodSessions.forEach { session ->
                    val dayOfWeek = Instant.ofEpochMilli(session.startedAt)
                        .atZone(ZoneId.systemDefault()).dayOfWeek.value - 1
                    durations[dayOfWeek] += session.actualDuration
                    counts[dayOfWeek]++
                }
                ChartData(labels, durations, counts)
            }
            StatsPeriod.MONTH -> {
                val today = java.time.LocalDate.now()
                val daysInMonth = today.lengthOfMonth()
                val labels = (1..daysInMonth).map { "${it}" }
                val durations = MutableList(daysInMonth) { 0L }
                val counts = MutableList(daysInMonth) { 0 }
                state.periodSessions.forEach { session ->
                    val day = Instant.ofEpochMilli(session.startedAt)
                        .atZone(ZoneId.systemDefault()).dayOfMonth - 1
                    if (day in durations.indices) {
                        durations[day] += session.actualDuration
                        counts[day]++
                    }
                }
                ChartData(labels, durations, counts)
            }
        }
    }

    // 更新图表数据
    LaunchedEffect(chartData) {
        modelProducer.runTransaction {
            columnSeries { series(chartData.durations.map { it / 60000.0 }) }
        }
        countModelProducer.runTransaction {
            columnSeries { series(chartData.counts.map { it.toDouble() }) }
        }
    }

    // 最高记录
    val bestRecord = remember(state.periodSessions) {
        state.periodSessions.maxByOrNull { it.actualDuration }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // 周期切换 (本日/本周/本月)
        item(contentType = "period_selector") {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                StatsPeriod.entries.forEachIndexed { index, period ->
                    val label = when (period) {
                        StatsPeriod.DAY -> stringResource(Res.string.today_tab)
                        StatsPeriod.WEEK -> stringResource(Res.string.this_week)
                        StatsPeriod.MONTH -> stringResource(Res.string.this_month)
                    }
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index, StatsPeriod.entries.size),
                        selected = state.statsPeriod == period,
                        onClick = { onAction(RecordsAction.SetStatsPeriod(period)) },
                        label = { Text(label, style = typography.labelSmall) }
                    )
                }
            }
        }

        // 日历卡片
        item(contentType = "calendar") {
            CalendarCard(
                selectedDate = state.selectedCalendarDate,
                calendarMonth = state.calendarMonth,
                datesWithRecords = state.calendarDatesWithRecords,
                dailyTaskStats = state.dailyTaskStats,
                onSelectDate = { onAction(RecordsAction.SelectCalendarDate(it)) },
                onChangeMonth = { onAction(RecordsAction.ChangeCalendarMonth(it)) }
            )
        }

        // 今日概览卡片
        item(contentType = "summary") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SummaryCard(
                    title = stringResource(Res.string.focus),
                    value = formatSessionDuration(state.todayTotalFocus + state.infiniteFocusElapsed),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = stringResource(Res.string.completed_tasks_count),
                    value = "${state.todayCompletedTaskCount}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 计数器变化概览
        if (state.todayCounterTotalChange > 0) {
            item(contentType = "counter_summary") {
                SummaryCard(
                    title = stringResource(Res.string.counter_total_change),
                    value = "+${state.todayCounterTotalChange}",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 各计划今日时长
        if (state.timerDurationStats.isNotEmpty() || state.infiniteFocusElapsed > 0) {
            item(contentType = "plan_stats") {
                Surface(
                    shape = shapes.large,
                    color = colorScheme.surfaceBright,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(Res.string.today_focus_by_plan),
                            style = typography.titleSmall,
                            color = colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        // 合并无限模式实时时长到当前活跃计划
                        val mergedStats = buildList {
                            val activeTimerName = state.timerState.activeTimerName
                            var found = false
                            state.timerDurationStats.forEach { stat ->
                                if (stat.timerName == activeTimerName && state.infiniteFocusElapsed > 0) {
                                    add(stat.copy(totalDuration = stat.totalDuration + state.infiniteFocusElapsed))
                                    found = true
                                } else {
                                    add(stat)
                                }
                            }
                            if (!found && state.infiniteFocusElapsed > 0) {
                                add(org.nsh07.pomodoro.data.TimerDurationStat(activeTimerName, state.infiniteFocusElapsed))
                            }
                        }
                        mergedStats.forEach { stat ->
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    stat.timerName,
                                    style = typography.bodyMedium,
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    formatSessionDuration(stat.totalDuration),
                                    style = typography.bodyMedium,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 时长统计图表
        item(contentType = "duration_chart") {
            Surface(
                shape = shapes.large,
                color = colorScheme.surfaceBright,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(Res.string.focus_duration_chart),
                        style = typography.titleSmall,
                        color = colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    SimpleColumnChart(
                        modelProducer = modelProducer,
                        labels = chartData.labels
                    )
                }
            }
        }

        // 次数统计图表
        item(contentType = "count_chart") {
            Surface(
                shape = shapes.large,
                color = colorScheme.surfaceBright,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(Res.string.session_count_chart),
                        style = typography.titleSmall,
                        color = colorScheme.tertiary
                    )
                    Spacer(Modifier.height(8.dp))
                    SimpleColumnChart(
                        modelProducer = countModelProducer,
                        labels = chartData.labels,
                        columnColor = colorScheme.tertiary
                    )
                }
            }
        }

        // 最高记录
        if (bestRecord != null) {
            item(contentType = "best_record") {
                Surface(
                    shape = shapes.large,
                    color = colorScheme.surfaceBright,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            stringResource(Res.string.best_record),
                            style = typography.titleSmall,
                            color = colorScheme.primary
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                formatSessionDuration(bestRecord.actualDuration),
                                style = typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                formatSessionTime(bestRecord.startedAt),
                                style = typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarCard(
    selectedDate: LocalDate,
    calendarMonth: LocalDate,
    datesWithRecords: Set<LocalDate>,
    dailyTaskStats: List<DailyTaskStat>,
    onSelectDate: (LocalDate) -> Unit,
    onChangeMonth: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val yearMonth = YearMonth.from(calendarMonth)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = calendarMonth.withDayOfMonth(1).dayOfWeek.value - 1 // Monday=0

    Surface(
        shape = shapes.large,
        color = colorScheme.surfaceBright,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 月份标题 + 切换按钮
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { onChangeMonth(calendarMonth.minusMonths(1)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painterResource(Res.drawable.arrow_back),
                        contentDescription = "Previous month",
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    "${yearMonth.year}年${yearMonth.monthValue}月",
                    style = typography.titleSmall,
                    color = colorScheme.primary
                )
                IconButton(
                    onClick = { onChangeMonth(calendarMonth.plusMonths(1)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painterResource(Res.drawable.arrow_forward_big),
                        contentDescription = "Next month",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // 星期标题行
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("一", "二", "三", "四", "五", "六", "日").forEach { day ->
                    Text(
                        text = day,
                        style = typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // 日期网格
            val rows = ((firstDayOfWeek + daysInMonth + 6) / 7)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                for (row in 0 until rows) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (col in 0..6) {
                            val dayIndex = row * 7 + col - firstDayOfWeek
                            if (dayIndex in 0 until daysInMonth) {
                                val date = calendarMonth.withDayOfMonth(dayIndex + 1)
                                val isSelected = date == selectedDate
                                val isToday = date == today
                                val hasRecord = date in datesWithRecords

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> colorScheme.primary
                                                isToday -> colorScheme.primaryContainer
                                                else -> Color.Transparent
                                            }
                                        )
                                        .clickable { onSelectDate(date) }
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${dayIndex + 1}",
                                            style = typography.bodySmall,
                                            color = when {
                                                isSelected -> colorScheme.onPrimary
                                                isToday -> colorScheme.onPrimaryContainer
                                                else -> colorScheme.onSurface
                                            }
                                        )
                                        if (hasRecord && !isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(colorScheme.primary)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }

            // 选中日期的专注明细
            if (dailyTaskStats.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = colorScheme.outlineVariant)
                Spacer(Modifier.height(8.dp))
                Text(
                    "${selectedDate.monthValue}月${selectedDate.dayOfMonth}日 专注详情",
                    style = typography.titleSmall,
                    color = colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                dailyTaskStats.forEach { stat ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            stat.timerName,
                            style = typography.bodyMedium,
                            color = colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                formatSessionDuration(stat.totalDuration),
                                style = typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${stat.sessionCount}次",
                                style = typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 简化的柱状图组件 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SimpleColumnChart(
    modelProducer: CartesianChartModelProducer,
    labels: List<String>,
    columnColor: androidx.compose.ui.graphics.Color = colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val xValueFormatter = remember(labels) {
        CartesianValueFormatter { _, value, _ ->
            val index = value.toInt()
            if (index in labels.indices) labels[index] else "${index + 1}"
        }
    }

    ProvideVicoTheme(rememberM3VicoTheme()) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    ColumnCartesianLayer.ColumnProvider.series(
                        rememberLineComponent(
                            fill = Fill(columnColor),
                            thickness = 8.dp,
                            shape = CircleShape
                        )
                    )
                ),
                startAxis = VerticalAxis.rememberStart(
                    line = rememberLineComponent(Fill.Transparent, 8.dp),
                    label = rememberTextComponent(typography.bodySmall.copy(colorScheme.onSurface)),
                    tick = null,
                    guideline = null,
                    valueFormatter = CartesianValueFormatter { value, _ ->
                        val minutes = value.toInt()
                        if (minutes >= 60) "${minutes / 60}时${minutes % 60}分"
                        else "${minutes}分"
                    },
                    itemPlacer = VerticalAxis.ItemPlacer.count({ 4 })
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    line = rememberLineComponent(Fill.Transparent, 8.dp),
                    label = rememberTextComponent(typography.bodySmall.copy(colorScheme.onSurface)),
                    tick = null,
                    guideline = null,
                    valueFormatter = xValueFormatter
                ),
                fadingEdges = rememberFadingEdges()
            ),
            modelProducer = modelProducer,
            zoomState = rememberVicoZoomState(zoomEnabled = false),
            scrollState = rememberVicoScrollState(),
            modifier = modifier.height(180.dp)
        )
    }
}

/** 图表数据辅助类 */
private data class ChartData(
    val labels: List<String>,
    val durations: List<Long>,
    val counts: List<Int>
)

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = shapes.large,
        color = colorScheme.surfaceBright,
        tonalElevation = 1.dp,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = value,
                style = typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary
            )
            Text(
                text = title,
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatSessionDuration(duration: Long): String {
    val minutes = duration / (60 * 1000)
    return if (minutes >= 60) "${minutes / 60}小时${minutes % 60}分钟"
    else "${minutes}分钟"
}

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun formatSessionTime(timestamp: Long): String {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .format(timeFormatter)
}
