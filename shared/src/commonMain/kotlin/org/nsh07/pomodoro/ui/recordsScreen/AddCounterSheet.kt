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

package org.nsh07.pomodoro.ui.recordsScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.ui.recordsScreen.viewModel.RecordsAction
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.add_counter
import tomato.shared.generated.resources.cancel
import tomato.shared.generated.resources.counter_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCounterSheet(
    onDismiss: () -> Unit,
    onAction: (RecordsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorScheme.surfaceContainer,
        contentColor = colorScheme.onSurface,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                stringResource(Res.string.add_counter),
                style = typography.headlineSmall
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(Res.string.counter_title)) },
                singleLine = true,
                shape = shapes.large,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                FilledTonalButton(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    shapes = ButtonDefaults.shapes()
                ) {
                    Text(stringResource(Res.string.cancel))
                }
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onAction(RecordsAction.AddCounter(title))
                        }
                        scope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    enabled = title.isNotBlank(),
                    shapes = ButtonDefaults.shapes()
                ) {
                    Text(stringResource(Res.string.add_counter))
                }
            }
        }
    }
}
