package musicplayer.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.launch
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

    @Test
    fun `previous after more than 3 seconds restarts the current track`() = runTest {
        val queue = queueOf()
        queue.replace(tracks, startIndex = 1)
        engine.seekTo(5_000)

        queue.previous(positionMs = 5_000)

        assertEquals(tracks[1], queue.queue.value.current)
        assertEquals(0L, engine.positionMs)
        assertEquals(1, engine.loaded.size)
    }

    @Test
    fun `previous within 3 seconds moves to the previous track`() = runTest {
        val queue = queueOf()
        queue.replace(tracks, startIndex = 1)

        queue.previous(positionMs = 3_000)

        assertEquals(tracks[0], queue.queue.value.current)
        assertEquals("content://media/audio/1", engine.loaded.last())
        assertTrue(queue.isPlaying.value)
    }

    @Test
    fun `previous on the first track within 3 seconds restarts it`() = runTest {
        val queue = queueOf()
        queue.replace(tracks, startIndex = 0)
        engine.seekTo(1_000)

        queue.previous(positionMs = 1_000)

        assertEquals(tracks[0], queue.queue.value.current)
        assertEquals(0L, engine.positionMs)
    }

    @Test
    fun `next moves to the next track`() = runTest {
        val queue = queueOf()
        queue.replace(tracks, startIndex = 0)

        queue.next()

        assertEquals(tracks[1], queue.queue.value.current)
        assertEquals("content://media/audio/2", engine.loaded.last())
    }

    @Test
    fun `next on the last track stops playback`() = runTest {
        val queue = queueOf()
        queue.replace(tracks, startIndex = 2)

        queue.next()

        assertFalse(queue.isPlaying.value)
        assertEquals(tracks[2], queue.queue.value.current)
        assertEquals(1, engine.loaded.size)
    }

    @Test
    fun `a playback error skips to the next track and reports the skipped title once`() = runTest {
        val queue = queueOf()
        val skipped = mutableListOf<String>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { queue.skipped.collect { skipped += it } }
        queue.replace(tracks, startIndex = 0)

        engine.fail("boom")

        assertEquals(tracks[1], queue.queue.value.current)
        assertTrue(queue.isPlaying.value)
        assertEquals(listOf("T1"), skipped)
    }

    @Test
    fun `a playback error on the last track stops and still reports it`() = runTest {
        val queue = queueOf()
        val skipped = mutableListOf<String>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { queue.skipped.collect { skipped += it } }
        queue.replace(tracks, startIndex = 2)

        engine.fail()

        assertFalse(queue.isPlaying.value)
        assertEquals(listOf("T3"), skipped)
    }

    @Test
    fun `play after the queue ran out plays the last track again`() = runTest {
        val queue = queueOf()
        queue.replace(tracks, startIndex = 2)
        engine.complete()

        queue.play()

        assertEquals(tracks[2], queue.queue.value.current)
        assertEquals(2, engine.loaded.size)
        assertTrue(queue.isPlaying.value)
    }

    @Test
    fun `play after a pause resumes without reloading`() = runTest {
        val queue = queueOf()
        queue.replace(tracks, startIndex = 2)
        queue.pause()

        queue.play()

        assertEquals(1, engine.loaded.size)
        assertTrue(queue.isPlaying.value)
    }

    @Test
    fun `a failed connection to the player does not skip tracks`() = runTest {
        val queue = queueOf()
        val skipped = mutableListOf<String>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { queue.skipped.collect { skipped += it } }
        queue.replace(tracks, startIndex = 0)

        engine.failConnection()

        assertEquals(tracks[0], queue.queue.value.current)
        assertEquals(emptyList(), skipped)
    }

    @Test
    fun `playing an Album replaces the queue with its tracks in disc and track order, starting at the one chosen`() = runTest {
        val library = Library(
            FakeTrackSource(
                listOf(
                    rawTrack(1, albumArtist = "A", discNumber = 2, trackNumber = 1),
                    rawTrack(2, albumArtist = "A", discNumber = 1, trackNumber = 2),
                    rawTrack(3, albumArtist = "A", discNumber = 1, trackNumber = 1),
                    rawTrack(9, album = "Other", albumArtist = "B"),
                ),
            ),
        )
        library.refresh()
        val queue = queueOf()
        queue.replace(listOf(track(7)), startIndex = 0)
        val album = library.snapshot.value.albums.single { it.title == "Album" }

        queue.replace(album.tracks, startIndex = 1)

        assertEquals(listOf(3L, 2L, 1L), queue.queue.value.tracks.map { it.id })
        assertEquals(2L, queue.queue.value.current?.id)
    }

    @Test
    fun `playing an Artist replaces the queue with that Artist's tracks, including compilation tracks`() = runTest {
        val library = Library(
            FakeTrackSource(
                listOf(
                    rawTrack(1, title = "B", artist = "Alice", album = "Solo", albumArtist = "Alice"),
                    rawTrack(2, title = "A", artist = "Alice", album = "Hits", albumArtist = "Various Artists"),
                    rawTrack(3, title = "C", artist = "Bob", album = "Hits", albumArtist = "Various Artists"),
                ),
            ),
        )
        library.refresh()
        val queue = queueOf()
        queue.replace(listOf(track(7)), startIndex = 0)
        val alice = library.snapshot.value.artists.single { it.name == "Alice" }

        queue.replace(alice.tracks, startIndex = 1)

        assertEquals(listOf(2L, 1L), queue.queue.value.tracks.map { it.id })
        assertEquals(1L, queue.queue.value.current?.id)
    }

    private fun ids(queue: Queue) = queue.queue.value.tracks.map { it.id }

    @Test
    fun `playNext inserts the track right after the current one without interrupting playback`() = runTest {
        val queue = queueOf()
        queue.replace(tracks, startIndex = 0)

        queue.playNext(track(9))

        assertEquals(listOf(1L, 9L, 2L, 3L), ids(queue))
        assertEquals(1L, queue.queue.value.current?.id)
        assertEquals(1, engine.loaded.size)
    }

    @Test
    fun `addToEnd appends the track to the end of the queue`() = runTest {
        val queue = queueOf()
        queue.replace(tracks, startIndex = 1)

        queue.addToEnd(track(9))

        assertEquals(listOf(1L, 2L, 3L, 9L), ids(queue))
        assertEquals(2L, queue.queue.value.current?.id)
    }

    @Test
    fun `playNext and addToEnd on an empty queue start playing the track`() = runTest {
        val queue = queueOf()

        queue.playNext(track(9))

        assertEquals(listOf(9L), ids(queue))
        assertTrue(queue.isPlaying.value)

        val other = queueOf()
        other.addToEnd(track(8))
        assertEquals(listOf(8L), ids(other))
        assertEquals(8L, other.queue.value.current?.id)
    }

    @Test
    fun `adding after the last track finished makes the new track play when the user presses play`() = runTest {
        val queue = queueOf()
        queue.replace(tracks, startIndex = 2)
        engine.complete()

        queue.addToEnd(track(9))
        queue.next()

        assertEquals(9L, queue.queue.value.current?.id)
        assertTrue(queue.isPlaying.value)
    }

    @Test
    fun `moving a track reorders the queue and the current track keeps playing`() = runTest {
        val queue = queueOf()
        queue.replace((1L..4L).map(::track), startIndex = 1)

        queue.move(from = 3, to = 0) // current shifts from 1 to 2
        assertEquals(listOf(4L, 1L, 2L, 3L), ids(queue))
        assertEquals(2L, queue.queue.value.current?.id)

        queue.move(from = 2, to = 3) // moving the current track itself
        assertEquals(listOf(4L, 1L, 3L, 2L), ids(queue))
        assertEquals(2L, queue.queue.value.current?.id)

        queue.move(from = 0, to = 2) // crossing the current track from before to after: current stays
        assertEquals(listOf(1L, 3L, 4L, 2L), ids(queue))
        assertEquals(2L, queue.queue.value.current?.id)
        assertEquals(1, engine.loaded.size)
    }

    @Test
    fun `moving ignores indices outside the queue`() = runTest {
        val queue = queueOf()
        queue.replace(tracks, startIndex = 0)

        queue.move(from = 5, to = 0)
        queue.move(from = 0, to = -1)

        assertEquals(listOf(1L, 2L, 3L), ids(queue))
    }

    @Test
    fun `removing a track before the current one keeps the same track playing`() = runTest {
        val queue = queueOf()
        queue.replace(tracks, startIndex = 2)

        queue.remove(0)

        assertEquals(listOf(2L, 3L), ids(queue))
        assertEquals(3L, queue.queue.value.current?.id)
        assertEquals(1, engine.loaded.size)
        assertTrue(queue.isPlaying.value)
    }

    @Test
    fun `removing a track after the current one leaves the current track alone`() = runTest {
        val queue = queueOf()
        queue.replace(tracks, startIndex = 0)

        queue.remove(2)

        assertEquals(listOf(1L, 2L), ids(queue))
        assertEquals(1L, queue.queue.value.current?.id)
    }

    @Test
    fun `removing the current track moves playback to the next track`() = runTest {
        val queue = queueOf()
        queue.replace(tracks, startIndex = 1)

        queue.remove(1)

        assertEquals(listOf(1L, 3L), ids(queue))
        assertEquals(3L, queue.queue.value.current?.id)
        assertEquals(listOf("content://media/audio/2", "content://media/audio/3"), engine.loaded)
        assertTrue(queue.isPlaying.value)
    }

    @Test
    fun `removing the current track when none is left after it stops playback`() = runTest {
        val queue = queueOf()
        queue.replace(tracks, startIndex = 2)

        queue.remove(2)

        assertEquals(listOf(1L, 2L), ids(queue))
        assertNull(queue.queue.value.current)
        assertFalse(queue.isPlaying.value)
        assertEquals(1, engine.loaded.size)
    }

    @Test
    fun `removing the only track empties the queue and stops playback`() = runTest {
        val queue = queueOf()
        queue.replace(listOf(track(1)), startIndex = 0)

        queue.remove(0)

        assertEquals(emptyList(), ids(queue))
        assertNull(queue.queue.value.current)
        assertFalse(queue.isPlaying.value)
    }

    @Test
    fun `adding to a stopped queue after its current track was removed makes the new track current without playing it`() = runTest {
        val queue = queueOf()
        queue.replace(tracks, startIndex = 2)
        queue.remove(2)

        queue.playNext(track(9))

        assertEquals(listOf(1L, 2L, 9L), ids(queue))
        assertEquals(9L, queue.queue.value.current?.id)
        assertFalse(queue.isPlaying.value)

        queue.play()
        assertTrue(queue.isPlaying.value)
        assertEquals("content://media/audio/9", engine.loaded.last())

        val other = queueOf()
        other.replace(tracks, startIndex = 2)
        other.remove(2)
        other.addToEnd(track(8))
        assertEquals(8L, other.queue.value.current?.id)
    }

    @Test
    fun `removing the current track while paused moves to the next track without starting playback`() = runTest {
        val queue = queueOf()
        queue.replace(tracks, startIndex = 1)
        queue.pause()

        queue.remove(1)

        assertEquals(3L, queue.queue.value.current?.id)
        assertEquals("content://media/audio/3", engine.loaded.last())
        assertFalse(queue.isPlaying.value)

        queue.play()
        assertTrue(queue.isPlaying.value)
    }

    @Test
    fun `removing a track from a stopped queue keeps it stopped`() = runTest {
        val queue = queueOf()
        queue.replace(tracks, startIndex = 2)
        queue.remove(2)

        queue.remove(0)

        assertEquals(listOf(2L), ids(queue))
        assertNull(queue.queue.value.current)
        assertFalse(queue.isPlaying.value)
    }
}
