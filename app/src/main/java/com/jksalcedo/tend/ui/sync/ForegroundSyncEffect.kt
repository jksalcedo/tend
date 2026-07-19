package com.jksalcedo.tend.ui.sync

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.jksalcedo.tend.domain.usecase.RefreshLinkedContactsUseCase
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ForegroundSyncEffect() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val refreshLinkedContactsUseCase = koinInject<RefreshLinkedContactsUseCase>()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    scope.launch {
                        // Permission can be revoked between the check above and the use
                        // case's own ContentResolver calls; don't let that (or any other
                        // provider hiccup) crash the app on a routine foreground resume.
                        try {
                            refreshLinkedContactsUseCase()
                        } catch (e: Exception) {
                            Log.w("ForegroundSyncEffect", "Contact refresh failed", e)
                        }
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
