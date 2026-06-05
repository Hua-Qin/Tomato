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

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.data.CounterRecord
import org.nsh07.pomodoro.data.TimerSession
import org.nsh07.pomodoro.ui.recordsScreen.viewModel.RecordsAction
import org.nsh07.pomodoro.ui.recordsScreen.viewModel.RecordsState
import org.nsh07.pomodoro.ui.recordsScreen.viewModel.StatsPeriod
import org.nsh07.pomodoro.ui.timerScreen.viewmodel.TimerMode
import org.nsh07.pomodoro.ui.timerScreen.viewmodel.TimerState
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.add
import tomato.shared.generated.resources.pause
import tomato.shared.generated.resources.pause_large
import tomato.shared.generated.resources.play
import tomato.shared.generated.resources.play_large
import tomato.shared.generated.resources.restart_large
import tomato.shared.generated.resources.skip_next_large
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
                    onAction = onAction
                )
                2 -> StatisticsTab(
                    state = state,
                    onAction = onAction
                )
            }
        }
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
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Default timer chip
                val defaultSelected = state.activeTimerId == null
                FilledTonalButton(
                    onClick = { /* Select default timer */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        state.timerState.activeTimerName,
                        fontWeight = if (defaultSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
                state.customTimers.forEach { timer ->
                    val selected = state.activeTimerId == timer.id
                    FilledTonalButton(
                        onClick = { onAction(RecordsAction.SelectTimer(timer.id)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            timer.name,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
                FilledTonalButton(
                    onClick = { onAction(RecordsAction.ShowAddTimerSheet) }
                ) {
                    Icon(
                        painterResource(Res.drawable.add),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Timer display
        item {
            TimerDisplay(
                timerState = state.timerState
            )
        }

        // Today's sessions
        item {
            Text(
                stringResource(Res.string.today),
                style = typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
            )
        }

        if (state.todaySessions.isEmpty()) {
            item {
                Text(
                    "暂无记录",
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(state.todaySessions, key = { it.id }) { session ->
                SessionItem(session)
            }
        }
    }
}

@Composable
private fun TimerDisplay(
    timerState: TimerState,
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
                onClick = { /* Reset */ },
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    painterResource(Res.drawable.restart_large),
                    contentDescription = stringResource(Res.string.restart)
                )
            }

            Spacer(Modifier.width(16.dp))

            FilledIconButton(
                onClick = { /* Toggle play/pause */ },
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    painterResource(
                        if (timerState.timerRunning) Res.drawable.pause_large
                        else Res.drawable.play_large
                    ),
                    contentDescription = if (timerState.timerRunning) stringResource(Res.string.pause)
                    else stringResource(Res.string.play),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            FilledTonalIconButton(
                onClick = { /* Skip */ },
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

@Composable
private fun CounterTab(
    state: RecordsState,
    onAction: (RecordsAction) -> Unit,
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
                    "暂无计数器",
                    style = typography.bodyLarge,
                    color = colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                FilledTonalButton(onClick = { onAction(RecordsAction.ShowAddCounterSheet) }) {
                    Text(stringResource(Res.string.add_counter))
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp)
            ) {
                items(state.counters, key = { it.id }) { counter ->
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
            LargeExtendedFloatingActionButton(
                onClick = { onAction(RecordsAction.ShowAddCounterSheet) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    painterResource(Res.drawable.add),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Text(stringResource(Res.string.add_counter))
            }
        }
    }
}

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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = counter.title,
                    style = typography.titleMedium
                )
                Text(
                    text = stringResource(Res.string.today_count, count),
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
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

                Text(
                    text = "$count",
                    style = typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(48.dp)
                )

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
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledTonalButton(
                    onClick = { onAction(RecordsAction.SetStatsPeriod(StatsPeriod.WEEK)) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        stringResource(Res.string.this_week),
                        fontWeight = if (state.statsPeriod == StatsPeriod.WEEK) FontWeight.Bold else FontWeight.Normal
                    )
                }
                FilledTonalButton(
                    onClick = { onAction(RecordsAction.SetStatsPeriod(StatsPeriod.MONTH)) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        stringResource(Res.string.this_month),
                        fontWeight = if (state.statsPeriod == StatsPeriod.MONTH) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Duration breakdown placeholder
        item {
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
        item {
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
