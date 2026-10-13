package musicplayer.core

import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccentPaletteTest {
    // WCAG 2.x relative luminance and contrast ratio.
    private fun luminance(argb: Long): Double {
        fun channel(shift: Int): Double {
            val c = ((argb shr shift) and 0xFF) / 255.0
            return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
    }

    private fun contrast(a: Long, b: Long): Double {
        val (hi, lo) = listOf(luminance(a), luminance(b)).sortedDescending()
        return (hi + 0.05) / (lo + 0.05)
    }

    @Test
    fun the_palette_has_eight_distinct_accents() {
        assertEquals(8, AccentPalette.size)
        assertEquals(8, AccentPalette.map { it.id }.toSet().size)
        assertEquals(8, AccentPalette.map { it.light.primary }.toSet().size)
    }

    @Test
    fun every_accent_meets_4_5_to_1_contrast_in_both_brightnesses() {
        val failures = AccentPalette.flatMap { accent ->
            listOf("light" to accent.light, "dark" to accent.dark).flatMap { (name, roles) ->
                listOf(
                    "primary" to contrast(roles.primary, roles.onPrimary),
                    "primaryContainer" to contrast(roles.primaryContainer, roles.onPrimaryContainer),
                    "surface" to contrast(roles.surface, roles.onSurface),
                ).filter { it.second < 4.5 }.map { "${accent.id} $name ${it.first}: ${it.second}" }
            }
        }
        assertTrue(failures.isEmpty(), failures.joinToString("\n"))
    }
}
