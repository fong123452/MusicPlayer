package com.example.musicplayer.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

/**
 * Asks for the audio permission as soon as it is composed, and shows [content] once granted.
 * While not granted it shows an explanation with a button that asks again.
 */
@Composable
fun PermissionGate(
    permission: String,
    isGranted: () -> Boolean,
    content: @Composable () -> Unit,
) {
    var granted by remember { mutableStateOf(isGranted()) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }

    // The user may also grant it from system settings while the app is in the background.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { granted = isGranted() }
    LaunchedEffect(Unit) { if (!granted) launcher.launch(permission) }

    if (granted) {
        content()
    } else {
        val context = LocalContext.current
        PermissionDeniedScreen(
            onGrant = { launcher.launch(permission) },
            onOpenSettings = {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)),
                )
            },
        )
    }
}

@Composable
fun PermissionDeniedScreen(onGrant: () -> Unit, onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Allow access to your music", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Music Player needs permission to read the audio files on your device. " +
                "Without it the app has nothing to show, so your library stays empty.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(onClick = onGrant) { Text("Grant permission") }
        TextButton(onClick = onOpenSettings) { Text("Open app settings") }
    }
}
