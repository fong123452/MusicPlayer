package com.example.musicplayer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import musicplayer.core.Accent
import musicplayer.core.AccentPalette
import musicplayer.core.Theme
import musicplayer.core.ThemeMode

const val SETTINGS_LABEL = "Settings"
const val THEME_PREVIEW_TAG = "theme_preview"

fun ThemeMode.label(): String = when (this) {
    ThemeMode.Light -> "Light"
    ThemeMode.Dark -> "Dark"
    ThemeMode.FollowSystem -> "Follow system"
}

fun accentDescription(accent: Accent) = "Accent ${accent.label}"

/** Theme controls: mode and accent. Changes apply at once because the app's Theme is read from the same store. */
@Composable
fun SettingsScreen(
    theme: Theme,
    onModeChange: (ThemeMode) -> Unit,
    onAccentChange: (Accent) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onClose)
    Column(modifier.fillMaxSize()) {
        IconButton(onClick = onClose) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = BACK_DESCRIPTION)
        }
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text(SETTINGS_LABEL, style = MaterialTheme.typography.titleLarge)
            Text("Theme", Modifier.padding(top = 16.dp), style = MaterialTheme.typography.titleMedium)
            Box(
                Modifier.fillMaxWidth().padding(vertical = 8.dp).height(16.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .testTag(THEME_PREVIEW_TAG),
            )
            ThemeMode.entries.forEach { mode ->
                Row(
                    Modifier.fillMaxWidth().clickable { onModeChange(mode) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = theme.mode == mode, onClick = { onModeChange(mode) })
                    Text(mode.label())
                }
            }
            Text("Accent color", Modifier.padding(top = 16.dp, bottom = 8.dp), style = MaterialTheme.typography.titleMedium)
            AccentGrid(theme, onAccentChange)
        }
    }
}

@Composable
private fun AccentGrid(theme: Theme, onAccentChange: (Accent) -> Unit) {
    Column {
        AccentPalette.chunked(4).forEach { row ->
            Row {
                row.forEach { accent ->
                    val chosen = accent == theme.accent
                    Box(
                        Modifier.padding(end = 12.dp, bottom = 12.dp).size(48.dp)
                            .background(Color(accent.light.primary), CircleShape)
                            .border(if (chosen) 3.dp else 0.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                            .clickable(role = Role.RadioButton) { onAccentChange(accent) }
                            .semantics {
                                contentDescription = accentDescription(accent)
                                selected = chosen
                            },
                    )
                }
            }
        }
    }
}
