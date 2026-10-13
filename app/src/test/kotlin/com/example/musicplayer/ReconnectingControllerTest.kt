package com.example.musicplayer

import com.example.musicplayer.playback.ReconnectingController
import com.google.common.util.concurrent.SettableFuture
import kotlin.test.Test
import kotlin.test.assertEquals

class ReconnectingControllerTest {
    private val crashes = mutableListOf<Throwable>()
    private val direct = java.util.concurrent.Executor {
        try { it.run() } catch (t: Throwable) { crashes += t }
    }
    private val futures = mutableListOf<SettableFuture<String>>()
    private val connected = mutableListOf<String>()
    private val failures = mutableListOf<String?>()
    private val ran = mutableListOf<String>()

    private val controller = ReconnectingController<String>(
        connect = { SettableFuture.create<String>().also { futures += it } },
        executor = direct,
        onConnected = { connected += it },
        onFailure = { failures += it.message },
    )

    @Test
    fun `a command after a failed connection reconnects and runs`() {
        controller.run { ran += "first:$it" }
        futures[0].setException(IllegalStateException("service unavailable"))

        controller.run { ran += "second:$it" }
        assertEquals(2, futures.size)
        futures[1].set("c2")

        assertEquals(listOf("second:c2"), ran)
        assertEquals(listOf("c2"), connected)
        assertEquals(emptyList(), crashes)
    }

    @Test
    fun `a failed connection is reported once however many commands were queued`() {
        controller.run { ran += "a" }
        controller.run { ran += "b" }
        futures[0].setException(IllegalStateException("service unavailable"))

        assertEquals(listOf<String?>("service unavailable"), failures)
        assertEquals(emptyList(), ran)
    }

    @Test
    fun `commands queued before connecting run in order on one connection`() {
        controller.run { ran += "1" }
        controller.run { ran += "2" }
        controller.run { ran += "3" }
        assertEquals(1, futures.size)
        futures[0].set("c")

        assertEquals(listOf("1", "2", "3"), ran)
        assertEquals(listOf("c"), connected)
    }

    @Test
    fun `a healthy connection is reused`() {
        controller.run { ran += "1" }
        futures[0].set("c")
        controller.run { ran += "2" }

        assertEquals(1, futures.size)
        assertEquals(listOf("1", "2"), ran)
    }

    @Test
    fun `a cancelled connection is retried by the next command without reporting an error`() {
        controller.run { ran += "1" }
        futures[0].cancel(false)
        controller.run { ran += "2" }
        futures[1].set("c")

        assertEquals(listOf("2"), ran)
        assertEquals(emptyList(), failures)
        assertEquals(emptyList(), crashes)
    }
}
