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

interface NoteRepository {
    suspend fun insertNote(note: Note): Long
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(note: Note)
    fun getAllNotes(): Flow<List<Note>>
    fun getNoteById(id: Long): Flow<Note?>
    fun searchNotes(query: String): Flow<List<Note>>
    suspend fun togglePin(noteId: Long)
}

class AppNoteRepository(
    private val noteDao: NoteDao,
    private val ioDispatcher: CoroutineDispatcher
) : NoteRepository {
    override suspend fun insertNote(note: Note): Long = withContext(ioDispatcher) {
        noteDao.insertNote(note)
    }

    override suspend fun updateNote(note: Note) = withContext(ioDispatcher) {
        noteDao.updateNote(note)
    }

    override suspend fun deleteNote(note: Note) = withContext(ioDispatcher) {
        noteDao.deleteNote(note)
    }

    override fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()

    override fun getNoteById(id: Long): Flow<Note?> = noteDao.getNoteById(id)

    override fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)

    override suspend fun togglePin(noteId: Long) = withContext(ioDispatcher) {
        val note = noteDao.getNoteByIdSync(noteId) ?: return@withContext
        noteDao.updateNote(note.copy(isPinned = !note.isPinned))
    }
}
