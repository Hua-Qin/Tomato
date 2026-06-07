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

package org.nsh07.pomodoro.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.widget.HistoryAppWidget
import org.nsh07.pomodoro.widget.TaskListAppWidget
import org.nsh07.pomodoro.widget.TodayAppWidget
import androidx.glance.appwidget.updateAll

class TaskReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Refresh widgets after device reboot
            val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            scope.launch {
                try {
                    TaskListAppWidget().updateAll(context)
                    TodayAppWidget().updateAll(context)
                    HistoryAppWidget().updateAll(context)
                } catch (_: Exception) {}
            }
            return
        }

        if (intent.action != TaskReminderScheduler.ACTION_SHOW_REMINDER) return

        val taskId = intent.getLongExtra(TaskReminderScheduler.EXTRA_TASK_ID, -1)
        val taskTitle = intent.getStringExtra(TaskReminderScheduler.EXTRA_TASK_TITLE) ?: return
        if (taskId == -1L) return

        val notificationManager = NotificationManagerCompat.from(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_TASK_REMINDERS)
            .setSmallIcon(R.drawable.tomato_logo_notification)
            .setContentTitle(taskTitle)
            .setContentText(taskTitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(
                android.app.PendingIntent.getActivity(
                    context,
                    0,
                    context.packageManager.getLaunchIntentForPackage(context.packageName),
                    android.app.PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

        notificationManager.notify(taskId.toInt(), notification)
    }

    companion object {
        const val CHANNEL_TASK_REMINDERS = "task_reminders"
    }
}
