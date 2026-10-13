package com.example.musicplayer.ui

import android.Manifest
import android.os.Build
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.hasTestTag
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.example.musicplayer.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import musicplayer.core.FakePlaybackEngine
import musicplayer.core.FakeTrackSource
import musicplayer.core.rawTrack
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class NowPlayingTest {
    @get:Rule(order = 0) val hilt = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose = createEmptyComposeRule()

    @Inject lateinit var source: FakeTrackSource
    @Inject lateinit var engine: FakePlaybackEngine
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
            rawTrack(1, title = "Alpha", artist = "First Band", durationMs = 200_000),
            rawTrack(2, title = "Beta", artist = "Second Band", durationMs = 200_000),
        )
        scenario = ActivityScenario.launch(MainActivity::class.java)
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Alpha").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Alpha").performClick()
    }

    @After fun tearDown() { scenario?.close() }

    private fun openNowPlaying() {
        compose.onNodeWithTag(MINI_PLAYER_TAG).performClick()
        compose.onNodeWithTag(NOW_PLAYING_TAG).assertIsDisplayed()
    }

    @Test
    fun tapping_the_mini_player_shows_the_current_track_and_the_queue() {
        openNowPlaying()

        compose.onNodeWithTag(ART_PLACEHOLDER_TAG).assertIsDisplayed()
        compose.onNodeWithTag(NOW_PLAYING_TAG).assertIsDisplayed()
        // Title and Artist in the header, and both Tracks in the Queue list.
        assertTrue(compose.onAllNodesWithText("Alpha").fetchSemanticsNodes().size >= 2)
        compose.onNode(hasText("Beta") and hasAnyAncestor(hasTestTag(QUEUE_LIST_TAG))).assertIsDisplayed()
    }

    @Test
    fun dragging_the_seek_bar_seeks_the_track() {
        openNowPlaying()

        compose.onNodeWithTag(SEEK_BAR_TAG).performSemanticsAction(SemanticsActions.SetProgress) { it(0.5f) }

        compose.waitUntil(5_000) { engine.positionMs > 0 }
        assertEquals(100_000.0, engine.positionMs.toDouble(), 2_000.0)
    }

    @Test
    fun next_moves_on_to_the_next_track() {
        openNowPlaying()

        compose.onNodeWithContentDescription(NEXT_DESCRIPTION).performClick()

        assertEquals("content://media/audio/2", engine.loaded.last())
    }

    @Test
    fun previous_after_3_seconds_restarts_the_current_track() {
        openNowPlaying()
        engine.seekTo(10_000)

        compose.onNodeWithContentDescription(PREVIOUS_DESCRIPTION).performClick()

        assertEquals(0L, engine.positionMs)
        assertEquals(1, engine.loaded.size)
    }

    @Test
    fun a_playback_error_shows_the_skipped_snackbar() {
        engine.fail()

        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("Couldn't play Alpha, skipped").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Couldn't play Alpha, skipped").assertIsDisplayed()
    }
}
