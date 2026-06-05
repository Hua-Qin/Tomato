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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.nsh07.pomodoro.data.Note
import org.nsh07.pomodoro.ui.collectionScreen.viewModel.CollectionAction
import org.nsh07.pomodoro.ui.collectionScreen.viewModel.CollectionViewModel
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.arrow_back
import tomato.shared.generated.resources.back
import tomato.shared.generated.resources.note_title
import tomato.shared.generated.resources.preview
import tomato.shared.generated.resources.start_writing
import tomato.shared.generated.resources.visibility

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    noteId: Long?,
    contentPadding: PaddingValues,
    onAction: (CollectionAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CollectionViewModel = koinViewModel()
) {
    val collectionState by viewModel.state.collectAsStateWithLifecycle()

    // Find the note if editing
    val existingNote = noteId?.let { id ->
        collectionState.notes.find { it.id == id }
    }

    var title by remember(noteId) { mutableStateOf(existingNote?.title ?: "") }
    var content by remember(noteId) { mutableStateOf(existingNote?.content ?: "") }
    var isPreview by remember { mutableStateOf(false) }

    // Update local state when note loads from repository
    LaunchedEffect(existingNote) {
        if (existingNote != null) {
            title = existingNote.title
            content = existingNote.content
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        TopAppBar(
            title = {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = {
                        Text(
                            stringResource(Res.string.note_title),
                            style = typography.titleLarge
                        )
                    },
                    singleLine = true,
                    textStyle = typography.titleLarge,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    saveNote(noteId, existingNote, title, content, onAction)
                    onBack()
                }) {
                    Icon(
                        painterResource(Res.drawable.arrow_back),
                        contentDescription = stringResource(Res.string.back)
                    )
                }
            },
            actions = {
                FilledTonalIconButton(
                    onClick = { isPreview = !isPreview },
                    colors = if (isPreview) IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = colorScheme.primaryContainer
                    ) else IconButtonDefaults.filledTonalIconButtonColors()
                ) {
                    Icon(
                        painterResource(Res.drawable.visibility),
                        contentDescription = stringResource(Res.string.preview)
                    )
                }
            }
        )

        if (isPreview) {
            // Preview mode
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (title.isNotEmpty()) {
                    Text(
                        text = title,
                        style = typography.headlineMedium,
                        color = colorScheme.onSurface
                    )
                    Spacer(Modifier.height(12.dp))
                }
                if (content.isNotEmpty()) {
                    Text(
                        text = renderMarkdownAnnotated(content),
                        style = typography.bodyLarge,
                        color = colorScheme.onSurface
                    )
                }
            }
        } else {
            // Edit mode
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Content editor
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = {
                        Text(stringResource(Res.string.start_writing))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = shapes.extraSmall
                )

                HorizontalDivider()

                // Formatting toolbar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    FormatButton(label = "B", style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        content = insertMarkdown(content, "**", "**")
                    }
                    FormatButton(label = "I", style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        content = insertMarkdown(content, "*", "*")
                    }
                    FormatButton(label = "H") {
                        content = insertMarkdownAtLineStart(content, "# ")
                    }
                    FormatButton(label = "\u2022") {
                        content = insertMarkdownAtLineStart(content, "- ")
                    }
                    FormatButton(label = "</>") {
                        content = insertMarkdown(content, "`", "`")
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatButton(
    label: String,
    style: SpanStyle = SpanStyle(),
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    TextButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = Modifier.size(48.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(style) { append(label) }
            },
            style = typography.titleSmall,
            color = colorScheme.onSurfaceVariant
        )
    }
}

private fun saveNote(
    noteId: Long?,
    existingNote: Note?,
    title: String,
    content: String,
    onAction: (CollectionAction) -> Unit
) {
    val now = System.currentTimeMillis()
    if (noteId != null && existingNote != null) {
        onAction(
            CollectionAction.UpdateNote(
                existingNote.copy(
                    title = title,
                    content = content,
                    updatedAt = now
                )
            )
        )
    } else if (title.isNotEmpty() || content.isNotEmpty()) {
        onAction(
            CollectionAction.AddNote(
                Note(
                    title = title,
                    content = content,
                    createdAt = now,
                    updatedAt = now
                )
            )
        )
    }
}

private fun insertMarkdown(text: String, prefix: String, suffix: String): String {
    return "$text$prefix$suffix"
}

private fun insertMarkdownAtLineStart(text: String, prefix: String): String {
    return "$prefix$text"
}

@Composable
private fun renderMarkdownAnnotated(content: String): AnnotatedString {
    val codeBackground = colorScheme.surfaceVariant
    return buildAnnotatedString {
        content.lines().forEachIndexed { index, line ->
            if (index > 0) append("\n")
            when {
                line.startsWith("### ") -> withStyle(
                    SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                ) { append(line.removePrefix("### ")) }

                line.startsWith("## ") -> withStyle(
                    SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                ) { append(line.removePrefix("## ")) }

                line.startsWith("# ") -> withStyle(
                    SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
                ) { append(line.removePrefix("# ")) }

                line.startsWith("- ") -> {
                    append("• ")
                    processInlineStyles(line.removePrefix("- "), codeBackground)
                }

                else -> processInlineStyles(line, codeBackground)
            }
        }
    }
}

private fun AnnotatedString.Builder.processInlineStyles(
    text: String,
    codeBackground: Color
) {
    val regex = Regex("""(\*\*(.+?)\*\*|\*(.+?)\*|`(.+?)`)""")
    var lastIndex = 0
    regex.findAll(text).forEach { match ->
        append(text.substring(lastIndex, match.range.first))
        when {
            match.groupValues[2].isNotEmpty() -> withStyle(
                SpanStyle(fontWeight = FontWeight.Bold)
            ) { append(match.groupValues[2]) }

            match.groupValues[3].isNotEmpty() -> withStyle(
                SpanStyle(fontStyle = FontStyle.Italic)
            ) { append(match.groupValues[3]) }

            match.groupValues[4].isNotEmpty() -> withStyle(
                SpanStyle(fontFamily = FontFamily.Monospace, background = codeBackground)
            ) { append(match.groupValues[4]) }
        }
        lastIndex = match.range.last + 1
    }
    if (lastIndex < text.length) append(text.substring(lastIndex))
}
