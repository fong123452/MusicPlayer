package com.example.musicplayer.ui

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
class SearchTest {
    @get:Rule(order = 0) val hilt = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose = createEmptyComposeRule()

    @Inject lateinit var source: FakeTrackSource
    @Inject lateinit var engine: FakePlaybackEngine
    private var scenario: ActivityScenario<MainActivity>? = null

    @Before fun setUp() {
        hilt.inject()
        source.tracks = listOf(
            rawTrack(1, title = "Midnight Train", artist = "Zed", album = "Night Moves", albumArtist = "Zed"),
            rawTrack(2, title = "Daybreak", artist = "Moonlight Band", album = "Dawn", albumArtist = "Moonlight Band"),
        )
    }

    @After fun tearDown() { scenario?.close() }

    private fun ComposeTestRule.waitForText(text: String) =
        waitUntil(5_000) { onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }

    private fun openSearch() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        compose.waitForText("Midnight Train")
        compose.onNodeWithContentDescription(SEARCH_DESCRIPTION).performClick()
    }

    @Test
    fun an_empty_query_shows_no_results() {
        openSearch()

        compose.onNodeWithTag(SEARCH_FIELD_TAG).assertExists()
        compose.onNodeWithTag(SEARCH_RESULTS_TAG).assertDoesNotExist()
        compose.onNodeWithText(NO_RESULTS_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun a_query_with_no_matches_shows_a_no_results_message() {
        openSearch()

        compose.onNodeWithTag(SEARCH_FIELD_TAG).performTextInput("zzz")

        compose.onNodeWithText(NO_RESULTS_MESSAGE).assertExists()
    }

    @Test
    fun typing_shows_results_grouped_and_tapping_an_album_opens_its_page() {
        openSearch()

        compose.onNodeWithTag(SEARCH_FIELD_TAG).performTextInput("MOVES")

        compose.waitForText(ALBUMS_HEADER)
        compose.onNodeWithText(TRACKS_HEADER).assertDoesNotExist()
        compose.onNodeWithText("Night Moves").performClick()

        compose.onNodeWithTag(ALBUM_TRACKS_TAG).assertExists()
        compose.onNodeWithText("Midnight Train").assertExists()
    }

    @Test
    fun results_are_grouped_under_tracks_albums_and_artists() {
        openSearch()

        compose.onNodeWithTag(SEARCH_FIELD_TAG).performTextInput("moon")

        compose.waitForText(ARTISTS_HEADER)
        compose.onNodeWithText("Moonlight Band").assertExists()
        compose.onNodeWithText(TRACKS_HEADER).assertDoesNotExist()
        compose.onNodeWithText(ALBUMS_HEADER).assertDoesNotExist()
    }

    @Test
    fun tapping_a_track_result_starts_playback_of_that_track() {
        openSearch()

        compose.onNodeWithTag(SEARCH_FIELD_TAG).performTextInput("daybreak")
        compose.waitForText(TRACKS_HEADER)
        compose.onNodeWithText("Daybreak").performClick()

        compose.waitUntil(5_000) { engine.loaded.isNotEmpty() }
        assertEquals(listOf("content://media/audio/2"), engine.loaded)
    }

    @Test
    fun tapping_an_artist_result_opens_the_artist_page() {
        openSearch()

        compose.onNodeWithTag(SEARCH_FIELD_TAG).performTextInput("moon")
        compose.waitForText(ARTISTS_HEADER)
        compose.onNodeWithText("Moonlight Band").performClick()

        compose.onNodeWithTag(ARTIST_PAGE_TAG).assertExists()
        compose.onNodeWithText("Daybreak").assertExists()
    }

    @Test
    fun closing_search_clears_the_query_for_the_next_time() {
        openSearch()
        compose.onNodeWithTag(SEARCH_FIELD_TAG).performTextInput("daybreak")
        compose.waitForText(TRACKS_HEADER)

        compose.onNodeWithContentDescription(BACK_DESCRIPTION).performClick()
        compose.onNodeWithContentDescription(SEARCH_DESCRIPTION).performClick()

        compose.onNodeWithTag(SEARCH_RESULTS_TAG).assertDoesNotExist()
    }
}
