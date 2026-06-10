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
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface CounterRecordDao {
    @Insert(onConflict = REPLACE)
    suspend fun insertCounterRecord(record: CounterRecord): Long

    @Update
    suspend fun updateCounterRecord(record: CounterRecord)

    @Delete
    suspend fun deleteCounterRecord(record: CounterRecord)

    @Query("SELECT * FROM counter_record ORDER BY sortOrder")
    fun getAllCounters(): Flow<List<CounterRecord>>

    @Query("DELETE FROM counter_record WHERE id = :id")
    suspend fun deleteCounterById(id: Long)

    // CounterEntry operations
    @Insert(onConflict = REPLACE)
    suspend fun insertCounterEntry(entry: CounterEntry): Long

    @Update
    suspend fun updateCounterEntry(entry: CounterEntry)

    @Query("SELECT * FROM counter_entry WHERE counterId = :counterId AND date = :date")
    fun getEntryByCounterAndDate(counterId: Long, date: LocalDate): Flow<CounterEntry?>

    @Query("UPDATE counter_entry SET count = count + 1 WHERE counterId = :counterId AND date = :date")
    suspend fun incrementCounterEntryCount(counterId: Long, date: LocalDate): Int

    @Query("UPDATE counter_entry SET count = count - 1 WHERE counterId = :counterId AND date = :date AND count > 0")
    suspend fun decrementCounterEntryCount(counterId: Long, date: LocalDate): Int

    @Query("SELECT * FROM counter_entry WHERE counterId = :counterId AND date = :date")
    suspend fun getEntryByCounterAndDateSync(counterId: Long, date: LocalDate): CounterEntry?

    @Query("SELECT * FROM counter_entry WHERE date = :date")
    fun getEntriesByDate(date: LocalDate): Flow<List<CounterEntry>>

    @Query("SELECT * FROM counter_entry WHERE counterId = :counterId ORDER BY date DESC")
    fun getEntriesByCounter(counterId: Long): Flow<List<CounterEntry>>

    @Query("SELECT COALESCE(SUM(count), 0) FROM counter_entry WHERE date = :date")
    fun getTotalCounterChangeByDate(date: LocalDate): Flow<Int>

    @Query("SELECT * FROM counter_entry WHERE date BETWEEN :startDate AND :endDate")
    fun getEntriesBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<CounterEntry>>
}
