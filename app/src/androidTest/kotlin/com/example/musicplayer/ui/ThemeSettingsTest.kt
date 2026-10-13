package com.example.musicplayer.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.test.core.app.ActivityScenario
import com.example.musicplayer.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import musicplayer.core.AccentPalette
import musicplayer.core.FakeTrackSource
import musicplayer.core.ThemeMode
import musicplayer.core.rawTrack
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class ThemeSettingsTest {
    @get:Rule(order = 0) val hilt = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose = createEmptyComposeRule()

    @Inject lateinit var source: FakeTrackSource
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

    /** The color the preview bar actually shows on screen. */
    private fun previewColor(): Int {
        val bitmap = compose.onNodeWithTag(THEME_PREVIEW_TAG).captureToImage().asAndroidBitmap()
        return bitmap.getPixel(bitmap.width / 2, bitmap.height / 2)
    }

    private fun argb(value: Long) = Color(value).toArgb()

    @Test
    fun choosing_dark_then_light_recolors_the_app_without_a_restart() {
        openSettings()

        compose.onNodeWithText(ThemeMode.Dark.label()).performClick()
        compose.waitForIdle()
        assertEquals(argb(AccentPalette.first().dark.primary), previewColor())

        compose.onNodeWithText(ThemeMode.Light.label()).performClick()
        compose.waitForIdle()
        assertEquals(argb(AccentPalette.first().light.primary), previewColor())
    }

    @Test
    fun choosing_another_accent_recolors_the_app_without_a_restart() {
        openSettings()
        compose.onNodeWithText(ThemeMode.Light.label()).performClick()
        val other = AccentPalette[3]

        compose.onNodeWithContentDescription(accentDescription(other)).performClick()
        compose.waitForIdle()

        assertEquals(argb(other.light.primary), previewColor())
    }

    @Test
    fun follow_system_uses_the_devices_current_brightness() {
        openSettings()
        compose.onNodeWithText(ThemeMode.Dark.label()).performClick()

        compose.onNodeWithText(ThemeMode.FollowSystem.label()).performClick()
        compose.waitForIdle()

        val night = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
            .resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        val roles = AccentPalette.first().let { if (night) it.dark else it.light }
        assertEquals(argb(roles.primary), previewColor())
    }
}
