package musicplayer.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class QueueTest {
    private val engine = FakePlaybackEngine()

    private fun TestScope.queueOf() = Queue(engine, CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

    private fun track(id: Long) = Track(id, "content://media/audio/$id", "T$id", "A", "B", null, null, null, null, 180_000)
    private val tracks = (1L..3L).map(::track)

    @Test
    fun `replacing the queue makes the start track current, loads it and starts playing`() = runTest {
        val queue = queueOf()

        queue.replace(tracks, startIndex = 1)

        assertEquals(tracks, queue.queue.value.tracks)
        assertEquals(tracks[1], queue.queue.value.current)
        assertEquals(listOf("content://media/audio/2"), engine.loaded)
        assertTrue(queue.isPlaying.value)
    }

    @Test
    fun `when a track finishes on its own the queue moves to and loads the next track`() = runTest {
        val queue = queueOf()
        queue.replace(tracks, startIndex = 0)

        engine.complete()

        assertEquals(tracks[1], queue.queue.value.current)
        assertEquals(listOf("content://media/audio/1", "content://media/audio/2"), engine.loaded)
        assertTrue(queue.isPlaying.value)
    }

    @Test
    fun `when the last track finishes playback stops and nothing more is loaded`() = runTest {
        val queue = queueOf()
        queue.replace(tracks, startIndex = 2)

        engine.complete()

        assertEquals(1, engine.loaded.size)
        assertFalse(queue.isPlaying.value)
        assertEquals(tracks[2], queue.queue.value.current)
    }

    @Test
    fun `pause and play control the engine`() = runTest {
        val queue = queueOf()
        queue.replace(tracks, startIndex = 0)

        queue.pause()
        assertFalse(queue.isPlaying.value)
        queue.play()
        assertTrue(queue.isPlaying.value)
    }

    @Test
    fun `an empty queue has no current track`() = runTest {
        assertNull(queueOf().queue.value.current)
    }
}
