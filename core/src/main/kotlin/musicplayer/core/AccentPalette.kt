package musicplayer.core

/** The colors one accent defines for one brightness, as 0xAARRGGBB. */
data class ColorRoles(
    val primary: Long,
    val onPrimary: Long,
    val primaryContainer: Long,
    val onPrimaryContainer: Long,
    val surface: Long,
    val onSurface: Long,
)

/** One selectable accent with its hand-tuned Material 3 light and dark schemes. */
data class Accent(val id: String, val label: String, val light: ColorRoles, val dark: ColorRoles)

private fun accent(
    id: String,
    label: String,
    light: List<Long>,
    dark: List<Long>,
) = Accent(id, label, roles(light), roles(dark))

private fun roles(c: List<Long>) = ColorRoles(c[0], c[1], c[2], c[3], c[4], c[5])

// Each list: primary, onPrimary, primaryContainer, onPrimaryContainer, surface, onSurface.
val AccentPalette: List<Accent> = listOf(
    accent("violet", "Violet",
        listOf(0xFF6750A4, 0xFFFFFFFF, 0xFFEADDFF, 0xFF21005D, 0xFFFEF7FF, 0xFF1D1B20),
        listOf(0xFFD0BCFF, 0xFF381E72, 0xFF4F378B, 0xFFEADDFF, 0xFF141218, 0xFFE6E0E9)),
    accent("blue", "Blue",
        listOf(0xFF0B57D0, 0xFFFFFFFF, 0xFFD3E3FD, 0xFF041E49, 0xFFF8F9FF, 0xFF191C20),
        listOf(0xFFA8C7FA, 0xFF062E6F, 0xFF0842A0, 0xFFD3E3FD, 0xFF111318, 0xFFE2E2E9)),
    accent("teal", "Teal",
        listOf(0xFF006A6A, 0xFFFFFFFF, 0xFF6FF7F6, 0xFF002020, 0xFFF4FAFA, 0xFF161D1D),
        listOf(0xFF4CDADA, 0xFF003737, 0xFF004F4F, 0xFF6FF7F6, 0xFF0E1415, 0xFFDDE4E3)),
    accent("green", "Green",
        listOf(0xFF386A20, 0xFFFFFFFF, 0xFFB7F397, 0xFF042100, 0xFFF8FAF0, 0xFF191D16),
        listOf(0xFF9CD67D, 0xFF0B3900, 0xFF205107, 0xFFB7F397, 0xFF11140F, 0xFFE1E4D9)),
    accent("amber", "Amber",
        listOf(0xFF8B5000, 0xFFFFFFFF, 0xFFFFDCBE, 0xFF2C1600, 0xFFFFF8F4, 0xFF201B16),
        listOf(0xFFFFB870, 0xFF4A2800, 0xFF693C00, 0xFFFFDCBE, 0xFF18120C, 0xFFEBE0D8)),
    accent("red", "Red",
        listOf(0xFFBA1A1A, 0xFFFFFFFF, 0xFFFFDAD6, 0xFF410002, 0xFFFFF8F7, 0xFF231918),
        listOf(0xFFFFB4AB, 0xFF690005, 0xFF93000A, 0xFFFFDAD6, 0xFF1A1110, 0xFFF1DFDC)),
    accent("pink", "Pink",
        listOf(0xFF984061, 0xFFFFFFFF, 0xFFFFD9E2, 0xFF3E001D, 0xFFFFF8F8, 0xFF22191B),
        listOf(0xFFFFB1C8, 0xFF5E1133, 0xFF7B2949, 0xFFFFD9E2, 0xFF191113, 0xFFF0DEE1)),
    accent("cyan", "Cyan",
        listOf(0xFF00677F, 0xFFFFFFFF, 0xFFB7EAFF, 0xFF001F28, 0xFFF5FAFC, 0xFF171C1E),
        listOf(0xFF5FD4FD, 0xFF003544, 0xFF004D61, 0xFFB7EAFF, 0xFF0E1415, 0xFFDDE3E6)),
)
