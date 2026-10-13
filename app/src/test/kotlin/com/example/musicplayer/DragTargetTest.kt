package com.example.musicplayer

import com.example.musicplayer.ui.dragTargetIndex
import kotlin.test.Test
import kotlin.test.assertEquals

class DragTargetTest {
    @Test fun `dragging down by 1_4 rows moves one row`() = assertEquals(3, dragTargetIndex(from = 2, offsetPx = 140f, rowHeightPx = 100, lastIndex = 9))

    @Test fun `dragging down by 1_6 rows moves two rows`() = assertEquals(4, dragTargetIndex(2, 160f, 100, 9))

    @Test fun `dragging up moves up`() = assertEquals(0, dragTargetIndex(2, -200f, 100, 9))

    @Test fun `the target is clamped to the queue`() {
        assertEquals(0, dragTargetIndex(1, -900f, 100, 9))
        assertEquals(9, dragTargetIndex(8, 900f, 100, 9))
    }

    @Test fun `a small drag stays put`() = assertEquals(2, dragTargetIndex(2, 40f, 100, 9))
}
