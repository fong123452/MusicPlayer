package com.example.musicplayer.ui

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
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
class ArtistsTabTest {
    @get:Rule(order = 0) val hilt = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose = createEmptyComposeRule()

    @Inject lateinit var source: FakeTrackSource
    @Inject lateinit var engine: FakePlaybackEngine
    private var scenario: ActivityScenario<MainActivity>? = null

    @Before fun setUp() {
        hilt.inject()
        source.tracks = listOf(
            rawTrack(1, title = "Own Song", artist = "Alice", album = "Solo Record", albumArtist = "Alice"),
            rawTrack(2, title = "Guest Spot", artist = "Alice", album = "Hits", albumArtist = "Various Artists"),
            rawTrack(3, title = "Other Spot", artist = "Bob", album = "Hits", albumArtist = "Various Artists"),
        )
    }

    @After fun tearDown() { scenario?.close() }

    private fun ComposeTestRule.waitForText(text: String) =
        waitUntil(5_000) { onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }

    private fun openArtist(name: String) {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        compose.waitForText("Own Song")
        compose.onNodeWithText("Artists").performClick()
        compose.waitForText(name)
        compose.onNodeWithText(name).performClick()
    }

    @Test
    fun the_artists_tab_lists_seeded_artists_and_tapping_one_opens_their_albums_and_tracks() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        compose.waitForText("Own Song")
        compose.onNodeWithText("Artists").performClick()
        compose.waitForText("Alice")
        compose.onNodeWithText("Bob").assertExists()
        compose.onNodeWithText("Various Artists").assertExists()

        compose.onNodeWithText("Alice").performClick()

        compose.onNodeWithText("Solo Record").assertExists()
        compose.onNodeWithText("Own Song").assertExists()
        compose.onNodeWithText("Guest Spot").assertExists()
    }

    @Test
    fun playing_from_the_artist_page_replaces_the_queue_with_the_artists_tracks() {
        openArtist("Alice")

        compose.onNodeWithText(PLAY_ARTIST_LABEL).performClick()

        compose.waitUntil(5_000) { engine.loaded.isNotEmpty() }
        assertEquals(listOf("content://media/audio/2"), engine.loaded)
    }
}
