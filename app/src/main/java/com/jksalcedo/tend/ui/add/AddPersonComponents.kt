package com.jksalcedo.tend.ui.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.jksalcedo.tend.R
import com.jksalcedo.tend.domain.model.EventType
import com.jksalcedo.tend.domain.model.PersonEvent
import com.jksalcedo.tend.domain.model.SocialLink
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

@Composable
fun AddSocialLinkDialog(
    onDismiss: () -> Unit,
    onConfirm: (SocialLink) -> Unit
) {
    var platform by remember { mutableStateOf("") }
    var handle by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_person_social_dialog_title)) },
        text = {
            Column {
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
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(SocialLink(platform, handle)) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (PersonEvent) -> Unit
) {
    var label by remember { mutableStateOf("") }
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
            Column {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
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
                    label = { Text("Reminder lead time (days)") },
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
                        val inferredType = when {
                            label.contains("birthday", ignoreCase = true) || label.contains(
                                "bday",
                                ignoreCase = true
                            ) -> EventType.BIRTHDAY

                            label.contains(
                                "anniversary",
                                ignoreCase = true
                            ) -> EventType.ANNIVERSARY

                            else -> EventType.OTHER
                        }
                        onConfirm(
                            PersonEvent(
                                label = label,
                                date = date,
                                type = inferredType,
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
