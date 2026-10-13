package com.example.musicplayer

import com.example.musicplayer.data.MediaStoreQuery
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MediaStoreQueryTest {
    @Test
    fun `only music of at least 30 seconds is selected`() {
        assertEquals("is_music = 1 AND duration >= ?", MediaStoreQuery.SELECTION)
        assertEquals(listOf("30000"), MediaStoreQuery.SELECTION_ARGS.toList())
    }

    @Test
    fun `columns that do not exist before API 30 are not requested there`() {
        val old = MediaStoreQuery.projection(29).toList()
        val new = MediaStoreQuery.projection(30).toList()

        assertFalse("album_artist" in old)
        assertFalse("disc_number" in old)
        assertTrue("album_artist" in new)
        assertTrue("disc_number" in new)
    }
}
