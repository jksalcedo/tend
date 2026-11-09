package com.jksalcedo.tend.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jksalcedo.tend.R
import com.jksalcedo.tend.domain.model.EventType
import com.jksalcedo.tend.domain.model.PersonEvent
import com.jksalcedo.tend.domain.model.SocialLink
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddSocialLinkDialog(
    onDismiss: () -> Unit,
    onConfirm: (SocialLink) -> Unit
) {
    var platform by remember { mutableStateOf("") }
    var handle by remember { mutableStateOf("") }

    val selectedPreset = socialPlatformPresets.firstOrNull {
        it.platform.equals(platform.trim(), ignoreCase = true)
    }
    val handleHint = selectedPreset
        ?.let { stringResource(it.handleHintRes) }
        ?: stringResource(R.string.add_person_handle_label)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_person_social_dialog_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = stringResource(R.string.add_person_platform_quick_pick),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    socialPlatformPresets.forEach { preset ->
                        FilterChip(
                            selected = preset == selectedPreset,
                            onClick = { platform = preset.platform },
                            label = { Text(stringResource(preset.labelRes)) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = platform,
                    onValueChange = { platform = it },
                    label = { Text(stringResource(R.string.add_person_platform_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = handle,
                    onValueChange = { handle = it },
                    label = { Text(stringResource(R.string.add_person_handle_label)) },
                    placeholder = { Text(handleHint) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(SocialLink(platform.trim(), handle.trim())) },
                enabled = platform.isNotBlank() && handle.isNotBlank()
            ) {
                Text(stringResource(R.string.common_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (PersonEvent) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var eventType by remember { mutableStateOf(EventType.OTHER) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var leadTimeDaysText by remember { mutableStateOf("0") }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.common_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_person_event_dialog_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = stringResource(R.string.add_person_event_quick_pick),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    eventPresets.forEach { preset ->
                        val presetLabel = stringResource(preset.labelRes)
                        FilterChip(
                            selected = label.equals(presetLabel, ignoreCase = true),
                            onClick = {
                                label = presetLabel
                                eventType = preset.type
                            },
                            label = { Text(presetLabel) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = {
                        label = it
                        eventType = inferEventType(it)
                    },
                    label = { Text(stringResource(R.string.add_person_event_label_field)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val dateText = selectedDate?.let { dateMillis ->
                        val fmt = SimpleDateFormat(
                            "MMM d, yyyy",
                            LocalLocale.current.platformLocale
                        ).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }
                        fmt.format(Date(dateMillis))
                    }
                    Text(dateText ?: stringResource(R.string.add_person_select_date))
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = leadTimeDaysText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            leadTimeDaysText = newValue
                        }
                    },
                    label = { Text(stringResource(R.string.add_person_event_lead_time_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedDate?.let { date ->
                        val leadTime = leadTimeDaysText.toIntOrNull() ?: 0
                        onConfirm(
                            PersonEvent(
                                label = label.trim(),
                                date = date,
                                type = eventType,
                                leadTimeDays = leadTime
                            )
                        )
                    }
                },
                enabled = label.isNotBlank() && selectedDate != null
            ) {
                Text(stringResource(R.string.common_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
