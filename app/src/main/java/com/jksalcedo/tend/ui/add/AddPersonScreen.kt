package com.jksalcedo.tend.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import com.google.gson.Gson
import com.jksalcedo.tend.domain.model.EventType
import com.jksalcedo.tend.domain.model.PersonEvent
import com.jksalcedo.tend.domain.model.SocialLink
import com.jksalcedo.tend.ui.theme.TendPastels
import com.jksalcedo.tend.ui.theme.TendTheme
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun AddPersonScreen(
    viewModel: AddPersonViewModel = koinViewModel(),
    personId: Long? = null,
    sharedData: String? = null,
    onNavigateBack: () -> Unit
) {
    val isEditMode = personId != null
    val existingPerson by viewModel.existingPerson.collectAsState()

    LaunchedEffect(personId) {
        if (personId != null) viewModel.loadPerson(personId)
    }

    val sharedPerson = remember(sharedData) {
        if (!sharedData.isNullOrBlank()) {
            try {
                Gson().fromJson(sharedData, SharedPerson::class.java)
            } catch (e: Exception) {
                try {
                    val decoded = java.net.URLDecoder.decode(sharedData, "UTF-8")
                    Gson().fromJson(decoded, SharedPerson::class.java)
                } catch (e2: Exception) {
                    null
                }
            }
        } else null
    }

    var name by remember { mutableStateOf(sharedPerson?.name ?: "") }
    var phoneNumber by remember { mutableStateOf(sharedPerson?.phoneNumber ?: "") }
    var email by remember { mutableStateOf(sharedPerson?.email ?: "") }
    var socialLinks by remember {
        mutableStateOf(
            sharedPerson?.socialLinks?.map { SocialLink(it.platform, it.handle) } ?: emptyList()
        )
    }
    var events by remember {
        mutableStateOf(
            sharedPerson?.events?.map {
                PersonEvent(
                    label = it.label,
                    date = it.date,
                    type = try { EventType.valueOf(it.type) } catch (ex: Exception) { EventType.OTHER }
                )
            } ?: emptyList()
        )
    }
    var frequency by remember { mutableStateOf((sharedPerson?.frequencyDays ?: 14).toString()) }
    var notes by remember { mutableStateOf(sharedPerson?.notes ?: "") }

    // Pre-fill from loaded person when in edit mode
    LaunchedEffect(existingPerson) {
        existingPerson?.let { p ->
            name = p.name
            phoneNumber = p.phoneNumber ?: ""
            email = p.email ?: ""
            socialLinks = p.socialLinks
            events = p.events
            frequency = p.frequencyDays.toString()
        }
    }

    var showSocialDialog by remember { mutableStateOf(false) }
    var showEventDialog by remember { mutableStateOf(false) }

    if (showSocialDialog) {
        AddSocialLinkDialog(
            onDismiss = { showSocialDialog = false },
            onConfirm = { link ->
                socialLinks = socialLinks + link
                showSocialDialog = false
            }
        )
    }

    if (showEventDialog) {
        AddEventDialog(
            onDismiss = { showEventDialog = false },
            onConfirm = { event ->
                events = events + event
                showEventDialog = false
            }
        )
    }

    AddPersonScreenContent(
        isEditMode = isEditMode,
        name = name,
        onNameChange = { name = it },
        phoneNumber = phoneNumber,
        onPhoneNumberChange = { phoneNumber = it },
        email = email,
        onEmailChange = { email = it },
        socialLinks = socialLinks,
        onAddSocialLink = { showSocialDialog = true },
        onRemoveSocialLink = { link -> socialLinks = socialLinks - link },
        events = events,
        onAddEvent = { showEventDialog = true },
        onRemoveEvent = { event -> events = events - event },
        frequency = frequency,
        onFrequencyChange = { frequency = it },
        notes = notes,
        onNotesChange = { notes = it },
        onSaveClick = {
            val frequencyDays = frequency.toIntOrNull() ?: 14
            if (isEditMode && personId != null) {
                viewModel.updatePerson(
                    personId = personId,
                    name = name,
                    frequencyDays = frequencyDays,
                    phoneNumber = phoneNumber.ifBlank { null },
                    email = email.ifBlank { null },
                    socialLinks = socialLinks,
                    events = events
                )
            } else {
                viewModel.addPerson(
                    name = name,
                    frequencyDays = frequencyDays,
                    notes = notes,
                    phoneNumber = phoneNumber.ifBlank { null },
                    email = email.ifBlank { null },
                    socialLinks = socialLinks,
                    events = events
                )
            }
            onNavigateBack()
        },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPersonScreenContent(
    isEditMode: Boolean = false,
    name: String,
    onNameChange: (String) -> Unit,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    socialLinks: List<SocialLink>,
    onAddSocialLink: () -> Unit,
    onRemoveSocialLink: (SocialLink) -> Unit = {},
    events: List<PersonEvent>,
    onAddEvent: () -> Unit,
    onRemoveEvent: (PersonEvent) -> Unit = {},
    frequency: String,
    onFrequencyChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    val mintColor = if (isDarkTheme) TendPastels.MintDark else TendPastels.Mint
    val mintAccent = if (isDarkTheme) TendPastels.Mint else TendPastels.MintDark

    val purpleColor = if (isDarkTheme) TendPastels.PurpleDark else TendPastels.Purple
    val purpleAccent = if (isDarkTheme) TendPastels.Purple else TendPastels.PurpleDark

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .padding(top = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = if (isEditMode) "Edit Connection" else "Add Connection",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Name") },
                placeholder = { Text("Enter person's name") },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                singleLine = true
            )

            // Phone Field
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = onPhoneNumberChange,
                label = { Text("Phone Number") },
                placeholder = { Text("+63 994 6154 397") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                singleLine = true
            )

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                placeholder = { Text("example@email.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                singleLine = true
            )

            // Social Links Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Social Links",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = onAddSocialLink) {
                            Text("Add")
                        }
                    }
                    socialLinks.forEach { link ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${link.platform}: ${link.handle}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 4.dp).weight(1f)
                            )
                            IconButton(onClick = { onRemoveSocialLink(link) }) {
                                Icon(
                                    androidx.compose.material.icons.Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Events Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Important Dates",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = onAddEvent) {
                            Text("Add")
                        }
                    }
                    events.forEach { event ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = buildString {
                                    append(event.label)
                                    append(": ")
                                    append(
                                        SimpleDateFormat(
                                            "MMM dd",
                                            LocalLocale.current.platformLocale
                                        ).format(Date(event.date))
                                    )
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 4.dp).weight(1f)
                            )
                            IconButton(onClick = { onRemoveEvent(event) }) {
                                Icon(
                                    androidx.compose.material.icons.Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }


            // Frequency Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = mintColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Check-in frequency",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = mintAccent
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "How often should you reach out?",
                        style = MaterialTheme.typography.bodySmall,
                        color = mintAccent.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick select chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "7" to "Weekly",
                            "14" to "Bi-weekly",
                            "30" to "Monthly"
                        ).forEach { (days, label) ->
                            val isSelected = frequency == days
                            Surface(
                                onClick = { onFrequencyChange(days) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) mintAccent else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.surface else mintAccent
                                    )
                                    Text(
                                        text = "$days days",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.surface.copy(
                                            alpha = 0.8f
                                        ) else mintAccent.copy(
                                            alpha = 0.7f
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Custom input
                    OutlinedTextField(
                        value = frequency,
                        onValueChange = onFrequencyChange,
                        label = { Text("Custom (days)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = mintAccent,
                            unfocusedBorderColor = mintAccent.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        singleLine = true
                    )
                }
            }

            // Notes Field
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = purpleColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = purpleAccent
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = onNotesChange,
                        placeholder = { Text("How did you meet? What do they like?") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = purpleAccent,
                            unfocusedBorderColor = purpleAccent.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        minLines = 4,
                        maxLines = 6
                    )
                }
            }

            // Save Button
            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            ) {
                Text(
                    if (isEditMode) "Save Changes" else "Save Connection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
private fun AddPersonScreenPreviewLight() {
    TendTheme(darkTheme = false) {
        AddPersonScreenContent(
            name = "",
            onNameChange = {},
            phoneNumber = "",
            onPhoneNumberChange = {},
            email = "",
            onEmailChange = {},
            frequency = "14",
            onFrequencyChange = {},
            notes = "",
            onNotesChange = {},
            onSaveClick = {},
            onNavigateBack = {},
            socialLinks = emptyList(),
            onAddSocialLink = {},
            events = emptyList(),
            onAddEvent = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark Mode")
@Composable
private fun AddPersonScreenPreviewDark() {
    TendTheme(darkTheme = true) {
        AddPersonScreenContent(
            name = "",
            onNameChange = {},
            phoneNumber = "",
            onPhoneNumberChange = {},
            email = "",
            onEmailChange = {},
            frequency = "14",
            onFrequencyChange = {},
            notes = "",
            onNotesChange = {},
            onSaveClick = {},
            onNavigateBack = {},
            socialLinks = emptyList(),
            onAddSocialLink = {},
            events = emptyList(),
            onAddEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Light Mode - Filled")
@Composable
private fun AddPersonScreenFilledPreviewLight() {
    TendTheme(darkTheme = false) {
        AddPersonScreenContent(
            name = "Aunt Denise",
            onNameChange = {},
            phoneNumber = "555-0123",
            onPhoneNumberChange = {},
            email = "denise@example.com",
            onEmailChange = {},
            frequency = "14",
            onFrequencyChange = {},
            notes = "Lives in Silverton, TN. Allergic to shellfish.",
            onNotesChange = {},
            onSaveClick = {},
            onNavigateBack = {},
            socialLinks = emptyList(),
            onAddSocialLink = {},
            events = emptyList(),
            onAddEvent = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark Mode - Filled")
@Composable
private fun AddPersonScreenFilledPreviewDark() {
    TendTheme(darkTheme = true) {
        AddPersonScreenContent(
            name = "Aunt Denise",
            onNameChange = {},
            phoneNumber = "555-0123",
            onPhoneNumberChange = {},
            email = "denise@example.com",
            onEmailChange = {},
            frequency = "14",
            onFrequencyChange = {},
            notes = "Lives in Silverton, TN. Allergic to shellfish.",
            onNotesChange = {},
            onSaveClick = {},
            onNavigateBack = {},
            socialLinks = emptyList(),
            onAddSocialLink = {},
            events = emptyList(),
            onAddEvent = {}
        )
    }
}
