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

package org.nsh07.pomodoro.ui.recordsScreen.viewModel

import androidx.compose.runtime.Immutable
import org.nsh07.pomodoro.data.CounterEntry
import org.nsh07.pomodoro.data.CounterRecord
import org.nsh07.pomodoro.data.CustomTimer
import org.nsh07.pomodoro.data.DailyTaskStat
import org.nsh07.pomodoro.data.Stat
import org.nsh07.pomodoro.data.TimerDurationStat
import org.nsh07.pomodoro.data.TimerSession
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerState
import java.time.LocalDate

@Immutable
data class RecordsState(
    val selectedTab: Int = 0,
    val customTimers: List<CustomTimer> = emptyList(),
    val activeTimerId: Long? = null,
    val timerState: TimerState = TimerState(),
    val todaySessions: List<TimerSession> = emptyList(),
    val counters: List<CounterRecord> = emptyList(),
    val counterCounts: Map<Long, Int> = emptyMap(),
    val showAddTimerSheet: Boolean = false,
    val showAddCounterSheet: Boolean = false,
    val statsPeriod: StatsPeriod = StatsPeriod.WEEK,
    val todayTotalFocus: Long = 0L,
    val todaySessionCount: Int = 0,
    val timerDurationStats: List<TimerDurationStat> = emptyList(),
    val periodSessions: List<TimerSession> = emptyList(),
    val todayStat: Stat? = null,
    val infiniteFocusElapsed: Long = 0L,
    val todayCompletedTaskCount: Int = 0,
    val todayCounterTotalChange: Int = 0,
    val periodCounterEntries: List<CounterEntry> = emptyList(),
    val selectedCalendarDate: LocalDate = LocalDate.now(),
    val dailyTaskStats: List<DailyTaskStat> = emptyList(),
    val calendarDatesWithRecords: Set<LocalDate> = emptySet(),
    val calendarMonth: LocalDate = LocalDate.now().withDayOfMonth(1)
)

enum class StatsPeriod { DAY, WEEK, MONTH }
