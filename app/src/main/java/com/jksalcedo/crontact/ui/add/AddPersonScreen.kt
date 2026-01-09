package com.jksalcedo.crontact.ui.add

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jksalcedo.crontact.ui.add.AddPersonViewModel
import com.jksalcedo.crontact.ui.theme.CronTactPastels
import com.jksalcedo.crontact.ui.theme.CronTactTheme

@Composable
fun AddPersonScreen(
    viewModel: AddPersonViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var cadence by remember { mutableStateOf("14") }
    var notes by remember { mutableStateOf("") }

    AddPersonScreenContent(
        name = name,
        onNameChange = { name = it },
        cadence = cadence,
        onCadenceChange = { cadence = it },
        notes = notes,
        onNotesChange = { notes = it },
        onSaveClick = {
            val cadenceInt = cadence.toIntOrNull() ?: 14
            viewModel.addPerson(name, cadenceInt, notes)
            onNavigateBack()
        },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPersonScreenContent(
    name: String,
    onNameChange: (String) -> Unit,
    cadence: String,
    onCadenceChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    val mintColor = if (isDarkTheme) CronTactPastels.MintDark else CronTactPastels.Mint
    val mintAccent = if (isDarkTheme) CronTactPastels.Mint else CronTactPastels.MintDark

    val purpleColor = if (isDarkTheme) CronTactPastels.PurpleDark else CronTactPastels.Purple
    val purpleAccent = if (isDarkTheme) CronTactPastels.Purple else CronTactPastels.PurpleDark

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
                text = "Add Person",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
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


            // Cadence Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = mintColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Check-in Cadence",
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
                            val isSelected = cadence == days
                            Surface(
                                onClick = { onCadenceChange(days) },
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
                        value = cadence,
                        onValueChange = onCadenceChange,
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

            Spacer(modifier = Modifier.weight(1f))

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
                    "Save Contact",
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
    CronTactTheme(darkTheme = false) {
        AddPersonScreenContent(
            name = "",
            onNameChange = {},
            cadence = "14",
            onCadenceChange = {},
            notes = "",
            onNotesChange = {},
            onSaveClick = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark Mode")
@Composable
private fun AddPersonScreenPreviewDark() {
    CronTactTheme(darkTheme = true) {
        AddPersonScreenContent(
            name = "",
            onNameChange = {},
            cadence = "14",
            onCadenceChange = {},
            notes = "",
            onNotesChange = {},
            onSaveClick = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Light Mode - Filled")
@Composable
private fun AddPersonScreenFilledPreviewLight() {
    CronTactTheme(darkTheme = false) {
        AddPersonScreenContent(
            name = "Aunt Denise",
            onNameChange = {},
            cadence = "14",
            onCadenceChange = {},
            notes = "Lives in Silverton, TN. Allergic to shellfish.",
            onNotesChange = {},
            onSaveClick = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark Mode - Filled")
@Composable
private fun AddPersonScreenFilledPreviewDark() {
    CronTactTheme(darkTheme = true) {
        AddPersonScreenContent(
            name = "Aunt Denise",
            onNameChange = {},
            cadence = "14",
            onCadenceChange = {},
            notes = "Lives in Silverton, TN. Allergic to shellfish.",
            onNotesChange = {},
            onSaveClick = {},
            onNavigateBack = {}
        )
    }
}
