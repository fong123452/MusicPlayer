package com.example.musicplayer.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

private fun glyph(name: String, build: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        .apply { path(fill = SolidColor(Color.Black), pathBuilder = build) }
        .build()

/** Material "skip next" glyph; drawn here because it is not in material-icons-core. */
internal val SkipNextIcon: ImageVector = glyph("SkipNext") {
    moveTo(6f, 18f)
    lineToRelative(8.5f, -6f)
    lineTo(6f, 6f)
    verticalLineToRelative(12f)
    close()
    moveTo(16f, 6f)
    verticalLineToRelative(12f)
    horizontalLineToRelative(2f)
    verticalLineTo(6f)
    horizontalLineToRelative(-2f)
    close()
}

/** Material "skip previous" glyph. */
internal val SkipPreviousIcon: ImageVector = glyph("SkipPrevious") {
    moveTo(6f, 6f)
    horizontalLineToRelative(2f)
    verticalLineToRelative(12f)
    horizontalLineTo(6f)
    close()
    moveTo(9.5f, 12f)
    lineToRelative(8.5f, 6f)
    verticalLineTo(6f)
    close()
}
