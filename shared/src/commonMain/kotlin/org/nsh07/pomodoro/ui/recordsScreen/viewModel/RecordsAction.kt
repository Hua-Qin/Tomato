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

import org.nsh07.pomodoro.data.CustomTimer

sealed interface RecordsAction {
    data class SelectTab(val index: Int) : RecordsAction
    data class SelectTimer(val timerId: Long) : RecordsAction
    data class AddCustomTimer(val timer: CustomTimer) : RecordsAction
    data class DeleteCustomTimer(val timerId: Long) : RecordsAction
    data object ShowAddTimerSheet : RecordsAction
    data object HideAddTimerSheet : RecordsAction
    data class IncrementCounter(val counterId: Long) : RecordsAction
    data class DecrementCounter(val counterId: Long) : RecordsAction
    data class AddCounter(val title: String) : RecordsAction
    data class DeleteCounter(val counterId: Long) : RecordsAction
    data object ShowAddCounterSheet : RecordsAction
    data object HideAddCounterSheet : RecordsAction
    data class SetStatsPeriod(val period: StatsPeriod) : RecordsAction
    data object ToggleTimer : RecordsAction
    data object ResetTimer : RecordsAction
    data object SkipTimer : RecordsAction
    data object StartInfiniteMode : RecordsAction
    data object ExitInfiniteMode : RecordsAction
    data class EditTimerName(val timerId: Long, val newName: String) : RecordsAction
}
