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

package org.nsh07.pomodoro.ui.collectionScreen.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.data.Note
import org.nsh07.pomodoro.data.NoteRepository

class CollectionViewModel(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _isSearching = MutableStateFlow(false)

    private val allNotes = noteRepository.getAllNotes()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val state = combine(
        allNotes,
        _searchQuery,
        _isSearching
    ) { notes, query, isSearching ->
        CollectionState(
            notes = if (isSearching && query.isNotBlank()) {
                notes.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.content.contains(query, ignoreCase = true)
                }
            } else {
                notes
            },
            searchQuery = query,
            isSearching = isSearching
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CollectionState())

    fun onAction(action: CollectionAction) {
        when (action) {
            is CollectionAction.AddNote -> addNote(action.note)
            is CollectionAction.UpdateNote -> updateNote(action.note)
            is CollectionAction.DeleteNote -> deleteNote(action.noteId)
            is CollectionAction.TogglePin -> togglePin(action.noteId)
            is CollectionAction.Search -> search(action.query)
            is CollectionAction.StartSearch -> startSearch()
            is CollectionAction.StopSearch -> stopSearch()
            is CollectionAction.NavigateToAddNote -> { /* Handled by AppScreen navigation */ }
            is CollectionAction.NavigateToEditNote -> { /* Handled by AppScreen navigation */ }
        }
    }

    private fun addNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.insertNote(note)
        }
    }

    private fun updateNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.updateNote(note)
        }
    }

    private fun deleteNote(noteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val note = allNotes.value.find { it.id == noteId }
            if (note != null) noteRepository.deleteNote(note)
        }
    }

    private fun togglePin(noteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val note = allNotes.value.find { it.id == noteId }
            if (note != null) noteRepository.updateNote(note.copy(isPinned = !note.isPinned))
        }
    }

    private fun search(query: String) {
        _searchQuery.update { query }
    }

    private fun startSearch() {
        _isSearching.update { true }
    }

    private fun stopSearch() {
        _isSearching.update { false }
        _searchQuery.update { "" }
    }
}
