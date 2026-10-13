package com.example.musicplayer.ui

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.app.ActivityOptionsCompat
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PermissionGateTest {
    @get:Rule val compose = createComposeRule()

    private val requested = mutableListOf<String>()
    private val registry = object : ActivityResultRegistry() {
        override fun <I, O> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?,
        ) {
            requested += input as String
        }
    }

    private fun setGate(granted: Boolean) {
        compose.setContent {
            CompositionLocalProvider(
                LocalActivityResultRegistryOwner provides object : ActivityResultRegistryOwner {
                    override val activityResultRegistry = registry
                },
            ) {
                PermissionGate(permission = "the.permission", isGranted = { granted }) { Text("library content") }
            }
        }
    }

    @Test
    fun a_denied_permission_shows_the_explanation_and_the_grant_button_asks_again() {
        setGate(granted = false)
        compose.waitForIdle()

        compose.onNodeWithText("Allow access to your music").assertExists()
        compose.onNodeWithText("library content").assertDoesNotExist()
        assertEquals(listOf("the.permission"), requested) // the ask on launch

        compose.onNodeWithText("Grant permission").performClick()

        assertEquals(listOf("the.permission", "the.permission"), requested)
    }

    @Test
    fun a_granted_permission_shows_the_content_without_asking() {
        setGate(granted = true)
        compose.waitForIdle()

        compose.onNodeWithText("library content").assertExists()
        assertEquals(emptyList<String>(), requested)
    }
}
