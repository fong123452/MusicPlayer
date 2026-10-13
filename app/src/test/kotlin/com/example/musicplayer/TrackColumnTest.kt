package com.example.musicplayer

import com.example.musicplayer.data.DiscAndTrack
import com.example.musicplayer.data.unpackTrackColumn
import kotlin.test.Test
import kotlin.test.assertEquals

class TrackColumnTest {
    @Test
    fun `a packed value splits into disc in the thousands and track`() {
        assertEquals(DiscAndTrack(disc = 1, track = 5), unpackTrackColumn(1005))
        assertEquals(DiscAndTrack(disc = 2, track = 12), unpackTrackColumn(2012))
    }

    @Test
    fun `a plain track number has no disc`() {
        assertEquals(DiscAndTrack(disc = null, track = 5), unpackTrackColumn(5))
    }

    @Test
    fun `a missing value has neither disc nor track`() {
        assertEquals(DiscAndTrack(disc = null, track = null), unpackTrackColumn(null))
    }
}
