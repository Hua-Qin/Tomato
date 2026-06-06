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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.data.CounterRecord
import org.nsh07.pomodoro.data.CounterRecordRepository
import org.nsh07.pomodoro.data.CustomTimer
import org.nsh07.pomodoro.data.CustomTimerRepository
import org.nsh07.pomodoro.data.StatRepository
import org.nsh07.pomodoro.data.StateRepository
import org.nsh07.pomodoro.data.TimerSessionRepository
import org.nsh07.pomodoro.service.ServiceHelper
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerAction
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerMode
import org.nsh07.pomodoro.utils.millisecondsToStr
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class RecordsViewModel(
    private val customTimerRepository: CustomTimerRepository,
    private val timerSessionRepository: TimerSessionRepository,
    private val counterRecordRepository: CounterRecordRepository,
    private val stateRepository: StateRepository,
    private val statRepository: StatRepository,
    private val serviceHelper: ServiceHelper
) : ViewModel() {

    private val _state = MutableStateFlow(RecordsState())
    val state = _state.asStateFlow()

    private val todayCounterEntries = counterRecordRepository.getEntriesByDate(LocalDate.now())

    init {
        // 核心数据流：计时器、计数器、今日会话
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                customTimerRepository.getAllCustomTimers(),
                counterRecordRepository.getAllCounters(),
                timerSessionRepository.getSessionsByDate(LocalDate.now()),
                stateRepository.timerState,
                todayCounterEntries
            ) { timers, counters, sessions, timerState, entries ->
                val counterCounts = mutableMapOf<Long, Int>()
                for (counter in counters) {
                    counterCounts[counter.id] = entries.find { it.counterId == counter.id }?.count ?: 0
                }

                _state.update { it.copy(
                    customTimers = timers,
                    counters = counters,
                    counterCounts = counterCounts,
                    todaySessions = sessions,
                    timerState = timerState,
                    activeTimerId = timerState.activeTimerId
                ) }
            }.collect {}
        }

        // 统计数据流：今日各计划时长、今日完成次数、今日Stat、周期内会话
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                timerSessionRepository.getDurationByTimerName(LocalDate.now(), LocalDate.now()),
                timerSessionRepository.getSessionCountByDate(LocalDate.now()),
                statRepository.getTodayStat(),
                _state.flatMapLatest { state ->
                    val (start, end) = getPeriodDates(state.statsPeriod)
                    timerSessionRepository.getSessionsBetweenDates(start, end)
                }
            ) { durationStats, sessionCount, todayStat, periodSessions ->
                _state.update { it.copy(
                    todayTotalFocus = todayStat?.totalFocusTime() ?: 0L,
                    todaySessionCount = sessionCount,
                    timerDurationStats = durationStats,
                    periodSessions = periodSessions,
                    todayStat = todayStat
                ) }
            }.collect {}
        }
    }

    fun onAction(action: RecordsAction) {
        when (action) {
            is RecordsAction.SelectTab -> selectTab(action.index)
            is RecordsAction.SelectTimer -> selectTimer(action.timerId)
            is RecordsAction.AddCustomTimer -> addCustomTimer(action.timer)
            is RecordsAction.DeleteCustomTimer -> deleteCustomTimer(action.timerId)
            is RecordsAction.ShowAddTimerSheet -> showAddTimerSheet()
            is RecordsAction.HideAddTimerSheet -> hideAddTimerSheet()
            is RecordsAction.IncrementCounter -> incrementCounter(action.counterId)
            is RecordsAction.DecrementCounter -> decrementCounter(action.counterId)
            is RecordsAction.AddCounter -> addCounter(action.title)
            is RecordsAction.DeleteCounter -> deleteCounter(action.counterId)
            is RecordsAction.ShowAddCounterSheet -> showAddCounterSheet()
            is RecordsAction.HideAddCounterSheet -> hideAddCounterSheet()
            is RecordsAction.SetStatsPeriod -> setStatsPeriod(action.period)
            is RecordsAction.ToggleTimer -> toggleTimer()
            is RecordsAction.ResetTimer -> resetTimer()
            is RecordsAction.SkipTimer -> skipTimer()
            is RecordsAction.StartInfiniteMode -> startInfiniteMode()
            is RecordsAction.ExitInfiniteMode -> exitInfiniteMode()
        }
    }

    private fun toggleTimer() {
        serviceHelper.startService(TimerAction.ToggleTimer)
    }

    private fun resetTimer() {
        serviceHelper.startService(TimerAction.ResetTimer)
    }

    private fun skipTimer() {
        serviceHelper.startService(TimerAction.SkipTimer(fromButton = true))
    }

    private fun startInfiniteMode() {
        stateRepository.timerState.update {
            it.copy(infiniteFocus = true)
        }
        serviceHelper.startService(TimerAction.ResetTimer)
        // Auto-start the timer after reset
        serviceHelper.startService(TimerAction.ToggleTimer)
    }

    private fun exitInfiniteMode() {
        stateRepository.timerState.update {
            it.copy(infiniteFocus = false)
        }
        serviceHelper.startService(TimerAction.ResetTimer)
    }

    private fun selectTab(index: Int) {
        _state.update { it.copy(selectedTab = index) }
    }

    private fun selectTimer(timerId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val timer = customTimerRepository.getCustomTimerById(timerId) ?: return@launch

            if (!stateRepository.timerState.value.serviceRunning) {
                stateRepository.time.update { timer.focusDuration }
                stateRepository.timerState.update { currentState ->
                    currentState.copy(
                        timerMode = TimerMode.FOCUS,
                        timeStr = millisecondsToStr(timer.focusDuration),
                        totalTime = timer.focusDuration,
                        nextTimerMode = if (timer.sessionLength > 1) TimerMode.SHORT_BREAK else TimerMode.LONG_BREAK,
                        nextTimeStr = millisecondsToStr(
                            if (timer.sessionLength > 1) timer.shortBreakDuration else timer.longBreakDuration
                        ),
                        currentFocusCount = 1,
                        totalFocusCount = timer.sessionLength,
                        activeTimerName = timer.name,
                        activeTimerId = timer.id
                    )
                }
            }

            _state.update { it.copy(activeTimerId = timerId) }
        }
    }

    private fun addCustomTimer(timer: CustomTimer) {
        viewModelScope.launch(Dispatchers.IO) {
            customTimerRepository.insertCustomTimer(timer)
            _state.update { it.copy(showAddTimerSheet = false) }
        }
    }

    private fun deleteCustomTimer(timerId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val timer = customTimerRepository.getCustomTimerById(timerId) ?: return@launch
            customTimerRepository.deleteCustomTimer(timer)

            // If the deleted timer was active, reset to default
            if (_state.value.activeTimerId == timerId) {
                if (!stateRepository.timerState.value.serviceRunning) {
                    stateRepository.timerState.update { currentState ->
                        currentState.copy(
                            activeTimerName = "专注",
                            activeTimerId = null
                        )
                    }
                }
                _state.update { it.copy(activeTimerId = null) }
            }
        }
    }

    private fun showAddTimerSheet() {
        _state.update { it.copy(showAddTimerSheet = true) }
    }

    private fun hideAddTimerSheet() {
        _state.update { it.copy(showAddTimerSheet = false) }
    }

    private fun incrementCounter(counterId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            counterRecordRepository.incrementCounter(counterId, LocalDate.now())
        }
    }

    private fun decrementCounter(counterId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            counterRecordRepository.decrementCounter(counterId, LocalDate.now())
        }
    }

    private fun addCounter(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val sortOrder = _state.value.counters.size
            counterRecordRepository.insertCounterRecord(
                CounterRecord(title = title, createdAt = System.currentTimeMillis(), sortOrder = sortOrder)
            )
            _state.update { it.copy(showAddCounterSheet = false) }
        }
    }

    private fun deleteCounter(counterId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val counter = _state.value.counters.find { it.id == counterId } ?: return@launch
            counterRecordRepository.deleteCounterRecord(counter)
        }
    }

    private fun showAddCounterSheet() {
        _state.update { it.copy(showAddCounterSheet = true) }
    }

    private fun hideAddCounterSheet() {
        _state.update { it.copy(showAddCounterSheet = false) }
    }

    private fun setStatsPeriod(period: StatsPeriod) {
        _state.update { it.copy(statsPeriod = period) }
    }

    /** 根据统计周期计算起止日期 */
    private fun getPeriodDates(period: StatsPeriod): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        return when (period) {
            StatsPeriod.DAY -> today to today
            StatsPeriod.WEEK -> {
                val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val end = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                start to end
            }
            StatsPeriod.MONTH -> {
                val start = today.withDayOfMonth(1)
                val end = today.withDayOfMonth(today.lengthOfMonth())
                start to end
            }
        }
    }
}
