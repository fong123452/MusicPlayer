package com.example.musicplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.musicplayer.permission.AudioPermission
import com.example.musicplayer.ui.HomeScreen
import com.example.musicplayer.ui.LibraryViewModel
import com.example.musicplayer.ui.PermissionGate
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var audioPermission: AudioPermission

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    Box(Modifier.safeDrawingPadding()) {
                        PermissionGate(
                            permission = audioPermission.permission,
                            isGranted = audioPermission::isGranted,
                        ) {
                            val viewModel: LibraryViewModel = hiltViewModel()
                            // Covers the moment the permission is first granted from the gate.
                            LaunchedEffect(Unit) { viewModel.refresh() }
                            HomeScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}
