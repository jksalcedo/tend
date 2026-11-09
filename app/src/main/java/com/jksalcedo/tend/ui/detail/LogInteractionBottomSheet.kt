package com.jksalcedo.tend.ui.detail
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.jksalcedo.tend.domain.model.InteractionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogInteractionBottomSheet(
    onDismissRequest: () -> Unit,
    onLogInteraction: (InteractionType?, String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedType by remember { mutableStateOf<InteractionType?>(InteractionType.CALL) }
    var noteText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Log Interaction",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "How did you connect?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InteractionType.entries.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { type ->
                            val isSelected = selectedType == type
                            FilterChip(
                                modifier = Modifier.weight(1f)
                                    .height(48.dp),
                                selected = isSelected,
                                onClick = { selectedType = if (isSelected) null else type },
                                label = {
                                    Text(
                                        text = type.displayName,
                                        maxLines = 1
                                    )
                                },
                                leadingIcon = {
                                    val icon = getInteractionIcon(type)
                                    Icon(imageVector = icon, contentDescription = type.displayName)
                                }
                            )
                        }
                        if (rowItems.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Interaction Notes (Optional)") },
                placeholder = { Text("e.g. Discussed weekend plans, had coffee") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = {
                    onLogInteraction(null, null)
                    onDismissRequest()
                }) {
                    Text("Quick Check-In")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        onLogInteraction(selectedType, noteText.ifBlank { null })
                        onDismissRequest()
                    }
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Log")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

fun getInteractionIcon(type: InteractionType): ImageVector {
    return when (type) {
        InteractionType.CALL -> Icons.Default.Phone
        InteractionType.MESSAGE -> Icons.AutoMirrored.Filled.Send
        InteractionType.MEETUP -> Icons.Default.Face
        InteractionType.VIDEO_CALL -> Icons.Default.Videocam
        InteractionType.OTHER -> Icons.Default.Edit
    }
}
