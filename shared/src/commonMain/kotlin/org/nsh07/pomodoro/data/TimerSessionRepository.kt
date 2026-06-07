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
import java.time.LocalDate

interface TimerSessionRepository {
    suspend fun insertSession(session: TimerSession): Long
    fun getSessionsByDate(date: LocalDate): Flow<List<TimerSession>>
    fun getSessionCountByDate(date: LocalDate): Flow<Int>
    fun getSessionsBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<TimerSession>>
    fun getDurationByTimerName(startDate: LocalDate, endDate: LocalDate): Flow<List<TimerDurationStat>>
    fun getAllSessions(): Flow<List<TimerSession>>
    fun getDailyTaskStats(date: LocalDate): Flow<List<DailyTaskStat>>
    fun getDatesWithRecords(startDate: LocalDate, endDate: LocalDate): Flow<List<LocalDate>>
}

class AppTimerSessionRepository(
    private val timerSessionDao: TimerSessionDao,
    private val ioDispatcher: CoroutineDispatcher,
    private val widgetRefreshNotifier: WidgetRefreshNotifier? = null
) : TimerSessionRepository {
    override suspend fun insertSession(session: TimerSession): Long = withContext(ioDispatcher) {
        val result = timerSessionDao.insertSession(session)
        widgetRefreshNotifier?.notifyTimerDataChanged()
        result
    }

    override fun getSessionsByDate(date: LocalDate): Flow<List<TimerSession>> =
        timerSessionDao.getSessionsByDate(date)

    override fun getSessionCountByDate(date: LocalDate): Flow<Int> =
        timerSessionDao.getSessionCountByDate(date)

    override fun getSessionsBetweenDates(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<TimerSession>> =
        timerSessionDao.getSessionsBetweenDates(startDate, endDate)

    override fun getDurationByTimerName(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<TimerDurationStat>> =
        timerSessionDao.getDurationByTimerName(startDate, endDate)

    override fun getAllSessions(): Flow<List<TimerSession>> = timerSessionDao.getAllSessions()

    override fun getDailyTaskStats(date: LocalDate) = timerSessionDao.getDailyTaskStats(date)

    override fun getDatesWithRecords(startDate: LocalDate, endDate: LocalDate) = timerSessionDao.getDatesWithRecords(startDate, endDate)
}
