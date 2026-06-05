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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.data.CustomTimer
import org.nsh07.pomodoro.ui.recordsScreen.viewModel.RecordsAction
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.add_custom_timer
import tomato.shared.generated.resources.alarm
import tomato.shared.generated.resources.cancel
import tomato.shared.generated.resources.focus
import tomato.shared.generated.resources.long_break
import tomato.shared.generated.resources.session_length
import tomato.shared.generated.resources.short_break
import tomato.shared.generated.resources.timer_name
import tomato.shared.generated.resources.vibrate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddCustomTimerSheet(
    onDismiss: () -> Unit,
    onAction: (RecordsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var focusMinutes by remember { mutableStateOf("25") }
    var shortBreakMinutes by remember { mutableStateOf("5") }
    var longBreakMinutes by remember { mutableStateOf("15") }
    var sessionLength by remember { mutableStateOf("4") }
    var alarmEnabled by remember { mutableStateOf(true) }
    var vibrateEnabled by remember { mutableStateOf(true) }

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
                stringResource(Res.string.add_custom_timer),
                style = typography.headlineSmall
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(Res.string.timer_name)) },
                singleLine = true,
                shape = shapes.large,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = focusMinutes,
                    onValueChange = { focusMinutes = it },
                    label = { Text(stringResource(Res.string.focus)) },
                    suffix = { Text("min") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = shapes.large,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = shortBreakMinutes,
                    onValueChange = { shortBreakMinutes = it },
                    label = { Text(stringResource(Res.string.short_break)) },
                    suffix = { Text("min") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = shapes.large,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = longBreakMinutes,
                    onValueChange = { longBreakMinutes = it },
                    label = { Text(stringResource(Res.string.long_break)) },
                    suffix = { Text("min") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = shapes.large,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = sessionLength,
                    onValueChange = { sessionLength = it },
                    label = { Text(stringResource(Res.string.session_length)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = shapes.large,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.alarm))
                Switch(
                    checked = alarmEnabled,
                    onCheckedChange = { alarmEnabled = it }
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.vibrate))
                Switch(
                    checked = vibrateEnabled,
                    onCheckedChange = { vibrateEnabled = it }
                )
            }

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
                        val focusMs = (focusMinutes.toLongOrNull() ?: 25) * 60 * 1000
                        val shortMs = (shortBreakMinutes.toLongOrNull() ?: 5) * 60 * 1000
                        val longMs = (longBreakMinutes.toLongOrNull() ?: 15) * 60 * 1000
                        val sessions = sessionLength.toIntOrNull() ?: 4

                        onAction(
                            RecordsAction.AddCustomTimer(
                                CustomTimer(
                                    name = name.ifBlank { "专注" },
                                    focusDuration = focusMs,
                                    shortBreakDuration = shortMs,
                                    longBreakDuration = longMs,
                                    sessionLength = sessions,
                                    alarmEnabled = alarmEnabled,
                                    vibrateEnabled = vibrateEnabled
                                )
                            )
                        )
                        scope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    shapes = ButtonDefaults.shapes()
                ) {
                    Text(stringResource(Res.string.add_custom_timer))
                }
            }
        }
    }
}
