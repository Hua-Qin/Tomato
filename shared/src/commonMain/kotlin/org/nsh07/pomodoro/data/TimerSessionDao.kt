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
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TimerSessionDao {
    @Insert(onConflict = REPLACE)
    suspend fun insertSession(session: TimerSession): Long

    @Query("SELECT * FROM timer_session WHERE date = :date ORDER BY startedAt DESC")
    fun getSessionsByDate(date: LocalDate): Flow<List<TimerSession>>

    @Query("SELECT COUNT(*) FROM timer_session WHERE date = :date")
    fun getSessionCountByDate(date: LocalDate): Flow<Int>

    @Query("SELECT COUNT(*) FROM timer_session WHERE date = :date AND timerName = :timerName")
    fun getSessionCountByDateAndType(date: LocalDate, timerName: String): Flow<Int>

    @Query("SELECT * FROM timer_session WHERE date >= :startDate AND date <= :endDate ORDER BY date, startedAt")
    fun getSessionsBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<TimerSession>>

    @Query("SELECT timerName, SUM(actualDuration) as totalDuration FROM timer_session WHERE date >= :startDate AND date <= :endDate GROUP BY timerName")
    fun getDurationByTimerName(startDate: LocalDate, endDate: LocalDate): Flow<List<TimerDurationStat>>

    @Query("SELECT * FROM timer_session ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<TimerSession>>

    @Query("DELETE FROM timer_session WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("SELECT timerName, SUM(actualDuration) as totalDuration, COUNT(*) as sessionCount FROM timer_session WHERE date = :date GROUP BY timerName ORDER BY totalDuration DESC")
    fun getDailyTaskStats(date: LocalDate): Flow<List<DailyTaskStat>>

    @Query("SELECT DISTINCT date FROM timer_session WHERE date >= :startDate AND date <= :endDate")
    fun getDatesWithRecords(startDate: LocalDate, endDate: LocalDate): Flow<List<LocalDate>>
}

data class TimerDurationStat(
    val timerName: String,
    val totalDuration: Long
)

data class DailyTaskStat(
    val timerName: String,
    val totalDuration: Long,
    val sessionCount: Int
)
