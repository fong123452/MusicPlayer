package com.example.musicplayer

import androidx.compose.ui.graphics.Color
import com.example.musicplayer.ui.isDark
import com.example.musicplayer.ui.toColorScheme
import musicplayer.core.AccentPalette
import musicplayer.core.ThemeMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppThemeTest {
    @Test
    fun light_and_dark_ignore_the_system_and_follow_system_copies_it() {
        assertFalse(isDark(ThemeMode.Light, systemDark = true))
        assertTrue(isDark(ThemeMode.Dark, systemDark = false))
        assertTrue(isDark(ThemeMode.FollowSystem, systemDark = true))
        assertFalse(isDark(ThemeMode.FollowSystem, systemDark = false))
    }

    @Test
    fun each_accent_scheme_carries_its_own_colors_and_no_baseline_violet_leaks_through() {
        for (accent in AccentPalette) {
            for ((dark, roles) in listOf(false to accent.light, true to accent.dark)) {
                val scheme = roles.toColorScheme(dark)
                val where = "${accent.id} dark=$dark"
                assertEquals(Color(roles.primary), scheme.primary, where)
                assertEquals(Color(roles.onPrimary), scheme.onPrimary, where)
                assertEquals(Color(roles.primaryContainer), scheme.primaryContainer, where)
                assertEquals(Color(roles.onPrimaryContainer), scheme.onPrimaryContainer, where)
                assertEquals(Color(roles.surface), scheme.surface, where)
                assertEquals(Color(roles.onSurface), scheme.onSurface, where)
                assertEquals(Color(roles.surface), scheme.background, where)
                // Roles the palette does not define take the accent's colors rather than Material's violet baseline.
                assertEquals(Color(roles.primary), scheme.tertiary, where)
                assertEquals(Color(roles.primaryContainer), scheme.surfaceVariant, where)
                assertEquals(Color(roles.surface), scheme.surfaceContainer, where)
                assertEquals(Color(roles.surface), scheme.surfaceDim, where)
                assertEquals(Color(roles.onSurface), scheme.outline, where)
                assertEquals(Color(roles.primaryContainer), scheme.outlineVariant, where)
                assertEquals(Color(roles.primary), scheme.surfaceTint, where)
            }
        }
    }
}
