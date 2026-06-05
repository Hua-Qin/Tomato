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

package org.nsh07.pomodoro.ui.collectionScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.nsh07.pomodoro.data.Note
import org.nsh07.pomodoro.ui.collectionScreen.viewModel.CollectionAction
import org.nsh07.pomodoro.ui.collectionScreen.viewModel.CollectionViewModel
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.add
import tomato.shared.generated.resources.add_note
import tomato.shared.generated.resources.cancel
import tomato.shared.generated.resources.clear
import tomato.shared.generated.resources.collection
import tomato.shared.generated.resources.delete
import tomato.shared.generated.resources.edit
import tomato.shared.generated.resources.edit_note
import tomato.shared.generated.resources.folder
import tomato.shared.generated.resources.no_notes
import tomato.shared.generated.resources.pin_note
import tomato.shared.generated.resources.push_pin
import tomato.shared.generated.resources.search
import tomato.shared.generated.resources.search_notes
import tomato.shared.generated.resources.unpin_note
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CollectionScreen(
    contentPadding: PaddingValues,
    onAction: (CollectionAction) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CollectionViewModel = koinViewModel()
) {
    val collectionState by viewModel.state.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            TopAppBar(
                title = {
                    Text(
                        stringResource(Res.string.collection),
                        style = typography.titleLarge
                    )
                },
                actions = {
                    if (!collectionState.isSearching) {
                        IconButton(onClick = { onAction(CollectionAction.StartSearch) }) {
                            Icon(
                                painterResource(Res.drawable.search),
                                contentDescription = stringResource(Res.string.search_notes)
                            )
                        }
                    } else {
                        IconButton(onClick = { onAction(CollectionAction.StopSearch) }) {
                            Icon(
                                painterResource(Res.drawable.clear),
                                contentDescription = stringResource(Res.string.cancel)
                            )
                        }
                    }
                }
            )

            // Search bar
            AnimatedVisibility(
                visible = collectionState.isSearching,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                OutlinedTextField(
                    value = collectionState.searchQuery,
                    onValueChange = { onAction(CollectionAction.Search(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = {
                        Text(stringResource(Res.string.search_notes))
                    },
                    leadingIcon = {
                        Icon(
                            painterResource(Res.drawable.search),
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = colorScheme.surfaceContainerHigh
                    ),
                    shape = shapes.large
                )
            }

            // Notes list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    collectionState.notes,
                    key = { it.id },
                    contentType = { "note" }
                ) { note ->
                    NoteCard(
                        note = note,
                        onAction = onAction
                    )
                }

                // Empty state
                if (collectionState.notes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painterResource(Res.drawable.folder),
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = colorScheme.outlineVariant.copy(alpha = 0.6f)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = stringResource(Res.string.no_notes),
                                    style = typography.bodyLarge,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // FAB
        SmallFloatingActionButton(
            onClick = { onAction(CollectionAction.NavigateToAddNote) },
            containerColor = colorScheme.primaryContainer,
            contentColor = colorScheme.onPrimaryContainer,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 16.dp,
                    bottom = contentPadding.calculateBottomPadding() + ScreenOffset
                )
        ) {
            Icon(
                painterResource(Res.drawable.add),
                contentDescription = stringResource(Res.string.add_note),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteCard(
    note: Note,
    onAction: (CollectionAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    Surface(
        shape = shapes.medium,
        color = if (note.isPinned) colorScheme.primaryContainer else colorScheme.surfaceBright,
        tonalElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onAction(CollectionAction.NavigateToEditNote(note.id)) },
                onLongClick = { showMenu = true }
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (note.title.isNotEmpty()) {
                        Text(
                            text = note.title,
                            style = typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = colorScheme.onSurface
                        )
                    }
                    if (note.content.isNotEmpty()) {
                        Text(
                            text = note.content.take(100),
                            style = typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (note.isPinned) {
                    Surface(
                        shape = CircleShape,
                        color = colorScheme.primaryContainer,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painterResource(Res.drawable.push_pin),
                            contentDescription = stringResource(Res.string.pin_note),
                            modifier = Modifier.size(12.dp),
                            tint = colorScheme.primary
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = dateFormatter.format(note.updatedAt),
                style = typography.labelSmall,
                color = colorScheme.outline
            )
        }
    }

    // Context menu
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(if (note.isPinned) Res.string.unpin_note else Res.string.pin_note)) },
            leadingIcon = {
                Icon(
                    painterResource(Res.drawable.push_pin),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            onClick = {
                onAction(CollectionAction.TogglePin(note.id))
                showMenu = false
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(Res.string.edit_note)) },
            leadingIcon = {
                Icon(
                    painterResource(Res.drawable.edit),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            onClick = {
                onAction(CollectionAction.NavigateToEditNote(note.id))
                showMenu = false
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(Res.string.delete)) },
            leadingIcon = {
                Icon(
                    painterResource(Res.drawable.delete),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            onClick = {
                onAction(CollectionAction.DeleteNote(note.id))
                showMenu = false
            }
        )
    }
}
