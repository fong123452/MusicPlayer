package com.example.musicplayer

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.example.musicplayer.permission.AudioPermission
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import musicplayer.core.FakeTrackSource
import musicplayer.core.Library
import musicplayer.core.rawTrack
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class LibraryRefreshObserverTest {
    private class FakePermission(var granted: Boolean) : AudioPermission {
        override val permission = "fake"
        override fun isGranted() = granted
    }

    private class Owner : LifecycleOwner {
        val registry = LifecycleRegistry.createUnsafe(this)
        override val lifecycle: Lifecycle get() = registry
    }

    @Test
    fun `the Library refreshes each time the app comes to the foreground`() = runTest {
        val source = FakeTrackSource(listOf(rawTrack(1)))
        val library = Library(source)
        val owner = Owner()
        owner.registry.addObserver(
            LibraryRefreshObserver(library, FakePermission(true), TestScope(UnconfinedTestDispatcher(testScheduler))),
        )

        owner.registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        assertEquals(1, source.loadCount)
        assertEquals(1, library.snapshot.value.tracks.size)

        owner.registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        owner.registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        assertEquals(2, source.loadCount)
    }

    @Test
    fun `nothing is scanned when the permission has not been granted`() = runTest {
        val source = FakeTrackSource(listOf(rawTrack(1)))
        val owner = Owner()
        owner.registry.addObserver(
            LibraryRefreshObserver(Library(source), FakePermission(false), TestScope(UnconfinedTestDispatcher(testScheduler))),
        )

        owner.registry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        assertEquals(0, source.loadCount)
    }

    @Test
    fun `a failed scan on coming to the foreground keeps the Library and does not crash`() = runTest {
        val source = FakeTrackSource(listOf(rawTrack(1)))
        val library = Library(source)
        val owner = Owner()
        val scope = TestScope(UnconfinedTestDispatcher(testScheduler))
        owner.registry.addObserver(LibraryRefreshObserver(library, FakePermission(true), scope))
        owner.registry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        source.failure = SecurityException("revoked")
        owner.registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        owner.registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        scope.runCurrent()

        assertEquals(2, source.loadCount)
        assertEquals(1, library.snapshot.value.tracks.size)
    }
}
