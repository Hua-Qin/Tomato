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

package org.nsh07.pomodoro.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

sealed class Screen : NavKey {
    // 计划任务（新首页）
    @Serializable
    sealed class Tasks : Screen() {
        @Serializable
        object Main : Tasks()

        @Serializable
        object AddTask : Tasks()

        @Serializable
        data class EditTask(val taskId: Long) : Tasks()
    }

    // 收集
    @Serializable
    sealed class Collection : Screen() {
        @Serializable
        object Main : Collection()

        @Serializable
        object AddNote : Collection()

        @Serializable
        data class EditNote(val noteId: Long) : Collection()
    }

    // 记录
    @Serializable
    sealed class Records : Screen() {
        @Serializable
        object Main : Records()

        @Serializable
        object Timer : Records()

        @Serializable
        object Statistics : Records()
    }

    // 设置（保留）
    @Serializable
    sealed class Settings : Screen() {
        @Serializable
        object Main : Settings()

        @Serializable
        object About : Settings()

        @Serializable
        object Alarm : Settings()

        @Serializable
        object Appearance : Settings()

        @Serializable
        object Backup : Settings()

        @Serializable
        object Timer : Settings()

        @Serializable
        object TimerManager : Settings()
    }

    // 保留兼容
    @Serializable
    object AOD : Screen()
}

data class NavItem(
    val route: Screen,
    val unselectedIcon: DrawableResource,
    val selectedIcon: DrawableResource,
    val label: StringResource,
    val onNavigateHome: () -> Unit
)

data class SettingsNavItem(
    val route: Screen.Settings,
    val icon: DrawableResource,
    val label: StringResource,
    val innerSettings: List<StringResource>
)
