package com.jksalcedo.tend.ui.home

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.jksalcedo.tend.domain.model.Note
import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.ui.add.SharedPerson
import com.jksalcedo.tend.ui.theme.TendPastels
import com.jksalcedo.tend.ui.theme.TendTheme
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onAddPersonClick: (String?) -> Unit,
    onPersonClick: (Long) -> Unit,
    onOpenNotificationSettings: () -> Unit = {},
    onArchivedClick: () -> Unit = {}
) {
    val people by viewModel.people.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    HomeScreenContent(
        people = people,
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::updateSearchQuery,
        onAddPersonClick = onAddPersonClick,
        onPersonClick = onPersonClick,
        onOpenNotificationSettings = onOpenNotificationSettings,
        onArchivedClick = onArchivedClick
    )
}

@Composable
private fun HomeScreenContent(
    people: List<Person>,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onAddPersonClick: (String?) -> Unit,
    onPersonClick: (Long) -> Unit,
    onOpenNotificationSettings: () -> Unit = {},
    onArchivedClick: () -> Unit = {}
) {
    val isDarkTheme = isSystemInDarkTheme()
    val dueSoon =
        people.filter { it.nextReminderAt <= System.currentTimeMillis() + TimeUnit.DAYS.toMillis(3) }

    val context = LocalContext.current
    val scanQrCodeLauncher = rememberLauncherForActivityResult(ScanQRCode()) { result ->
        when (result) {
            is QRResult.QRSuccess -> {
                val rawValue = result.content.rawValue
                if (!rawValue.isNullOrBlank()) {
                    try {
                        val shared = Gson().fromJson(rawValue, SharedPerson::class.java)
                        if (shared != null && shared.name.isNotBlank()) {
                            val encodedData = java.net.URLEncoder.encode(rawValue, "UTF-8")
                            onAddPersonClick(encodedData)
                        } else {
                            Toast.makeText(
                                context,
                                "Scanned QR code does not contain a valid connection",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (_: Exception) {
                        if (rawValue.length < 100) {
                            val fallbackJson = Gson().toJson(SharedPerson(name = rawValue))
                            val encodedData = java.net.URLEncoder.encode(fallbackJson, "UTF-8")
                            onAddPersonClick(encodedData)
                        } else {
                            Toast.makeText(
                                context,
                                "Scanned QR code is not a valid connection format",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

            is QRResult.QRMissingPermission -> {
                Toast.makeText(
                    context,
                    "Camera permission is required to scan QR codes",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is QRResult.QRError -> {
                Toast.makeText(
                    context,
                    "Error scanning QR code: ${result.exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {}
        }
    }

    // Resolve colors based on theme
    val mintColor = if (isDarkTheme) TendPastels.MintDark else TendPastels.Mint
    val mintAccent = if (isDarkTheme) TendPastels.Mint else TendPastels.MintDark

    val yellowColor = if (isDarkTheme) TendPastels.YellowDark else TendPastels.Yellow
    val yellowAccent = if (isDarkTheme) TendPastels.Yellow else TendPastels.YellowDark

    val purpleColor = if (isDarkTheme) TendPastels.PurpleDark else TendPastels.Purple
    val purpleAccent = if (isDarkTheme) TendPastels.Purple else TendPastels.PurpleDark

    val pinkColor = if (isDarkTheme) TendPastels.PinkDark else TendPastels.Pink
    val pinkAccent = if (isDarkTheme) TendPastels.Pink else TendPastels.PinkDark

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp)
                .padding(top = 48.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(purpleColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = purpleAccent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Hi there!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { scanQrCodeLauncher.launch(null) }) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR Code",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onOpenNotificationSettings) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    var showHomeMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showHomeMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = showHomeMenu,
                            onDismissRequest = { showHomeMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Archived connections") },
                                onClick = {
                                    showHomeMenu = false
                                    onArchivedClick()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dashboard Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardCard(
                    modifier = Modifier.weight(1f),
                    title = "Due Soon",
                    icon = Icons.Default.DateRange,
                    backgroundColor = yellowColor,
                    contentColor = yellowAccent,
                    count = dueSoon.size,
                    label = "reminders"
                )

                DashboardCard(
                    modifier = Modifier.weight(1f),
                    title = "Connections",
                    icon = Icons.Default.Person,
                    backgroundColor = purpleColor,
                    contentColor = purpleAccent,
                    count = people.size,
                    label = if (people.size == 1) "connection" else "connections"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search connections...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = mintAccent,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section Header
            Text(
                text = "Your Connections",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Connections List
            if (people.isEmpty()) {
                EmptyStateCard(
                    onAddPersonClick = { onAddPersonClick(null) },
                    pinkColor = pinkColor,
                    pinkAccent = pinkAccent
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(people) { person ->
                        PersonCard(
                            person = person,
                            mintColor = mintColor,
                            mintAccent = mintAccent,
                            yellowColor = yellowColor,
                            yellowAccent = yellowAccent,
                            purpleColor = purpleColor,
                            purpleAccent = purpleAccent,
                            pinkColor = pinkColor,
                            pinkAccent = pinkAccent,
                            onClick = { onPersonClick(person.id) }
                        )
                    }
                }
            }
        }

        // FAB
        ExtendedFloatingActionButton(
            onClick = { onAddPersonClick(null) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("New Connection", fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun DashboardCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    count: Int,
    label: String
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = contentColor
            ) {
                Text(
                    text = "$count $label",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = backgroundColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    onAddPersonClick: () -> Unit,
    pinkColor: Color,
    pinkAccent: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = pinkColor)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🌱", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No connections yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = pinkAccent
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Add someone to start keeping in touch",
                style = MaterialTheme.typography.bodyMedium,
                color = pinkAccent.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddPersonClick,
                colors = ButtonDefaults.buttonColors(containerColor = pinkAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add Connection")
            }
        }
    }
}

@Composable
fun PersonCard(
    person: Person,
    mintColor: Color,
    mintAccent: Color,
    yellowColor: Color,
    yellowAccent: Color,
    purpleColor: Color,
    purpleAccent: Color,
    pinkColor: Color,
    pinkAccent: Color,
    onClick: () -> Unit
) {
    val now = System.currentTimeMillis()
    val daysUntil = TimeUnit.MILLISECONDS.toDays(person.nextReminderAt - now)
    val isOverdue = daysUntil < 0
    val isDueSoon = daysUntil in 0..3

    val cardColor = when {
        isOverdue -> pinkColor
        isDueSoon -> yellowColor
        else -> MaterialTheme.colorScheme.surface
    }
    val accentColor = when {
        isOverdue -> pinkAccent
        isDueSoon -> yellowAccent
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (cardColor == MaterialTheme.colorScheme.surface) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (cardColor == MaterialTheme.colorScheme.surface) mintColor else MaterialTheme.colorScheme.surface.copy(
                            alpha = 0.5f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = person.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (cardColor == MaterialTheme.colorScheme.surface) mintAccent else accentColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (cardColor == MaterialTheme.colorScheme.surface) MaterialTheme.colorScheme.onSurface else accentColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        isOverdue -> "Overdue by ${-daysUntil} day${if (-daysUntil != 1L) "s" else ""}"
                        daysUntil == 0L -> "Due today!"
                        daysUntil == 1L -> "Due tomorrow"
                        else -> "Due in $daysUntil days"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (cardColor == MaterialTheme.colorScheme.surface) MaterialTheme.colorScheme.onSurfaceVariant else accentColor.copy(
                        alpha = 0.8f
                    )
                )
            }

            // Reminder badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (cardColor == MaterialTheme.colorScheme.surface) purpleColor else MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val dateFormat = SimpleDateFormat("MMM", LocalLocale.current.platformLocale)
                    val dayFormat = SimpleDateFormat("dd", LocalLocale.current.platformLocale)
                    val nextDate = Date(person.nextReminderAt)
                    Text(
                        text = dateFormat.format(nextDate).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (cardColor == MaterialTheme.colorScheme.surface) purpleAccent else accentColor,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = dayFormat.format(nextDate),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (cardColor == MaterialTheme.colorScheme.surface) purpleAccent else accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F9FA, name = "Light Mode")
@Composable
private fun HomeScreenEmptyPreviewLight() {
    TendTheme(darkTheme = false) {
        HomeScreenContent(
            people = emptyList(),
            onAddPersonClick = { _ -> },
            onPersonClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark Mode")
@Composable
private fun HomeScreenEmptyPreviewDark() {
    TendTheme(darkTheme = true) {
        HomeScreenContent(
            people = emptyList(),
            onAddPersonClick = { _ -> },
            onPersonClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F9FA, name = "Light Mode - With People")
@Composable
private fun HomeScreenWithPeoplePreviewLight() {
    TendTheme(darkTheme = false) {
        HomeScreenContent(
            people = listOf(
                Person(
                    id = 1,
                    name = "Aunt Denise",
                    photoUri = null,
                    phoneNumber = "555-0101",
                    email = "denise@example.com",
                    notes = listOf(Note(content = "Met at conference")),
                    events = emptyList(),
                    socialLinks = emptyList(),
                    frequencyDays = 14,
                    lastContactedAt = System.currentTimeMillis(),
                    nextReminderAt = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2)
                ),
                Person(
                    id = 2,
                    name = "Sarah",
                    photoUri = null,
                    phoneNumber = null,
                    email = null,
                    notes = listOf(Note(content = "College friend")),
                    events = emptyList(),
                    socialLinks = emptyList(),
                    frequencyDays = 30,
                    lastContactedAt = System.currentTimeMillis(),
                    nextReminderAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)
                ),
                Person(
                    id = 3,
                    name = "Bob",
                    photoUri = null,
                    phoneNumber = null,
                    email = null,
                    notes = listOf(Note(content = "Work colleague")),
                    events = emptyList(),
                    socialLinks = emptyList(),
                    frequencyDays = 7,
                    lastContactedAt = System.currentTimeMillis(),
                    nextReminderAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10)
                )
            ),
            onAddPersonClick = { _ -> },
            onPersonClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark Mode - With People")
@Composable
private fun HomeScreenWithPeoplePreviewDark() {
    TendTheme(darkTheme = true) {
        HomeScreenContent(
            people = listOf(
                Person(
                    id = 1,
                    name = "Aunt Denise",
                    photoUri = null,
                    phoneNumber = "555-0101",
                    email = "denise@example.com",
                    notes = listOf(Note(content = "Met at conference")),
                    events = emptyList(),
                    socialLinks = emptyList(),
                    frequencyDays = 14,
                    lastContactedAt = System.currentTimeMillis(),
                    nextReminderAt = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2)
                ),
                Person(
                    id = 2,
                    name = "Sarah",
                    photoUri = null,
                    phoneNumber = null,
                    email = null,
                    notes = listOf(Note(content = "College friend")),
                    events = emptyList(),
                    socialLinks = emptyList(),
                    frequencyDays = 30,
                    lastContactedAt = System.currentTimeMillis(),
                    nextReminderAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)
                )
            ),
            onAddPersonClick = { _ -> },
            onPersonClick = {}
        )
    }
}
