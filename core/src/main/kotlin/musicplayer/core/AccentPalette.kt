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

val AccentPalette: List<Accent> = listOf(
    Accent(
        id = "violet",
        label = "Violet",
        light = ColorRoles(
            primary = 0xFF6750A4,
            onPrimary = 0xFFFFFFFF,
            primaryContainer = 0xFFEADDFF,
            onPrimaryContainer = 0xFF21005D,
            surface = 0xFFFEF7FF,
            onSurface = 0xFF1D1B20,
        ),
        dark = ColorRoles(
            primary = 0xFFD0BCFF,
            onPrimary = 0xFF381E72,
            primaryContainer = 0xFF4F378B,
            onPrimaryContainer = 0xFFEADDFF,
            surface = 0xFF141218,
            onSurface = 0xFFE6E0E9,
        ),
    ),
    Accent(
        id = "blue",
        label = "Blue",
        light = ColorRoles(
            primary = 0xFF0B57D0,
            onPrimary = 0xFFFFFFFF,
            primaryContainer = 0xFFD3E3FD,
            onPrimaryContainer = 0xFF041E49,
            surface = 0xFFF8F9FF,
            onSurface = 0xFF191C20,
        ),
        dark = ColorRoles(
            primary = 0xFFA8C7FA,
            onPrimary = 0xFF062E6F,
            primaryContainer = 0xFF0842A0,
            onPrimaryContainer = 0xFFD3E3FD,
            surface = 0xFF111318,
            onSurface = 0xFFE2E2E9,
        ),
    ),
    Accent(
        id = "teal",
        label = "Teal",
        light = ColorRoles(
            primary = 0xFF006A6A,
            onPrimary = 0xFFFFFFFF,
            primaryContainer = 0xFF6FF7F6,
            onPrimaryContainer = 0xFF002020,
            surface = 0xFFF4FAFA,
            onSurface = 0xFF161D1D,
        ),
        dark = ColorRoles(
            primary = 0xFF4CDADA,
            onPrimary = 0xFF003737,
            primaryContainer = 0xFF004F4F,
            onPrimaryContainer = 0xFF6FF7F6,
            surface = 0xFF0E1415,
            onSurface = 0xFFDDE4E3,
        ),
    ),
    Accent(
        id = "green",
        label = "Green",
        light = ColorRoles(
            primary = 0xFF386A20,
            onPrimary = 0xFFFFFFFF,
            primaryContainer = 0xFFB7F397,
            onPrimaryContainer = 0xFF042100,
            surface = 0xFFF8FAF0,
            onSurface = 0xFF191D16,
        ),
        dark = ColorRoles(
            primary = 0xFF9CD67D,
            onPrimary = 0xFF0B3900,
            primaryContainer = 0xFF205107,
            onPrimaryContainer = 0xFFB7F397,
            surface = 0xFF11140F,
            onSurface = 0xFFE1E4D9,
        ),
    ),
    Accent(
        id = "amber",
        label = "Amber",
        light = ColorRoles(
            primary = 0xFF8B5000,
            onPrimary = 0xFFFFFFFF,
            primaryContainer = 0xFFFFDCBE,
            onPrimaryContainer = 0xFF2C1600,
            surface = 0xFFFFF8F4,
            onSurface = 0xFF201B16,
        ),
        dark = ColorRoles(
            primary = 0xFFFFB870,
            onPrimary = 0xFF4A2800,
            primaryContainer = 0xFF693C00,
            onPrimaryContainer = 0xFFFFDCBE,
            surface = 0xFF18120C,
            onSurface = 0xFFEBE0D8,
        ),
    ),
    Accent(
        id = "red",
        label = "Red",
        light = ColorRoles(
            primary = 0xFFBA1A1A,
            onPrimary = 0xFFFFFFFF,
            primaryContainer = 0xFFFFDAD6,
            onPrimaryContainer = 0xFF410002,
            surface = 0xFFFFF8F7,
            onSurface = 0xFF231918,
        ),
        dark = ColorRoles(
            primary = 0xFFFFB4AB,
            onPrimary = 0xFF690005,
            primaryContainer = 0xFF93000A,
            onPrimaryContainer = 0xFFFFDAD6,
            surface = 0xFF1A1110,
            onSurface = 0xFFF1DFDC,
        ),
    ),
    Accent(
        id = "pink",
        label = "Pink",
        light = ColorRoles(
            primary = 0xFF984061,
            onPrimary = 0xFFFFFFFF,
            primaryContainer = 0xFFFFD9E2,
            onPrimaryContainer = 0xFF3E001D,
            surface = 0xFFFFF8F8,
            onSurface = 0xFF22191B,
        ),
        dark = ColorRoles(
            primary = 0xFFFFB1C8,
            onPrimary = 0xFF5E1133,
            primaryContainer = 0xFF7B2949,
            onPrimaryContainer = 0xFFFFD9E2,
            surface = 0xFF191113,
            onSurface = 0xFFF0DEE1,
        ),
    ),
    Accent(
        id = "cyan",
        label = "Cyan",
        light = ColorRoles(
            primary = 0xFF00677F,
            onPrimary = 0xFFFFFFFF,
            primaryContainer = 0xFFB7EAFF,
            onPrimaryContainer = 0xFF001F28,
            surface = 0xFFF5FAFC,
            onSurface = 0xFF171C1E,
        ),
        dark = ColorRoles(
            primary = 0xFF5FD4FD,
            onPrimary = 0xFF003544,
            primaryContainer = 0xFF004D61,
            onPrimaryContainer = 0xFFB7EAFF,
            surface = 0xFF0E1415,
            onSurface = 0xFFDDE3E6,
        ),
    ),
)
