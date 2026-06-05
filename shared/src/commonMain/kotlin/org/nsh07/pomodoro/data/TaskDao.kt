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

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM task WHERE isCompleted = 0 ORDER BY sortOrder, dueDate")
    fun getPendingTasks(): Flow<List<Task>>

    @Query("SELECT * FROM task WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM task WHERE isCompleted = 0 AND dueDate IS NOT NULL AND date(dueDate / 1000, 'unixepoch') = date(:date / 1000, 'unixepoch') ORDER BY sortOrder, dueTime")
    fun getPendingTasksByDate(date: Long): Flow<List<Task>>

    @Query("SELECT * FROM task ORDER BY sortOrder, dueDate")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM task WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?

    @Query("DELETE FROM task WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("SELECT COUNT(*) FROM task WHERE isCompleted = 0")
    fun getPendingTaskCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM task WHERE isCompleted = 0 AND dueDate IS NOT NULL AND date(dueDate / 1000, 'unixepoch') = date(:date / 1000, 'unixepoch')")
    fun getPendingTaskCountByDate(date: Long): Flow<Int>
}
