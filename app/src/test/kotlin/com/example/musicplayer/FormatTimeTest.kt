package com.example.musicplayer

import com.example.musicplayer.ui.formatTime
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatTimeTest {
    @Test
    fun `formats minutes and zero-padded seconds`() {
        assertEquals("0:00", formatTime(0))
        assertEquals("0:59", formatTime(59_999))
        assertEquals("3:05", formatTime(185_000))
        assertEquals("60:00", formatTime(3_600_000))
    }
}
