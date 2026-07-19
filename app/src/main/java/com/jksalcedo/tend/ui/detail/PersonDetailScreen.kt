package com.jksalcedo.tend.ui.detail

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.jksalcedo.tend.R
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.jksalcedo.tend.domain.model.Note
import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.model.PersonEvent
import com.jksalcedo.tend.ui.add.ShareScanSheet
import com.jksalcedo.tend.ui.theme.TendPastels
import com.jksalcedo.tend.utils.SocialIconUtils
import com.jksalcedo.tend.utils.SocialLinkUtils
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

private enum class SyncToDeviceMessage { DENIED, PERMANENTLY_DENIED }
private enum class ReadContactsMessage { DENIED, PERMANENTLY_DENIED }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun PersonDetailScreen(
    viewModel: PersonDetailViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onEditClick: (Long) -> Unit = {},
    onNavigateToPerson: (Long) -> Unit = {}
) {
    val person by viewModel.person.collectAsState()
    val duplicates by viewModel.duplicates.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncFailed by viewModel.syncFailed.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    var showShareSheet by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }
    var showTagPicker by remember { mutableStateOf(false) }
    var tagToDelete by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(syncFailed) {
        if (syncFailed) {
            Toast.makeText(
                context,
                "Couldn't sync this connection to your device. Please try again.",
                Toast.LENGTH_SHORT
            ).show()
            viewModel.consumeSyncFailed()
        }
    }

    var hasContactsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var readContactsMessage by remember { mutableStateOf<ReadContactsMessage?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasContactsPermission = granted
        readContactsMessage = if (granted) {
            null
        } else if (activity != null && ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.READ_CONTACTS
            )
        ) {
            ReadContactsMessage.DENIED
        } else {
            ReadContactsMessage.PERMANENTLY_DENIED
        }
    }

    var syncToDeviceMessage by remember { mutableStateOf<SyncToDeviceMessage?>(null) }
    // Creating a native contact needs WRITE_CONTACTS, but NativeContactsDataSource.createContact()
    // also queries the provider afterward to resolve the new contact's id/lookup key — and
    // ContactsProvider requires READ_CONTACTS for any query regardless of WRITE_CONTACTS. Request
    // both together so a user who never separately granted READ_CONTACTS (e.g. never used
    // Import Contacts) doesn't hit a SecurityException on their first sync.
    val writeContactsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val writeGranted = results[Manifest.permission.WRITE_CONTACTS] == true
        val readGranted = results[Manifest.permission.READ_CONTACTS] == true
        hasContactsPermission = readGranted
        if (readGranted) readContactsMessage = null
        if (writeGranted && readGranted) {
            syncToDeviceMessage = null
            viewModel.syncToDevice()
        } else {
            val stillNeedsRationale = activity != null && (
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.WRITE_CONTACTS
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.READ_CONTACTS
                )
            )
            syncToDeviceMessage = if (stillNeedsRationale) {
                SyncToDeviceMessage.DENIED
            } else {
                SyncToDeviceMessage.PERMANENTLY_DENIED
            }
        }
    }
    val onSyncToDevice: () -> Unit = {
        val writeGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        val readGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        if (writeGranted && readGranted) {
            syncToDeviceMessage = null
            viewModel.syncToDevice()
        } else {
            // Android itself skips the dialog for whichever permission was permanently denied —
            // launching is always safe, the callback above disambiguates which case it was.
            writeContactsPermissionLauncher.launch(
                arrayOf(Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS)
            )
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasContactsPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED
                if (hasContactsPermission) {
                    readContactsMessage = null
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.person_detail_delete_dialog_title)) },
            text = { Text(stringResource(R.string.person_detail_delete_dialog_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.delete(onComplete = onNavigateBack)
                    }
                ) {
                    Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (showArchiveDialog) {
        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            title = { Text(stringResource(R.string.person_detail_archive_dialog_title)) },
            text = { Text(stringResource(R.string.person_detail_archive_dialog_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showArchiveDialog = false
                        viewModel.archive(onComplete = onNavigateBack)
                    }
                ) {
                    Text(stringResource(R.string.common_archive))
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (showTagPicker) {
        person?.let { p ->
            TagPickerDialog(
                allTags = allTags,
                personTags = p.tags,
                onToggle = { tag ->
                    if (tag in p.tags) viewModel.removeTag(tag) else viewModel.addTag(tag)
                },
                onCreate = { tag -> viewModel.addTag(tag) },
                onLongPressTag = { tag -> tagToDelete = tag },
                onDismiss = { showTagPicker = false }
            )
        }
    }

    if (tagToDelete != null) {
        val tag = tagToDelete!!
        AlertDialog(
            onDismissRequest = { tagToDelete = null },
            title = { Text("Delete tag \"$tag\"?") },
            text = { Text("This removes \"$tag\" from every connection that has it and takes it out of the tag list everywhere — this can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTag(tag)
                        tagToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { tagToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    var noteToEdit by remember { mutableStateOf<Note?>(null) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }

    if (noteToEdit != null) {
        var editContent by remember { mutableStateOf(noteToEdit!!.content) }
        AlertDialog(
            onDismissRequest = { noteToEdit = null },
            title = { Text(stringResource(R.string.person_detail_edit_note_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = editContent,
                    onValueChange = { editContent = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateNote(noteToEdit!!.id, editContent)
                        noteToEdit = null
                    },
                    enabled = editContent.isNotBlank()
                ) {
                    Text(stringResource(R.string.common_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToEdit = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text(stringResource(R.string.person_detail_delete_note_dialog_title)) },
            text = { Text(stringResource(R.string.person_detail_delete_note_dialog_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteNote(noteToDelete!!.id)
                        noteToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    person?.let { p ->
                        IconButton(onClick = { onEditClick(p.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.person_detail_edit_content_description))
                        }
                        IconButton(onClick = { showShareSheet = true }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.person_detail_share_content_description))
                        }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.common_more_options))
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                if (p.isArchived) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.person_detail_unarchive_connection)) },
                                        onClick = {
                                            showMenu = false
                                            viewModel.unarchive(onComplete = onNavigateBack)
                                        }
                                    )
                                } else {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.person_detail_archive_connection)) },
                                        onClick = {
                                            showMenu = false
                                            showArchiveDialog = true
                                        }
                                    )
                                }
                                if (p.isDeviceLinkBroken) {
                                    DropdownMenuItem(
                                        text = { Text("Unlink from device contact") },
                                        onClick = {
                                            showMenu = false
                                            viewModel.unlink()
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            stringResource(R.string.person_detail_delete_connection),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (showShareSheet) {
            person?.let { p ->
                ShareScanSheet(
                    person = p,
                    onDismissRequest = { showShareSheet = false }
                )
            }
        }
        person?.let { p ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                // Header
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val cachedPhoto = remember(p.localPhotoPath) {
                        p.localPhotoPath?.let { path ->
                            File(path).takeIf { it.exists() }
                                ?.let { BitmapFactory.decodeFile(it.absolutePath) }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (cachedPhoto != null) {
                            Image(
                                bitmap = cachedPhoto.asImageBitmap(),
                                contentDescription = "${p.name}'s photo",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = p.name.take(1).uppercase(),
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = p.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (p.reminderWindowDays > 0) {
                            stringResource(R.string.person_detail_check_in_frequency, p.frequencyDays) + " (±${p.reminderWindowDays}d float)"
                        } else {
                            stringResource(R.string.person_detail_check_in_frequency, p.frequencyDays)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (p.lastContactedAt > 0) {
                        val daysAgo = java.util.concurrent.TimeUnit.MILLISECONDS
                            .toDays(System.currentTimeMillis() - p.lastContactedAt)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (daysAgo) {
                                0L -> stringResource(R.string.person_detail_last_contacted_today)
                                1L -> stringResource(R.string.person_detail_last_contacted_yesterday)
                                else -> stringResource(R.string.person_detail_last_contacted_days_ago, daysAgo)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val onOpenSettings: () -> Unit = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }

                DeviceSyncStatusSection(
                    person = p,
                    duplicates = duplicates,
                    hasContactsPermission = hasContactsPermission,
                    readContactsMessage = readContactsMessage,
                    isSyncing = isSyncing,
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) },
                    onEditInContacts = {
                        val lookupKey = p.nativeLookupKey
                        val contactId = p.nativeContactId
                        if (lookupKey != null && contactId != null) {
                            val lookupUri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey)
                            context.startActivity(Intent(Intent.ACTION_EDIT).apply { data = lookupUri })
                        }
                    },
                    onNavigateToDuplicate = { duplicates.firstOrNull()?.let { onNavigateToPerson(it.id) } },
                    onUnlink = { viewModel.unlink() },
                    onSyncToDevice = onSyncToDevice,
                    syncToDeviceMessage = syncToDeviceMessage,
                    onOpenSettings = onOpenSettings
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tags
                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    p.tags.forEach { tag ->
                        InputChip(
                            selected = false,
                            onClick = {},
                            label = { Text(tag) },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove tag $tag",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { viewModel.removeTag(tag) }
                                )
                            }
                        )
                    }
                    AssistChip(
                        onClick = { showTagPicker = true },
                        label = { Text("+ Add tag") }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Contact Methods
                Text(
                    text = stringResource(R.string.person_detail_ways_to_connect),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (p.phoneNumber != null) {
                    ContactActionItem(
                        icon = Icons.Default.Call,
                        label = stringResource(R.string.person_detail_call_label),
                        value = p.phoneNumber,
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = "tel:${p.phoneNumber}".toUri()
                            }
                            context.startActivity(intent)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (p.email != null) {
                    ContactActionItem(
                        icon = Icons.Default.Email,
                        label = stringResource(R.string.person_detail_email_label),
                        value = p.email,
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:${p.email}".toUri()
                            }
                            context.startActivity(intent)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                p.socialLinks.forEach { link ->
                    ContactActionItem(
                        icon = SocialIconUtils.getIcon(link.platform),
                        label = link.platform,
                        value = link.handle,
                        onClick = {
                            SocialLinkUtils.openProfile(context, link.platform, link.handle)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Important Dates
                if (p.events.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.common_important_dates),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    p.events.forEach { event ->
                        EventItem(event)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Text(
                    text = stringResource(R.string.person_detail_history_notes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                var noteInput by remember { mutableStateOf("") }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        placeholder = { Text(stringResource(R.string.person_detail_add_note_placeholder)) },
                        modifier = Modifier.weight(1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.3f
                            )
                        ),
                        maxLines = 3
                    )
                    IconButton(
                        onClick = {
                            viewModel.addNote(noteInput)
                            noteInput = ""
                        },
                        enabled = noteInput.isNotBlank()
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.person_detail_save_note_content_description),
                            tint = if (noteInput.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (p.notes.isEmpty()) {
                    Text(
                        text = stringResource(R.string.person_detail_no_notes_yet),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                } else {
                    p.notes.reversed().forEach { note ->
                        NoteItem(
                            note = note,
                            onEditClick = { noteToEdit = note },
                            onDeleteClick = { noteToDelete = note }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.checkIn() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.person_detail_mark_as_contacted),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun DeviceSyncStatusSection(
    person: Person,
    duplicates: List<Person>,
    hasContactsPermission: Boolean,
    readContactsMessage: ReadContactsMessage?,
    isSyncing: Boolean,
    onRequestPermission: () -> Unit,
    onEditInContacts: () -> Unit,
    onNavigateToDuplicate: () -> Unit,
    onUnlink: () -> Unit,
    onSyncToDevice: () -> Unit,
    syncToDeviceMessage: SyncToDeviceMessage?,
    onOpenSettings: () -> Unit
) {
    val isLinked = person.nativeLookupKey != null

    if (duplicates.isNotEmpty()) {
        val bannerText = if (duplicates.size == 1) {
            "Possibly a duplicate of \"${duplicates.first().name}\" — tap to review"
        } else {
            "Possibly a duplicate of \"${duplicates.first().name}\" and ${duplicates.size - 1} " +
                "other${if (duplicates.size > 2) "s" else ""} — tap to review"
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNavigateToDuplicate),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = TendPastels.Yellow)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = TendPastels.YellowDark)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = bannerText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TendPastels.YellowDark
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    if (!isLinked) {
        Column {
            StatusCard(
                text = "Not synced to your device contacts",
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onSyncToDevice, enabled = !isSyncing) {
                Text(if (isSyncing) "Syncing…" else "Sync to Device")
            }
            Text(
                text = "Creates a local contact on this device only — it won't back up to your Google account.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            when (syncToDeviceMessage) {
                SyncToDeviceMessage.DENIED -> Text(
                    text = "Contacts access is needed to sync this connection to your device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                SyncToDeviceMessage.PERMANENTLY_DENIED -> Column {
                    Text(
                        text = "Contacts access was previously denied. Enable it from system settings to sync this connection.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    TextButton(onClick = onOpenSettings) { Text("Open Settings") }
                }
                null -> {}
            }
        }
        return
    }

    if (person.isDeviceLinkBroken) {
        Column {
            StatusCard(
                text = "This device contact was removed or is no longer found",
                containerColor = TendPastels.Pink,
                contentColor = TendPastels.PinkDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onUnlink) { Text("Unlink") }
            }
        }
        return
    }

    if (!hasContactsPermission) {
        Column {
            StatusCard(
                text = "Sync paused — contacts permission needed",
                containerColor = TendPastels.Yellow,
                contentColor = TendPastels.YellowDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (readContactsMessage == ReadContactsMessage.PERMANENTLY_DENIED) {
                Text(
                    text = "Contacts access was previously denied. Enable it from system settings to resume syncing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (readContactsMessage == ReadContactsMessage.PERMANENTLY_DENIED) {
                    TextButton(onClick = onOpenSettings) { Text("Open Settings") }
                } else {
                    TextButton(onClick = onRequestPermission) { Text("Grant permission") }
                }
                TextButton(onClick = onEditInContacts) { Text("Edit in Contacts") }
            }
        }
        return
    }

    Column {
        StatusCard(
            text = "Managed by your device contacts",
            containerColor = TendPastels.Mint,
            contentColor = TendPastels.MintDark
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onEditInContacts) { Text("Edit in Contacts") }
    }
}

@Composable
private fun StatusCard(
    text: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor
        )
    }
}

@Composable
fun ContactActionItem(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun EventItem(event: PersonEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // Lighter background
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = event.label,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (event.leadTimeDays > 0) {
                        Text(
                            text = "Remind ${event.leadTimeDays}d early",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Text(
                text = SimpleDateFormat("MMM dd", LocalLocale.current.platformLocale).format(
                    Date(
                        event.date
                    )
                ),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun TagPickerDialog(
    allTags: List<String>,
    personTags: List<String>,
    onToggle: (String) -> Unit,
    onCreate: (String) -> Unit,
    onLongPressTag: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newTag by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tags") },
        text = {
            Column {
                if (allTags.isNotEmpty()) {
                    Text(
                        text = "Tap to add or remove, hold to delete a tag everywhere",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allTags.forEach { tag ->
                            val isSelected = tag in personTags
                            // A single combinedClickable rather than FilterChip's own onClick
                            // plus a modifier on top — stacking two separate click handlers
                            // on one component risks double-registered gestures.
                            androidx.compose.material3.Surface(
                                modifier = Modifier.combinedClickable(
                                    onClick = { onToggle(tag) },
                                    onLongClick = { onLongPressTag(tag) }
                                ),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                OutlinedTextField(
                    value = newTag,
                    onValueChange = { newTag = it },
                    label = { Text("New tag") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCreate(newTag)
                    newTag = ""
                },
                enabled = newTag.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
fun NoteItem(
    note: Note,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", LocalLocale.current.platformLocale).format(
                        Date(note.createdAt)
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                var showNoteMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(
                        onClick = { showNoteMenu = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.person_detail_note_options_content_description),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = showNoteMenu,
                        onDismissRequest = { showNoteMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.person_detail_edit_note)) },
                            onClick = {
                                showNoteMenu = false
                                onEditClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.person_detail_delete_note), color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showNoteMenu = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
