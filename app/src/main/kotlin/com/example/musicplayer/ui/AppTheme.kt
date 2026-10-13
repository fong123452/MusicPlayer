package com.example.musicplayer.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import musicplayer.core.ColorRoles
import musicplayer.core.Theme
import musicplayer.core.ThemeMode

/** The Material 3 scheme for one brightness of an accent; no dynamic color. */
internal fun ColorRoles.toColorScheme(dark: Boolean): ColorScheme {
    val primary = Color(primary)
    val onPrimary = Color(onPrimary)
    val container = Color(primaryContainer)
    val onContainer = Color(onPrimaryContainer)
    val surface = Color(surface)
    val onSurface = Color(onSurface)
    return if (dark) {
        darkColorScheme(
            primary = primary, onPrimary = onPrimary, primaryContainer = container, onPrimaryContainer = onContainer,
            secondary = primary, onSecondary = onPrimary, secondaryContainer = container, onSecondaryContainer = onContainer,
            surface = surface, onSurface = onSurface, background = surface, onBackground = onSurface,
        )
    } else {
        lightColorScheme(
            primary = primary, onPrimary = onPrimary, primaryContainer = container, onPrimaryContainer = onContainer,
            secondary = primary, onSecondary = onPrimary, secondaryContainer = container, onSecondaryContainer = onContainer,
            surface = surface, onSurface = onSurface, background = surface, onBackground = onSurface,
        )
    }
}

/** Applies [theme] to [content]; changing it recolors the content in place. */
@Composable
fun AppTheme(theme: Theme, content: @Composable () -> Unit) {
    val dark = when (theme.mode) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.FollowSystem -> isSystemInDarkTheme()
    }
    val roles = if (dark) theme.accent.dark else theme.accent.light
    MaterialTheme(colorScheme = roles.toColorScheme(dark), content = content)
}
