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

import org.nsh07.pomodoro.data.Note

sealed interface CollectionAction {
    data class AddNote(val note: Note) : CollectionAction
    data class UpdateNote(val note: Note) : CollectionAction
    data class DeleteNote(val noteId: Long) : CollectionAction
    data class TogglePin(val noteId: Long) : CollectionAction
    data class Search(val query: String) : CollectionAction
    data object StartSearch : CollectionAction
    data object StopSearch : CollectionAction
    data object NavigateToAddNote : CollectionAction
    data class NavigateToEditNote(val noteId: Long) : CollectionAction
}
