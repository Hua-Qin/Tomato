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

package org.nsh07.pomodoro.ui.tasksScreen.viewModel

import org.nsh07.pomodoro.data.Task

sealed interface TasksAction {
    data class AddTask(val task: Task) : TasksAction
    data class UpdateTask(val task: Task) : TasksAction
    data class DeleteTask(val taskId: Long) : TasksAction
    data class CompleteTask(val taskId: Long) : TasksAction
    data class UncompleteTask(val taskId: Long) : TasksAction
    data class SelectDate(val date: Long) : TasksAction
    data class SetFilter(val category: String?) : TasksAction
    data object ShowAddDialog : TasksAction
    data object HideAddDialog : TasksAction
    data class EditTask(val task: Task) : TasksAction
    data object HideEditDialog : TasksAction
}
