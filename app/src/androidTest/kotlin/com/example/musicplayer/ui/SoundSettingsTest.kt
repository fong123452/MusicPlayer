package com.example.musicplayer.ui

import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import com.example.musicplayer.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import musicplayer.core.FakeSoundEffects
import musicplayer.core.FakeTrackSource
import musicplayer.core.SoundSettings
import musicplayer.core.SoundSupport
import musicplayer.core.rawTrack
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class SoundSettingsTest {
    @get:Rule(order = 0) val hilt = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose = createEmptyComposeRule()

    @Inject lateinit var source: FakeTrackSource
    @Inject lateinit var effects: FakeSoundEffects
    private var scenario: ActivityScenario<MainActivity>? = null

    @Before fun setUp() {
        hilt.inject()
        source.tracks = listOf(rawTrack(1, title = "Midnight Train"))
    }

    @After fun tearDown() { scenario?.close() }

    private fun openSettings() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Midnight Train").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(SETTINGS_LABEL).performClick()
    }

    @Test
    fun the_presets_listed_are_the_ones_the_device_offers() {
        effects.setSupport(SoundSupport(listOf("Flat", "Classical", "Dance"), bassBoost = true))
        openSettings()

        compose.onNodeWithText(EQ_OFF_LABEL).assertExists()
        listOf("Flat", "Classical", "Dance").forEach { compose.onNodeWithText(it).assertExists() }
        compose.onNodeWithText("Rock").assertDoesNotExist()
    }

    @Test
    fun choosing_a_preset_and_off_and_bass_boost_applies_them() {
        effects.setSupport(SoundSupport(listOf("Flat", "Dance"), bassBoost = true))
        openSettings()

        compose.onNodeWithText("Dance").performClick()
        compose.onNodeWithText(BASS_BOOST_LABEL).performClick()
        compose.waitUntil(3_000) { effects.applied.lastOrNull() == SoundSettings("Dance", true) }

        compose.onNodeWithText(EQ_OFF_LABEL).performClick()
        compose.waitUntil(3_000) { effects.applied.lastOrNull() == SoundSettings(null, true) }
    }

    @Test
    fun an_unsupported_device_says_so_instead_of_offering_controls() {
        effects.setSupport(SoundSupport(presets = null, bassBoost = false))
        openSettings()

        compose.onNodeWithText(EQUALIZER_UNSUPPORTED).assertExists()
        compose.onNodeWithText(BASS_BOOST_UNSUPPORTED).assertExists()
        compose.onNodeWithText(EQ_OFF_LABEL).assertDoesNotExist()
    }
}
