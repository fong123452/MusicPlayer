package com.example.musicplayer.ui

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
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
class QueueEditingTest {
    @get:Rule(order = 0) val hilt = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose = createEmptyComposeRule()

    @Inject lateinit var source: FakeTrackSource
    private var scenario: ActivityScenario<MainActivity>? = null

    @Before
    fun setUp() {
        hilt.inject()
        if (Build.VERSION.SDK_INT >= 33) {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            instrumentation.uiAutomation.grantRuntimePermission(
                instrumentation.targetContext.packageName,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        }
        source.tracks = listOf(
            rawTrack(1, title = "Alpha", durationMs = 200_000),
            rawTrack(2, title = "Beta", durationMs = 200_000),
            rawTrack(3, title = "Gamma", durationMs = 200_000),
        )
        scenario = ActivityScenario.launch(MainActivity::class.java)
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Alpha").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Alpha").performClick() // Queue is now Alpha, Beta, Gamma
    }

    @After fun tearDown() { scenario?.close() }

    /** Opens the row menu of the [row]th Track on the Songs tab and picks [action]. */
    private fun fromRow(row: Int, action: String) {
        compose.onAllNodesWithContentDescription(TRACK_MENU_DESCRIPTION)[row].performClick()
        compose.onNodeWithText(action).performClick()
    }

    private fun openNowPlaying() {
        compose.onNodeWithTag(MINI_PLAYER_TAG).performClick()
    }

    @Test
    fun play_next_from_a_track_row_shows_up_right_after_the_current_track_in_the_queue() {
        fromRow(row = 2, action = PLAY_NEXT_LABEL) // Gamma

        openNowPlaying()

        val queue = compose.onNodeWithTag(QUEUE_LIST_TAG)
        queue.onChildAt(0).assert(hasAnyDescendant(hasText("Alpha")))
        queue.onChildAt(1).assert(hasAnyDescendant(hasText("Gamma")))
        queue.onChildAt(2).assert(hasAnyDescendant(hasText("Beta")))
    }

    @Test
    fun add_to_queue_from_a_track_row_shows_up_at_the_end_of_the_queue() {
        fromRow(row = 0, action = ADD_TO_QUEUE_LABEL) // Alpha again

        openNowPlaying()

        val queue = compose.onNodeWithTag(QUEUE_LIST_TAG)
        queue.onChildAt(3).assert(hasAnyDescendant(hasText("Alpha")))
    }

    @Test
    fun removing_a_track_from_the_queue_takes_it_out_of_the_list() {
        openNowPlaying()

        compose.onAllNodesWithContentDescription(REMOVE_FROM_QUEUE_DESCRIPTION)[1].performClick() // Beta

        val queue = compose.onNodeWithTag(QUEUE_LIST_TAG)
        queue.onChildAt(0).assert(hasAnyDescendant(hasText("Alpha")))
        queue.onChildAt(1).assert(hasAnyDescendant(hasText("Gamma")))
    }
}
