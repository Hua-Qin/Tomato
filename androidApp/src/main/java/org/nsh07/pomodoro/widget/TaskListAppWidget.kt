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

package org.nsh07.pomodoro.widget

import android.content.Context
import android.os.Build
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.GlanceTheme.colors
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.nsh07.pomodoro.MainActivity
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.data.Task
import org.nsh07.pomodoro.data.TaskRepository
import org.nsh07.pomodoro.ui.theme.lightScheme
import org.nsh07.pomodoro.widget.TomatoWidgetSize.Width4
import java.time.LocalDate
import java.time.ZoneId

class TaskListAppWidget : GlanceAppWidget(), KoinComponent {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val taskRepository: TaskRepository = get()
        val today = LocalDate.now()
        val todayStartMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val tasks = taskRepository.getPendingTasksByDate(todayStartMillis).first().take(5)

        provideContent {
            key(LocalSize.current) {
                GlanceTheme {
                    Content(tasks)
                }
            }
        }
    }

    @Composable
    private fun Content(tasks: List<Task>) {
        val context = LocalContext.current
        val size = LocalSize.current
        val scope = rememberCoroutineScope()
        val roundedCornersSupported = Build.VERSION.SDK_INT >= 31
        Column(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .then(
                        if (roundedCornersSupported) GlanceModifier.background(colors.widgetBackground)
                        else GlanceModifier.background(
                            ImageProvider(R.drawable.rounded_24dp),
                            colorFilter = ColorFilter.tint(colors.widgetBackground)
                        )
                    )
                    .clickable(actionStartActivity<MainActivity>())
        ) {
            TitleBar(
                startIcon = ImageProvider(R.drawable.tomato_logo_notification),
                title = context.getString(R.string.pending_tasks),
                actions = {
                    Box(GlanceModifier.padding(horizontal = 16.dp)) {
                        Image(
                            provider = ImageProvider(R.drawable.refresh),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colors.onSurface),
                            modifier = GlanceModifier
                                .cornerRadius(24.dp)
                                .clickable {
                                    scope.launch { this@TaskListAppWidget.updateAll(context) }
                                }
                        )
                    }
                },
            )

            Column(
                GlanceModifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                if (tasks.isEmpty()) {
                    Text(
                        context.getString(R.string.no_pending_tasks),
                        style = TextStyle(
                            color = colors.onSurfaceVariant,
                            fontSize = typography.bodyMedium.fontSize
                        )
                    )
                } else {
                    tasks.forEach { task ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = GlanceModifier.padding(bottom = 8.dp)
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.checkbox_outline),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colors.onSurfaceVariant),
                                modifier = GlanceModifier.size(20.dp)
                            )
                            Spacer(GlanceModifier.width(8.dp))
                            Text(
                                task.title,
                                style = TextStyle(
                                    color = colors.onSurface,
                                    fontSize = typography.bodyMedium.fontSize,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = GlanceModifier.defaultWeight()
                            )
                            if (task.dueTime != null) {
                                Spacer(GlanceModifier.width(8.dp))
                                Text(
                                    formatDueTime(task.dueTime!!),
                                    style = TextStyle(
                                        color = colors.onSurfaceVariant,
                                        fontSize = typography.bodySmall.fontSize
                                    )
                                )
                            }
                        }
                        if (task != tasks.last()) {
                            Spacer(GlanceModifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }

    private fun formatDueTime(dueTime: Int): String {
        val hours = dueTime / 3600
        val minutes = (dueTime % 3600) / 60
        return String.format("%02d:%02d", hours, minutes)
    }

    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 400, heightDp = 216)
    @Composable
    private fun ContentPreview() {
        val tasks = listOf(
            Task(
                id = 1,
                title = "Review pull request",
                dueTime = 36000, // 10:00
                createdAt = System.currentTimeMillis()
            ),
            Task(
                id = 2,
                title = "Team standup",
                dueTime = 32400, // 09:00
                createdAt = System.currentTimeMillis()
            ),
            Task(
                id = 3,
                title = "Write documentation",
                dueTime = 54000, // 15:00
                createdAt = System.currentTimeMillis()
            )
        )
        GlanceTheme(colors = ColorProviders(lightScheme)) {
            Box(GlanceModifier.background(Color.White)) {
                Box(
                    GlanceModifier.cornerRadius(32.dp)
                ) {
                    Content(tasks)
                }
            }
        }
    }
}
