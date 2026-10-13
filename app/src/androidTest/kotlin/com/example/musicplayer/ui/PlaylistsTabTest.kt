package com.example.musicplayer.ui

import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import com.example.musicplayer.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import musicplayer.core.FakeTrackSource
import musicplayer.core.rawTrack
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class PlaylistsTabTest {
    @get:Rule(order = 0) val hilt = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose = createEmptyComposeRule()

    @Inject lateinit var source: FakeTrackSource
    private var scenario: ActivityScenario<MainActivity>? = null

    @Before
    fun setUp() {
        hilt.inject()
        source.tracks = listOf(rawTrack(1, title = "Alpha"), rawTrack(2, title = "Beta"))
        scenario = ActivityScenario.launch(MainActivity::class.java)
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Alpha").fetchSemanticsNodes().isNotEmpty() }
    }

    @After fun tearDown() { scenario?.close() }

    @Test
    fun a_playlist_created_while_adding_a_track_shows_in_the_playlists_tab_with_count_1() {
        compose.onAllNodesWithContentDescription(TRACK_MENU_DESCRIPTION)[0].performClick()
        compose.onNodeWithText(ADD_TO_PLAYLIST_LABEL).performClick()
        compose.onNodeWithText(NEW_PLAYLIST_LABEL).performClick()
        compose.onNodeWithTag(PLAYLIST_NAME_FIELD_TAG).performTextInput("Road trip")
        compose.onNodeWithText(CREATE_LABEL).performClick()

        compose.onNodeWithText("Playlists").performClick()

        compose.waitUntil(5_000) { compose.onAllNodesWithText("Road trip").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("1 song").assertExists()
    }

    @Test
    fun adding_a_track_already_in_the_playlist_says_so() {
        repeat(2) { attempt ->
            compose.onAllNodesWithContentDescription(TRACK_MENU_DESCRIPTION)[0].performClick()
            compose.onNodeWithText(ADD_TO_PLAYLIST_LABEL).performClick()
            if (attempt == 0) {
                compose.onNodeWithText(NEW_PLAYLIST_LABEL).performClick()
                compose.onNodeWithTag(PLAYLIST_NAME_FIELD_TAG).performTextInput("Mix")
                compose.onNodeWithText(CREATE_LABEL).performClick()
            } else {
                compose.onNodeWithText("Mix").performClick()
            }
        }

        compose.waitUntil(5_000) { compose.onAllNodesWithText("Already in Mix").fetchSemanticsNodes().isNotEmpty() }
    }

    /** Creates a Playlist called [name] holding the first Track, and opens its page from the Playlists tab. */
    private fun openNewPlaylist(name: String) {
        compose.onAllNodesWithContentDescription(TRACK_MENU_DESCRIPTION)[0].performClick()
        compose.onNodeWithText(ADD_TO_PLAYLIST_LABEL).performClick()
        compose.onNodeWithText(NEW_PLAYLIST_LABEL).performClick()
        compose.onNodeWithTag(PLAYLIST_NAME_FIELD_TAG).performTextInput(name)
        compose.onNodeWithText(CREATE_LABEL).performClick()
        compose.onNodeWithText("Playlists").performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithText(name).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(name).performClick()
    }

    @Test
    fun renaming_a_playlist_from_its_page_shows_the_new_name_in_the_playlists_tab() {
        openNewPlaylist("Mix")

        compose.onNodeWithText(RENAME_LABEL).performClick()
        compose.onNodeWithTag(PLAYLIST_NAME_FIELD_TAG).performTextClearance()
        compose.onNodeWithTag(PLAYLIST_NAME_FIELD_TAG).performTextInput("Road trip")
        compose.onAllNodesWithText(RENAME_LABEL).onLast().performClick()
        compose.onNodeWithContentDescription(BACK_DESCRIPTION).performClick()

        compose.waitUntil(5_000) { compose.onAllNodesWithText("Road trip").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Mix").assertDoesNotExist()
    }

    @Test
    fun deleting_a_playlist_from_its_page_takes_it_out_of_the_playlists_tab() {
        openNewPlaylist("Mix")

        compose.onNodeWithText(DELETE_LABEL).performClick()
        compose.onAllNodesWithText(DELETE_LABEL).onLast().performClick()

        compose.waitUntil(5_000) { compose.onAllNodesWithText("Mix").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithTag(PLAYLISTS_LIST_TAG).assertExists()
    }

    @Test
    fun removing_a_track_from_a_playlist_page_lowers_its_count() {
        openNewPlaylist("Mix")

        compose.onNodeWithContentDescription(REMOVE_FROM_PLAYLIST_DESCRIPTION).performClick()
        compose.onNodeWithContentDescription(BACK_DESCRIPTION).performClick()

        compose.waitUntil(5_000) { compose.onAllNodesWithText("0 songs").fetchSemanticsNodes().isNotEmpty() }
    }
}
