package com.example.musicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.musicplayer.permission.AudioPermission
import com.example.musicplayer.ui.AppTheme
import com.example.musicplayer.ui.HomeScreen
import com.example.musicplayer.ui.LibraryViewModel
import com.example.musicplayer.ui.PermissionGate
import com.example.musicplayer.ui.ThemeViewModel
import com.example.musicplayer.ui.isDark
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var audioPermission: AudioPermission

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    private var askedForNotifications = false

    /** Without this on API 33+ the media notification is hidden, though playback still works. */
    private fun requestNotificationPermissionOnce() {
        if (askedForNotifications || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        askedForNotifications = true
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Before the first frame, so the bars are never drawn in the default style while the Theme loads.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val theme by themeViewModel.theme.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val dark = theme?.let { isDark(it.mode, systemDark) } ?: systemDark
            // System bar icons follow the app's Theme, not the system's, or they vanish against the app's colors.
            SideEffect {
                val bars = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark }
                enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
            }
            // Nothing is drawn until the saved Theme is known, so there is no flash of the default colors.
            theme?.let { AppTheme(it) {
                Surface(Modifier.fillMaxSize()) {
                    Box(Modifier.safeDrawingPadding()) {
                        PermissionGate(
                            permission = audioPermission.permission,
                            isGranted = audioPermission::isGranted,
                        ) {
                            val viewModel: LibraryViewModel = hiltViewModel()
                            // Covers the moment the permission is first granted from the gate.
                            LaunchedEffect(Unit) { viewModel.refresh() }
                            HomeScreen(viewModel, onTrackPlayed = ::requestNotificationPermissionOnce)
                        }
                    }
                }
            } }
        }
    }
}
