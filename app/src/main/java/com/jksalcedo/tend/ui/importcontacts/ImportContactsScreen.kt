package com.jksalcedo.tend.ui.importcontacts

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.jksalcedo.tend.domain.model.NativeContact
import com.jksalcedo.tend.ui.theme.TendPastels
import org.koin.androidx.compose.koinViewModel

private enum class ContactsPermissionState {
    UNKNOWN, GRANTED, DENIED, PERMANENTLY_DENIED
}

@Composable
fun ImportContactsScreen(
    viewModel: ImportContactsViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var permissionState by remember {
        mutableStateOf(
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                ContactsPermissionState.GRANTED
            } else {
                ContactsPermissionState.UNKNOWN
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionState = when {
            granted -> ContactsPermissionState.GRANTED
            activity != null && ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.READ_CONTACTS
            ) -> ContactsPermissionState.DENIED

            else -> ContactsPermissionState.PERMANENTLY_DENIED
        }
    }

    LaunchedEffect(Unit) {
        if (permissionState == ContactsPermissionState.GRANTED) {
            viewModel.loadContacts()
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // Only resync once we've already gone through an explicit request in this
            // screen instance — otherwise this fires before the initial request above and
            // can misread "never asked" as permanently denied. Needed because granting via
            // system Settings (not our in-app dialog) never triggers the launcher's own
            // result callback, so this is the only way to notice that.
            if (event == Lifecycle.Event.ON_RESUME && permissionState != ContactsPermissionState.UNKNOWN) {
                permissionState = when {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED -> ContactsPermissionState.GRANTED

                    activity != null && ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.READ_CONTACTS
                    ) -> ContactsPermissionState.DENIED

                    else -> ContactsPermissionState.PERMANENTLY_DENIED
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(permissionState) {
        if (permissionState == ContactsPermissionState.GRANTED) {
            viewModel.loadContacts()
        }
    }

    val contacts by viewModel.contacts.collectAsState()
    val selectedLookupKeys by viewModel.selectedLookupKeys.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    ImportContactsContent(
        permissionState = permissionState,
        contacts = contacts,
        selectedLookupKeys = selectedLookupKeys,
        isLoading = isLoading,
        onToggleSelection = viewModel::toggleSelection,
        onRequestPermission = { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) },
        onOpenSettings = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        },
        onConfirm = { viewModel.confirmImport(onNavigateBack) },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportContactsContent(
    permissionState: ContactsPermissionState,
    contacts: List<NativeContact>,
    selectedLookupKeys: Set<String>,
    isLoading: Boolean,
    onToggleSelection: (String) -> Unit,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onConfirm: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Import Contacts",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (permissionState == ContactsPermissionState.GRANTED) {
                        TextButton(onClick = onConfirm) {
                            Text("Import (${selectedLookupKeys.size})")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            when (permissionState) {
                ContactsPermissionState.UNKNOWN -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                ContactsPermissionState.DENIED -> {
                    PermissionMessage(
                        message = "Contacts access is needed to show contacts you can import. You can try again below.",
                        buttonText = "Grant Access",
                        onClick = onRequestPermission
                    )
                }

                ContactsPermissionState.PERMANENTLY_DENIED -> {
                    PermissionMessage(
                        message = "Contacts access was previously denied. Enable it from system settings to import contacts.",
                        buttonText = "Open Settings",
                        onClick = onOpenSettings
                    )
                }

                ContactsPermissionState.GRANTED -> {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (contacts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No more contacts to import",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(contacts, key = { it.lookupKey }) { contact ->
                                NativeContactRow(
                                    contact = contact,
                                    isSelected = contact.lookupKey in selectedLookupKeys,
                                    onToggle = { onToggleSelection(contact.lookupKey) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionMessage(
    message: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onClick) {
                Text(buttonText)
            }
        }
    }
}

@Composable
private fun NativeContactRow(
    contact: NativeContact,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(TendPastels.Mint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.name.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TendPastels.MintDark
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            val subtitle = contact.phoneNumber ?: contact.email
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
    }
}
