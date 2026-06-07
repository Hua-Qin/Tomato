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

package org.nsh07.pomodoro.di

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.data.WidgetRefreshNotifier
import org.nsh07.pomodoro.widget.HistoryAppWidget
import org.nsh07.pomodoro.widget.TaskListAppWidget
import org.nsh07.pomodoro.widget.TodayAppWidget

class WidgetRefreshNotifierImpl(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : WidgetRefreshNotifier {
    override fun notifyTaskDataChanged() {
        scope.launch {
            try {
                TaskListAppWidget().updateAll(context)
                TodayAppWidget().updateAll(context)
            } catch (_: Exception) {}
        }
    }

    override fun notifyTimerDataChanged() {
        scope.launch {
            try {
                TodayAppWidget().updateAll(context)
                HistoryAppWidget().updateAll(context)
            } catch (_: Exception) {}
        }
    }
}
