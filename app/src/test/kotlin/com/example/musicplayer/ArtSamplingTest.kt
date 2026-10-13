package com.example.musicplayer

import com.example.musicplayer.ui.artSampleSize
import kotlin.test.Test
import kotlin.test.assertEquals

class ArtSamplingTest {
    @Test
    fun `art already at or under the target size is not downsampled`() {
        assertEquals(1, artSampleSize(width = 500, height = 400, targetPx = 512))
    }

    @Test
    fun `a large cover is halved until its short side is no smaller than the target`() {
        assertEquals(4, artSampleSize(width = 3000, height = 3000, targetPx = 512))
    }

    @Test
    fun `a wide cover is sampled by its short side so it still fills the target`() {
        assertEquals(2, artSampleSize(width = 4000, height = 1100, targetPx = 512))
    }

    @Test
    fun `unknown dimensions are not downsampled`() {
        assertEquals(1, artSampleSize(width = 0, height = 0, targetPx = 512))
    }

    @Test
    fun `a cover that halves to exactly the target size is halved`() {
        assertEquals(2, artSampleSize(width = 1024, height = 1024, targetPx = 512))
    }
}
