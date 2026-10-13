package com.example.musicplayer.ui

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.example.musicplayer.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import musicplayer.core.FakePlaybackEngine
import musicplayer.core.FakeTrackSource
import musicplayer.core.rawTrack
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import org.junit.Assert.assertEquals

@HiltAndroidTest
class MiniPlayerTest {
    @get:Rule(order = 0) val hilt = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose = createEmptyComposeRule()

    @Inject lateinit var source: FakeTrackSource
    @Inject lateinit var engine: FakePlaybackEngine
    private var scenario: ActivityScenario<MainActivity>? = null

    @Before
    fun setUp() {
        hilt.inject()
        if (Build.VERSION.SDK_INT >= 33) {
            // Keeps the system notification-permission dialog off the screen under test.
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            instrumentation.uiAutomation.grantRuntimePermission(
                instrumentation.targetContext.packageName,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        }
        source.tracks = listOf(
            rawTrack(1, title = "Alpha", artist = "First Band"),
            rawTrack(2, title = "Beta", artist = "Second Band"),
        )
        scenario = ActivityScenario.launch(MainActivity::class.java)
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Alpha").fetchSemanticsNodes().isNotEmpty() }
    }

    @After fun tearDown() { scenario?.close() }

    @Test
    fun there_is_no_mini_player_until_a_track_is_tapped() {
        assertEquals(0, compose.onAllNodesWithTag(MINI_PLAYER_TAG).fetchSemanticsNodes().size)
    }

    @Test
    fun tapping_a_track_shows_it_in_the_mini_player_and_loads_it() {
        compose.onNodeWithText("Beta").performClick()

        compose.onNodeWithTag(MINI_PLAYER_TAG).assertIsDisplayed()
        compose.onNodeWithTag(MINI_PLAYER_TAG).assert(hasAnyDescendant(hasText("Beta")))
        compose.onNodeWithTag(MINI_PLAYER_TAG).assert(hasAnyDescendant(hasText("Second Band")))
        assertEquals(listOf("content://media/audio/2"), engine.loaded)
    }

    @Test
    fun play_pause_toggles_the_playing_state() {
        compose.onNodeWithText("Alpha").performClick()
        compose.onNodeWithContentDescription(PAUSE_DESCRIPTION).assertIsDisplayed()

        compose.onNodeWithContentDescription(PAUSE_DESCRIPTION).performClick()
        compose.onNodeWithContentDescription(PLAY_DESCRIPTION).assertIsDisplayed()

        compose.onNodeWithContentDescription(PLAY_DESCRIPTION).performClick()
        compose.onNodeWithContentDescription(PAUSE_DESCRIPTION).assertIsDisplayed()
    }
}
