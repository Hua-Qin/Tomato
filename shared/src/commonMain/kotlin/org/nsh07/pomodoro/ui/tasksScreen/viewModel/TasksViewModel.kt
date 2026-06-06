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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.data.Task
import org.nsh07.pomodoro.data.TaskRepository

class TasksViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _tasksState = MutableStateFlow(TasksState())
    val tasksState = _tasksState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                taskRepository.getPendingTasks(),
                taskRepository.getCompletedTasks()
            ) { pending, completed ->
                _tasksState.update { currentState ->
                    currentState.copy(
                        pendingTasks = if (currentState.filterCategory != null)
                            pending.filter { it.category == currentState.filterCategory }
                        else pending,
                        completedTasks = if (currentState.filterCategory != null)
                            completed.filter { it.category == currentState.filterCategory }
                        else completed
                    )
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Unit
            ).collect {}
        }
    }

    fun onAction(action: TasksAction) {
        when (action) {
            is TasksAction.AddTask -> addTask(action.task)
            is TasksAction.UpdateTask -> updateTask(action.task)
            is TasksAction.DeleteTask -> deleteTask(action.taskId)
            is TasksAction.CompleteTask -> completeTask(action.taskId)
            is TasksAction.UncompleteTask -> uncompleteTask(action.taskId)
            is TasksAction.SelectDate -> selectDate(action.date)
            is TasksAction.SetFilter -> setFilter(action.category)
            is TasksAction.ShowAddDialog -> showAddDialog()
            is TasksAction.HideAddDialog -> hideAddDialog()
            is TasksAction.EditTask -> editTask(action.task)
            is TasksAction.HideEditDialog -> hideEditDialog()
        }
    }

    private fun addTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.insertTask(task)
            _tasksState.update { it.copy(showAddDialog = false) }
        }
    }

    private fun updateTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.updateTask(task)
            _tasksState.update { it.copy(editingTask = null) }
        }
    }

    private fun deleteTask(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = taskRepository.getTaskById(taskId)
            if (task != null) taskRepository.deleteTask(task)
        }
    }

    private fun completeTask(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.completeTask(taskId)
        }
    }

    private fun uncompleteTask(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.uncompleteTask(taskId)
        }
    }

    private fun selectDate(date: Long) {
        _tasksState.update { it.copy(selectedDate = date) }
    }

    private fun setFilter(category: String?) {
        _tasksState.update { it.copy(filterCategory = category) }
    }

    private fun showAddDialog() {
        _tasksState.update { it.copy(showAddDialog = true) }
    }

    private fun hideAddDialog() {
        _tasksState.update { it.copy(showAddDialog = false) }
    }

    private fun editTask(task: Task) {
        _tasksState.update { it.copy(editingTask = task) }
    }

    private fun hideEditDialog() {
        _tasksState.update { it.copy(editingTask = null) }
    }
}
