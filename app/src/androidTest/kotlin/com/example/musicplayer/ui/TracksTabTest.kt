package com.example.musicplayer.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.core.app.ActivityScenario
import com.example.musicplayer.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import musicplayer.core.FakeTrackSource
import musicplayer.core.rawTrack
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class TracksTabTest {
    @get:Rule(order = 0) val hilt = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose = createEmptyComposeRule()

    @Inject lateinit var source: FakeTrackSource
    private var scenario: ActivityScenario<MainActivity>? = null

    @Before fun setUp() = hilt.inject()
    @After fun tearDown() { scenario?.close() }

    private fun launch() { scenario = ActivityScenario.launch(MainActivity::class.java) }

    private fun ComposeTestRule.waitForText(text: String) =
        waitUntil(5_000) { onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }

    @Test
    fun tracks_are_listed_by_title_ignoring_case() {
        source.tracks = listOf(
            rawTrack(1, title = "banana"),
            rawTrack(2, title = "Cherry"),
            rawTrack(3, title = "apple"),
        )

        launch()

        compose.waitForText("apple")
        val list = compose.onNodeWithTag(TRACKS_LIST_TAG)
        list.onChildAt(0).assertTextContains("apple")
        list.onChildAt(1).assertTextContains("banana")
        list.onChildAt(2).assertTextContains("Cherry")
    }

    @Test
    fun an_empty_library_shows_the_empty_message() {
        source.tracks = emptyList()

        launch()

        compose.waitForText(EMPTY_LIBRARY_MESSAGE)
        compose.onNodeWithText(EMPTY_LIBRARY_MESSAGE).assertExists()
    }

    @Test
    fun pulling_down_refreshes_the_library() {
        source.tracks = listOf(rawTrack(1, title = "Old"))
        launch()
        compose.waitForText("Old")

        source.tracks = listOf(rawTrack(1, title = "Old"), rawTrack(2, title = "Newly added"))
        compose.onNodeWithTag(TRACKS_LIST_TAG).performTouchInput { swipeDown() }

        compose.waitForText("Newly added")
    }

    @Test
    fun home_has_only_songs_and_playlists_tabs() {
        launch()

        compose.onAllNodes(isSelectable()).assertCountEquals(2)
        compose.onNodeWithText("Songs").assertExists()
        compose.onNodeWithText("Playlists").assertExists()
        compose.onNodeWithText("Albums").assertDoesNotExist()
        compose.onNodeWithText("Artists").assertDoesNotExist()
    }

    @Test
    fun tab_labels_fit_on_one_line() {
        launch()

        // Two single-line labels share a height; a wrapped one would be taller than the other.
        val songs = compose.onNodeWithText("Songs").fetchSemanticsNode().size.height
        val playlists = compose.onNodeWithText("Playlists").fetchSemanticsNode().size.height
        assertEquals(songs, playlists)
    }
}
