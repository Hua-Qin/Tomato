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

interface CustomTimerRepository {
    suspend fun insertCustomTimer(timer: CustomTimer): Long
    suspend fun updateCustomTimer(timer: CustomTimer)
    suspend fun deleteCustomTimer(timer: CustomTimer)
    suspend fun deleteCustomTimerById(id: Long)
    fun getAllCustomTimers(): Flow<List<CustomTimer>>
    suspend fun getCustomTimerById(id: Long): CustomTimer?
    suspend fun updateTimerName(id: Long, name: String)
}

class AppCustomTimerRepository(
    private val customTimerDao: CustomTimerDao,
    private val ioDispatcher: CoroutineDispatcher
) : CustomTimerRepository {
    override suspend fun insertCustomTimer(timer: CustomTimer): Long = withContext(ioDispatcher) {
        customTimerDao.insertCustomTimer(timer)
    }

    override suspend fun updateCustomTimer(timer: CustomTimer) = withContext(ioDispatcher) {
        customTimerDao.updateCustomTimer(timer)
    }

    override suspend fun deleteCustomTimer(timer: CustomTimer) = withContext(ioDispatcher) {
        customTimerDao.deleteCustomTimer(timer)
    }

    override suspend fun deleteCustomTimerById(id: Long) = withContext(ioDispatcher) {
        customTimerDao.deleteCustomTimerById(id)
    }

    override fun getAllCustomTimers(): Flow<List<CustomTimer>> = customTimerDao.getAllCustomTimers()

    override suspend fun getCustomTimerById(id: Long): CustomTimer? = withContext(ioDispatcher) {
        customTimerDao.getCustomTimerById(id)
    }

    override suspend fun updateTimerName(id: Long, name: String) = withContext(ioDispatcher) {
        customTimerDao.updateTimerName(id, name)
    }
}
