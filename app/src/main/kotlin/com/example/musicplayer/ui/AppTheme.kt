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

/** Whether [mode] shows dark colors when the system itself is [systemDark]. */
fun isDark(mode: ThemeMode, systemDark: Boolean): Boolean = when (mode) {
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
    ThemeMode.FollowSystem -> systemDark
}

/**
 * The Material 3 scheme for one brightness of an accent; no dynamic color. Roles the palette does not define
 * borrow the accent's own colors, so no part of the baseline violet shows under another accent.
 */
internal fun ColorRoles.toColorScheme(dark: Boolean): ColorScheme {
    val accent = Color(primary)
    val onAccent = Color(onPrimary)
    val container = Color(primaryContainer)
    val onContainer = Color(onPrimaryContainer)
    val base = Color(surface)
    val onBase = Color(onSurface)
    val scheme = if (dark) darkColorScheme() else lightColorScheme()
    return scheme.copy(
        primary = accent, onPrimary = onAccent, primaryContainer = container, onPrimaryContainer = onContainer,
        secondary = accent, onSecondary = onAccent, secondaryContainer = container, onSecondaryContainer = onContainer,
        tertiary = accent, onTertiary = onAccent, tertiaryContainer = container, onTertiaryContainer = onContainer,
        surface = base, onSurface = onBase, background = base, onBackground = onBase,
        surfaceVariant = container, onSurfaceVariant = onContainer, outline = onBase, outlineVariant = container,
        surfaceContainerLowest = base, surfaceContainerLow = base, surfaceContainer = base,
        surfaceContainerHigh = base, surfaceContainerHighest = base, surfaceBright = base, surfaceDim = base,
        surfaceTint = accent,
    )
}

/** Applies [theme] to [content]; changing it recolors the content in place. */
@Composable
fun AppTheme(theme: Theme, content: @Composable () -> Unit) {
    val dark = isDark(theme.mode, isSystemInDarkTheme())
    val roles = if (dark) theme.accent.dark else theme.accent.light
    MaterialTheme(colorScheme = roles.toColorScheme(dark), content = content)
}
