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
interface CustomTimerDao {
    @Insert(onConflict = REPLACE)
    suspend fun insertCustomTimer(timer: CustomTimer): Long

    @Update
    suspend fun updateCustomTimer(timer: CustomTimer)

    @Delete
    suspend fun deleteCustomTimer(timer: CustomTimer)

    @Query("SELECT * FROM custom_timer ORDER BY sortOrder")
    fun getAllCustomTimers(): Flow<List<CustomTimer>>

    @Query("SELECT * FROM custom_timer WHERE id = :id")
    suspend fun getCustomTimerById(id: Long): CustomTimer?

    @Query("DELETE FROM custom_timer WHERE id = :id")
    suspend fun deleteCustomTimerById(id: Long)

    @Query("UPDATE custom_timer SET name = :name WHERE id = :id")
    suspend fun updateTimerName(id: Long, name: String)
}
