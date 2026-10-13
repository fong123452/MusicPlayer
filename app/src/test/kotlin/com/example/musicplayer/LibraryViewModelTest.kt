package com.example.musicplayer

import com.example.musicplayer.ui.LibraryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import musicplayer.core.FakeTrackSource
import musicplayer.core.Library
import musicplayer.core.rawTrack
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {
    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `a failed pull to refresh keeps the Library, stops the spinner and does not crash`() {
        val source = FakeTrackSource(listOf(rawTrack(1)))
        val viewModel = LibraryViewModel(Library(source))
        viewModel.refresh()

        source.failure = SecurityException("revoked")
        viewModel.refresh()

        assertEquals(2, source.loadCount)
        assertEquals(false, viewModel.isRefreshing.value)
        assertEquals(1, viewModel.snapshot.value.tracks.size)
    }
}
