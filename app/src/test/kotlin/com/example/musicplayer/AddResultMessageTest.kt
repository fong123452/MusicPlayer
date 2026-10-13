package com.example.musicplayer

import com.example.musicplayer.ui.describe
import com.example.musicplayer.ui.trackCountLabel
import musicplayer.core.AddResult
import musicplayer.core.Track
import kotlin.test.Test
import kotlin.test.assertEquals

class AddResultMessageTest {
    private fun track(id: Long) = Track(id, "content://media/audio/$id", "T$id", "A", "B", null, null, null, null, 180_000)

    @Test
    fun `adding one track that is already there says so`() {
        assertEquals("Already in Mix", AddResult(emptyList(), listOf(track(1))).describe("Mix"))
    }

    @Test
    fun `adding several tracks that are all already there says how many`() {
        assertEquals("All 2 songs already in Mix", AddResult(emptyList(), listOf(track(1), track(2))).describe("Mix"))
    }

    @Test
    fun `adding only new tracks confirms the add`() {
        assertEquals("Added to Mix", AddResult(listOf(track(1)), emptyList()).describe("Mix"))
    }

    @Test
    fun `adding a mix of new and existing tracks reports both counts`() {
        assertEquals("Added 1 to Mix, 2 already there", AddResult(listOf(track(1)), listOf(track(2), track(3))).describe("Mix"))
    }

    @Test
    fun `the track count is singular for one song`() {
        assertEquals("1 song", trackCountLabel(1))
        assertEquals("0 songs", trackCountLabel(0))
        assertEquals("3 songs", trackCountLabel(3))
    }
}
