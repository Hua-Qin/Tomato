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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SmallFloatingActionButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.data.CounterRecord
import org.nsh07.pomodoro.data.TimerSession
import org.nsh07.pomodoro.ui.recordsScreen.viewModel.RecordsAction
import org.nsh07.pomodoro.ui.recordsScreen.viewModel.RecordsState
import org.nsh07.pomodoro.ui.recordsScreen.viewModel.StatsPeriod
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerMode
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerState
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.add
import tomato.shared.generated.resources.add_counter
import tomato.shared.generated.resources.check
import tomato.shared.generated.resources.completed
import tomato.shared.generated.resources.counter_record
import tomato.shared.generated.resources.duration_record
import tomato.shared.generated.resources.focus
import tomato.shared.generated.resources.focus_breakdown
import tomato.shared.generated.resources.focus_breakdown_desc
import tomato.shared.generated.resources.long_break
import tomato.shared.generated.resources.no_counters
import tomato.shared.generated.resources.no_records
import tomato.shared.generated.resources.pause
import tomato.shared.generated.resources.pause_large
import tomato.shared.generated.resources.play
import tomato.shared.generated.resources.play_large
import tomato.shared.generated.resources.restart
import tomato.shared.generated.resources.restart_large
import tomato.shared.generated.resources.short_break
import tomato.shared.generated.resources.skip
import tomato.shared.generated.resources.skip_next_large
import tomato.shared.generated.resources.statistics
import tomato.shared.generated.resources.this_month
import tomato.shared.generated.resources.this_week
import tomato.shared.generated.resources.today
import tomato.shared.generated.resources.today_count
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
                    onAction = onAction
                )
                1 -> CounterTab(
                    state = state,
                    onAction = onAction,
                    contentPadding = contentPadding
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
                        } else null
                    )
                }

                item(contentType = "add_timer_chip") {
                    FilterChip(
                        selected = false,
                        onClick = { onAction(RecordsAction.ShowAddTimerSheet) },
                        label = {
                            Icon(
                                painterResource(Res.drawable.add),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }

        // Timer display
        item(contentType = "timer_display") {
            TimerDisplay(
                timerState = state.timerState,
                onAction = onAction
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
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        // Circular progress + time display
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .aspectRatio(1f)
        ) {
            val progress = if (timerState.totalTime > 0) {
                (timerState.totalTime.toFloat() - timerState.totalTime) / timerState.totalTime
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

            CircularProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                color = indicatorColor,
                trackColor = trackColor,
                strokeWidth = 8.dp,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = timerState.activeTimerName,
                    style = typography.labelLarge,
                    color = colorScheme.onSurfaceVariant
                )
                Text(
                    text = timerState.timeStr,
                    style = typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (timerState.timerMode) {
                        TimerMode.FOCUS -> stringResource(Res.string.focus)
                        TimerMode.SHORT_BREAK -> stringResource(Res.string.short_break)
                        TimerMode.LONG_BREAK -> stringResource(Res.string.long_break)
                        TimerMode.BRAND -> ""
                    },
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
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
            FilledTonalIconButton(
                onClick = { onAction(RecordsAction.ResetTimer) },
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    painterResource(Res.drawable.restart_large),
                    contentDescription = stringResource(Res.string.restart)
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
                    .size(72.dp)
                    .combinedClickable(
                        onClick = { onAction(RecordsAction.ToggleTimer) },
                        onLongClick = {
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
                onClick = { onAction(RecordsAction.SkipTimer) },
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    painterResource(Res.drawable.skip_next_large),
                    contentDescription = stringResource(Res.string.skip)
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

        if (state.counters.isNotEmpty()) {
            SmallFloatingActionButton(
                onClick = { onAction(RecordsAction.ShowAddCounterSheet) },
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
                    painterResource(Res.drawable.add),
                    contentDescription = stringResource(Res.string.add_counter),
                    modifier = Modifier.size(24.dp)
                )
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
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Period toggle
        item(contentType = "period_selector") {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                    selected = state.statsPeriod == StatsPeriod.WEEK,
                    onClick = { onAction(RecordsAction.SetStatsPeriod(StatsPeriod.WEEK)) },
                    label = { Text(stringResource(Res.string.this_week)) }
                )
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                    selected = state.statsPeriod == StatsPeriod.MONTH,
                    onClick = { onAction(RecordsAction.SetStatsPeriod(StatsPeriod.MONTH)) },
                    label = { Text(stringResource(Res.string.this_month)) }
                )
            }
        }

        // Duration breakdown placeholder
        item(contentType = "breakdown") {
            Surface(
                shape = shapes.large,
                color = colorScheme.surfaceBright,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        stringResource(Res.string.focus_breakdown),
                        style = typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(Res.string.focus_breakdown_desc),
                        style = typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Summary cards
        item(contentType = "summary") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SummaryCard(
                    title = stringResource(Res.string.focus),
                    value = "0m",
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = stringResource(Res.string.completed),
                    value = "0",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

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
    return if (minutes >= 60) "${minutes / 60}h ${minutes % 60}m"
    else "${minutes}m"
}

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun formatSessionTime(timestamp: Long): String {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .format(timeFormatter)
}
