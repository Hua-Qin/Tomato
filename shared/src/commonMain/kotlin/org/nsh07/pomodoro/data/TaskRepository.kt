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

package org.nsh07.pomodoro.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

interface TaskRepository {
    suspend fun insertTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    fun getPendingTasks(): Flow<List<Task>>
    fun getCompletedTasks(): Flow<List<Task>>
    fun getPendingTasksByDate(date: Long): Flow<List<Task>>
    fun getAllTasks(): Flow<List<Task>>
    suspend fun getTaskById(id: Long): Task?
    suspend fun completeTask(taskId: Long): Task?
    suspend fun uncompleteTask(taskId: Long)
    fun getPendingTaskCount(): Flow<Int>
    fun getPendingTaskCountByDate(date: Long): Flow<Int>
    fun getCompletedTaskCountByDate(timestamp: Long): Flow<Int>
}

class AppTaskRepository(
    private val taskDao: TaskDao,
    private val ioDispatcher: CoroutineDispatcher,
    private val widgetRefreshNotifier: WidgetRefreshNotifier? = null
) : TaskRepository {
    override suspend fun insertTask(task: Task): Long = withContext(ioDispatcher) {
        val result = taskDao.insertTask(task)
        widgetRefreshNotifier?.notifyTaskDataChanged()
        result
    }

    override suspend fun updateTask(task: Task) = withContext(ioDispatcher) {
        taskDao.updateTask(task)
        widgetRefreshNotifier?.notifyTaskDataChanged()
    }

    override suspend fun deleteTask(task: Task) = withContext(ioDispatcher) {
        taskDao.deleteTask(task)
        widgetRefreshNotifier?.notifyTaskDataChanged()
    }

    override fun getPendingTasks(): Flow<List<Task>> = taskDao.getPendingTasks()

    override fun getCompletedTasks(): Flow<List<Task>> = taskDao.getCompletedTasks()

    override fun getPendingTasksByDate(date: Long): Flow<List<Task>> =
        taskDao.getPendingTasksByDate(date)

    override fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    override suspend fun getTaskById(id: Long): Task? = withContext(ioDispatcher) {
        taskDao.getTaskById(id)
    }

    override suspend fun completeTask(taskId: Long): Task? = withContext(ioDispatcher) {
        val task = taskDao.getTaskById(taskId) ?: return@withContext null
        val now = System.currentTimeMillis()
        taskDao.updateTask(task.copy(isCompleted = true, completedAt = now))

        // Generate next instance for repeating tasks
        if (task.repeatRule != "none") {
            val nextDueDate = calculateNextDueDate(task)
            if (nextDueDate != null) {
                val nextTask = task.copy(
                    id = 0,
                    isCompleted = false,
                    completedAt = null,
                    dueDate = nextDueDate,
                    createdAt = now
                )
                taskDao.insertTask(nextTask)
            }
        }
        widgetRefreshNotifier?.notifyTaskDataChanged()
        task
    }

    override suspend fun uncompleteTask(taskId: Long) = withContext(ioDispatcher) {
        val task = taskDao.getTaskById(taskId) ?: return@withContext
        taskDao.updateTask(task.copy(isCompleted = false, completedAt = null))
        widgetRefreshNotifier?.notifyTaskDataChanged()
    }

    override fun getPendingTaskCount(): Flow<Int> = taskDao.getPendingTaskCount()

    override fun getPendingTaskCountByDate(date: Long): Flow<Int> =
        taskDao.getPendingTaskCountByDate(date)

    override fun getCompletedTaskCountByDate(timestamp: Long): Flow<Int> =
        taskDao.getCompletedTaskCountByDate(timestamp)

    private fun calculateNextDueDate(task: Task): Long? {
        val dueDate = task.dueDate ?: return null
        val calendar = Calendar.getInstance().apply { timeInMillis = dueDate }

        when (task.repeatRule) {
            "daily" -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            "weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            "monthly" -> calendar.add(Calendar.MONTH, 1)
            "custom" -> {
                val customDays = task.repeatCustomDays.split(",").mapNotNull { it.trim().toIntOrNull() }
                if (customDays.isEmpty()) return null
                // Find next matching day of week
                val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                val dayMapping = mapOf(
                    1 to Calendar.SUNDAY, 2 to Calendar.MONDAY, 3 to Calendar.TUESDAY,
                    4 to Calendar.WEDNESDAY, 5 to Calendar.THURSDAY, 6 to Calendar.FRIDAY,
                    7 to Calendar.SATURDAY
                )
                val targetDays = customDays.mapNotNull { dayMapping[it] }.sorted()
                val nextDay = targetDays.firstOrNull { it > currentDayOfWeek }
                    ?: targetDays.firstOrNull() ?: return null
                val daysToAdd = if (nextDay > currentDayOfWeek) {
                    nextDay - currentDayOfWeek
                } else {
                    7 - currentDayOfWeek + nextDay
                }
                calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
            }
            else -> return null
        }
        return calendar.timeInMillis
    }
}
