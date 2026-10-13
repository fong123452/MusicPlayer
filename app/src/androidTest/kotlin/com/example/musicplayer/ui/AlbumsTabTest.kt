package com.example.musicplayer.ui

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertTextContains
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
class AlbumsTabTest {
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

    private fun launchOnAlbumsTab() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        compose.waitForText("Lonely")
        compose.onNodeWithText("Albums").performClick()
    }

    private fun ComposeTestRule.waitForText(text: String) =
        waitUntil(5_000) { onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }

    @Test
    fun the_albums_tab_lists_seeded_albums_and_tapping_one_opens_its_page_in_track_order() {
        launchOnAlbumsTab()

        compose.waitForText("Blue Record")
        compose.onNodeWithText("Red Record").assertExists()
        compose.onNodeWithText("Blue Record").performClick()

        val tracks = compose.onNodeWithTag(ALBUM_TRACKS_TAG)
        tracks.onChildAt(0).assertTextContains("First Song")
        tracks.onChildAt(1).assertTextContains("Second Song")
    }

    @Test
    fun tapping_a_track_on_the_album_page_replaces_the_queue_with_the_albums_tracks() {
        launchOnAlbumsTab()
        compose.waitForText("Blue Record")
        compose.onNodeWithText("Blue Record").performClick()

        compose.onNodeWithTag(ALBUM_TRACKS_TAG).onChildAt(1).performClick()

        compose.waitUntil(5_000) { engine.loaded.isNotEmpty() }
        assertEquals(listOf("content://media/audio/1"), engine.loaded)
        compose.onNodeWithTag(MINI_PLAYER_TAG).assertExists()
    }

    @Test
    fun playing_the_album_starts_at_its_first_track() {
        launchOnAlbumsTab()
        compose.waitForText("Blue Record")
        compose.onNodeWithText("Blue Record").performClick()

        compose.onNodeWithText(PLAY_ALBUM_LABEL).performClick()

        compose.waitUntil(5_000) { engine.loaded.isNotEmpty() }
        assertEquals(listOf("content://media/audio/2"), engine.loaded)
    }

    @Test
    fun an_album_and_its_tracks_without_art_show_a_placeholder() {
        launchOnAlbumsTab()
        compose.waitForText("Red Record")

        compose.waitUntil(5_000) { compose.onAllNodesWithTag(ROW_ART_PLACEHOLDER_TAG).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Red Record").performClick()

        compose.waitUntil(5_000) { compose.onAllNodesWithTag(ROW_ART_PLACEHOLDER_TAG).fetchSemanticsNodes().isNotEmpty() }
    }
}
