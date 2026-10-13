package com.example.musicplayer

import com.example.musicplayer.playback.whenConnected
import com.google.common.util.concurrent.SettableFuture
import kotlin.test.Test
import kotlin.test.assertEquals

class WhenConnectedTest {
    private val direct = java.util.concurrent.Executor { it.run() }

    @Test
    fun `the command runs with the controller once connected`() {
        val future = SettableFuture.create<String>()
        val seen = mutableListOf<String>()
        whenConnected(future, direct) { seen += it }

        future.set("controller")

        assertEquals(listOf("controller"), seen)
    }

    @Test
    fun `a failed connection skips the command instead of throwing`() {
        val future = SettableFuture.create<String>()
        val seen = mutableListOf<String>()
        whenConnected(future, direct) { seen += it }

        future.setException(IllegalStateException("service unavailable"))

        assertEquals(emptyList(), seen)
    }

    @Test
    fun `a cancelled connection skips the command instead of throwing`() {
        val future = SettableFuture.create<String>()
        val seen = mutableListOf<String>()
        whenConnected(future, direct) { seen += it }

        future.cancel(false)

        assertEquals(emptyList(), seen)
    }
}
