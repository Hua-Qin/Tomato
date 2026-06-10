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

interface CounterRecordRepository {
    suspend fun insertCounterRecord(record: CounterRecord): Long
    suspend fun updateCounterRecord(record: CounterRecord)
    suspend fun deleteCounterRecord(record: CounterRecord)
    fun getAllCounters(): Flow<List<CounterRecord>>
    suspend fun incrementCounter(counterId: Long, date: LocalDate)
    suspend fun decrementCounter(counterId: Long, date: LocalDate)
    fun getEntryByCounterAndDate(counterId: Long, date: LocalDate): Flow<CounterEntry?>
    fun getEntriesByDate(date: LocalDate): Flow<List<CounterEntry>>
    fun getTotalCounterChangeByDate(date: LocalDate): Flow<Int>
    fun getEntriesBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<CounterEntry>>
}

class AppCounterRecordRepository(
    private val counterRecordDao: CounterRecordDao,
    private val ioDispatcher: CoroutineDispatcher,
    private val widgetRefreshNotifier: WidgetRefreshNotifier? = null
) : CounterRecordRepository {
    override suspend fun insertCounterRecord(record: CounterRecord): Long =
        withContext(ioDispatcher) {
            counterRecordDao.insertCounterRecord(record)
        }

    override suspend fun updateCounterRecord(record: CounterRecord) = withContext(ioDispatcher) {
        counterRecordDao.updateCounterRecord(record)
    }

    override suspend fun deleteCounterRecord(record: CounterRecord) = withContext(ioDispatcher) {
        counterRecordDao.deleteCounterRecord(record)
    }

    override fun getAllCounters(): Flow<List<CounterRecord>> = counterRecordDao.getAllCounters()

    override suspend fun incrementCounter(counterId: Long, date: LocalDate): Unit =
        withContext(ioDispatcher) {
            val rowsAffected = counterRecordDao.incrementCounterEntryCount(counterId, date)
            if (rowsAffected == 0) {
                counterRecordDao.insertCounterEntry(
                    CounterEntry(counterId = counterId, date = date, count = 1)
                )
            }
            widgetRefreshNotifier?.notifyCounterDataChanged() ?: Unit
        }

    override suspend fun decrementCounter(counterId: Long, date: LocalDate): Unit =
        withContext(ioDispatcher) {
            counterRecordDao.decrementCounterEntryCount(counterId, date)
            widgetRefreshNotifier?.notifyCounterDataChanged() ?: Unit
        }

    override fun getEntryByCounterAndDate(
        counterId: Long,
        date: LocalDate
    ): Flow<CounterEntry?> = counterRecordDao.getEntryByCounterAndDate(counterId, date)

    override fun getEntriesByDate(date: LocalDate): Flow<List<CounterEntry>> =
        counterRecordDao.getEntriesByDate(date)

    override fun getTotalCounterChangeByDate(date: LocalDate): Flow<Int> =
        counterRecordDao.getTotalCounterChangeByDate(date)

    override fun getEntriesBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<CounterEntry>> =
        counterRecordDao.getEntriesBetweenDates(startDate, endDate)
}
