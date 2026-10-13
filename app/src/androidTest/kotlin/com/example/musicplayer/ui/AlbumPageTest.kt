package com.example.musicplayer.ui

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import com.example.musicplayer.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import musicplayer.core.FakePlaybackEngine
import musicplayer.core.FakeTrackSource
import musicplayer.core.rawTrack
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class AlbumPageTest {
    @get:Rule(order = 0) val hilt = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose = createEmptyComposeRule()

    @Inject lateinit var source: FakeTrackSource
    @Inject lateinit var engine: FakePlaybackEngine
    private var scenario: ActivityScenario<MainActivity>? = null

    @Before fun setUp() {
        hilt.inject()
        source.tracks = listOf(
            rawTrack(1, title = "Second Song", album = "Blue Record", albumArtist = "The Band", trackNumber = 2),
            rawTrack(2, title = "First Song", album = "Blue Record", albumArtist = "The Band", trackNumber = 1),
            rawTrack(3, title = "Lonely", album = "Red Record", albumArtist = "Solo"),
        )
    }

    @After fun tearDown() { scenario?.close() }

    private fun launchOnAlbumResults() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        compose.waitForText("Lonely")
        compose.onNodeWithContentDescription(SEARCH_DESCRIPTION).performClick()
        compose.onNodeWithTag(SEARCH_FIELD_TAG).performTextInput("Record")
    }

    private fun ComposeTestRule.waitForText(text: String) =
        waitUntil(5_000) { onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }

    @Test
    fun album_search_results_list_seeded_albums_and_tapping_one_opens_its_page_in_track_order() {
        launchOnAlbumResults()

        compose.waitForText("Blue Record")
        compose.onNodeWithText("Red Record").assertExists()
        compose.onNodeWithText("Blue Record").performClick()

        val tracks = compose.onNodeWithTag(ALBUM_TRACKS_TAG)
        tracks.onChildAt(0).assertTextContains("First Song")
        tracks.onChildAt(1).assertTextContains("Second Song")
    }

    @Test
    fun tapping_a_track_on_the_album_page_replaces_the_queue_with_the_albums_tracks() {
        launchOnAlbumResults()
        compose.waitForText("Blue Record")
        compose.onNodeWithText("Blue Record").performClick()

        compose.onNodeWithTag(ALBUM_TRACKS_TAG).onChildAt(1).performClick()

        compose.waitUntil(5_000) { engine.loaded.isNotEmpty() }
        assertEquals(listOf("content://media/audio/1"), engine.loaded)
        compose.onNodeWithTag(MINI_PLAYER_TAG).performClick()
        val queue = compose.onNodeWithTag(QUEUE_LIST_TAG)
        queue.onChildAt(0).assertTextContains("First Song")
        queue.onChildAt(1).assertTextContains("Second Song")
        queue.onChildren().assertCountEquals(2)
    }

    @Test
    fun playing_the_album_starts_at_its_first_track() {
        launchOnAlbumResults()
        compose.waitForText("Blue Record")
        compose.onNodeWithText("Blue Record").performClick()

        compose.onNodeWithText(PLAY_ALBUM_LABEL).performClick()

        compose.waitUntil(5_000) { engine.loaded.isNotEmpty() }
        assertEquals(listOf("content://media/audio/2"), engine.loaded)
    }

    @Test
    fun an_album_and_its_tracks_without_art_show_a_placeholder() {
        launchOnAlbumResults()
        compose.waitForText("Red Record")
        // Red Record is the only Album whose art is missing in this list of two, and each row shows its own placeholder.
        compose.waitUntil(5_000) { compose.onAllNodesWithTag(ROW_ART_PLACEHOLDER_TAG).fetchSemanticsNodes().size == 2 }

        compose.onNodeWithText("Red Record").performClick()

        // The page header plus its one Track.
        compose.waitUntil(5_000) { compose.onAllNodesWithTag(ROW_ART_PLACEHOLDER_TAG).fetchSemanticsNodes().size == 2 }
    }
}
