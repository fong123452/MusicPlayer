package musicplayer.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistsTest {
    private val source = FakeTrackSource(
        listOf(
            rawTrack(1, title = "Alpha"),
            rawTrack(2, title = "Beta"),
            rawTrack(3, title = "Gamma"),
            rawTrack(4, title = "Delta", album = "Record", albumArtist = "Band", discNumber = 1, trackNumber = 2),
            rawTrack(5, title = "Echo", album = "Record", albumArtist = "Band", discNumber = 1, trackNumber = 1),
            rawTrack(6, title = "Foxtrot", album = "Record", albumArtist = "Band", discNumber = 1, trackNumber = 3),
        ),
    )
    private val library = Library(source)
    private val store = FakePlaylistStore()

    private suspend fun playlists(): Playlists {
        if (!library.snapshot.value.loaded) library.refresh()
        return Playlists(store, library)
    }

    private fun track(id: Long) = library.snapshot.value.tracks.first { it.id == id }
    private suspend fun Playlists.current(id: Long) = playlists.first().first { it.id == id }

    @Test
    fun `a created playlist lists the tracks added to it, and a new playlists instance over the same store still has them`() = runTest {
        val id = playlists().create("Road trip")
        playlists().add(id, listOf(track(2), track(1)))

        val reopened = playlists().playlists.first()

        assertEquals(listOf("Road trip"), reopened.map { it.name })
        assertEquals(listOf(track(2), track(1)), reopened.single().tracks)
    }

    @Test
    fun `adding a track already in the playlist leaves it unchanged and reports it as already there`() = runTest {
        val lists = playlists()
        val id = lists.create("Mix")
        lists.add(id, listOf(track(1)))

        val result = lists.add(id, listOf(track(1)))

        assertEquals(listOf(track(1)), result.alreadyThere)
        assertEquals(emptyList(), result.added)
        assertEquals(listOf(track(1)), lists.current(id).tracks)
    }

    @Test
    fun `adding an album appends its tracks in album order and skips the ones already there`() = runTest {
        val lists = playlists()
        val id = lists.create("Mix")
        lists.add(id, listOf(track(1), track(4)))
        val album = library.snapshot.value.albums.first { it.title == "Record" }

        val result = lists.add(id, album.tracks)

        assertEquals(listOf(track(5), track(6)), result.added)
        assertEquals(listOf(track(4)), result.alreadyThere)
        assertEquals(listOf(track(1), track(4), track(5), track(6)), lists.current(id).tracks)
    }

    @Test
    fun `adding a set of tracks appends the missing ones in order and ignores duplicates`() = runTest {
        val lists = playlists()
        val id = lists.create("Mix")
        lists.add(id, listOf(track(3)))

        val result = lists.add(id, listOf(track(1), track(3), track(2), track(1)))

        assertEquals(listOf(track(1), track(2)), result.added)
        assertEquals(listOf(track(3)), result.alreadyThere)
        assertEquals(listOf(track(3), track(1), track(2)), lists.current(id).tracks)
    }

    @Test
    fun `ticked tracks are picked in library order, without the ones the playlist already holds`() = runTest {
        playlists()
        val all = library.snapshot.value.tracks

        val picked = tracksToAdd(all, ticked = setOf(4L, 2L, 3L), alreadyIn = setOf(3L))

        assertEquals(all.filter { it.id in setOf(2L, 4L) }, picked)
        assertEquals(emptyList(), tracksToAdd(all, ticked = emptySet(), alreadyIn = setOf(3L)))
    }

    @Test
    fun `playing a playlist fills the queue, and reordering or removing queue entries leaves the playlist unchanged`() = runTest {
        val lists = playlists()
        val id = lists.create("Mix")
        lists.add(id, listOf(track(1), track(2), track(3)))
        val queue = Queue(FakePlaybackEngine(), CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

        lists.play(lists.current(id), queue)
        assertEquals(listOf(track(1), track(2), track(3)), queue.queue.value.tracks)
        queue.move(0, 2)
        queue.remove(1)

        assertEquals(listOf(track(2), track(1)), queue.queue.value.tracks)
        assertEquals(listOf(track(1), track(2), track(3)), lists.current(id).tracks)
    }

    @Test
    fun `playing an empty playlist reports false and leaves the queue as it was`() = runTest {
        val lists = playlists()
        val id = lists.create("Empty")
        val queue = Queue(FakePlaybackEngine(), CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        queue.replace(listOf(track(3)), 0)

        val played = lists.play(lists.current(id), queue)

        assertFalse(played)
        assertEquals(listOf(track(3)), queue.queue.value.tracks)
    }

    @Test
    fun `a track that left the library no longer shows in the playlist`() = runTest {
        val lists = playlists()
        val id = lists.create("Mix")
        lists.add(id, listOf(track(1), track(2)))

        source.tracks = source.tracks.filter { it.id != 1L }
        library.refresh()

        assertEquals(listOf(track(2)), lists.current(id).tracks)
    }

    @Test
    fun `a blank playlist name is rejected`() = runTest {
        val lists = playlists()

        assertFailsWith<IllegalArgumentException> { lists.create("   ") }
        assertEquals(emptyList(), lists.playlists.first())
    }

    @Test
    fun `a playlist name is stored without surrounding spaces`() = runTest {
        val lists = playlists()

        lists.create("  Road trip ")

        assertEquals(listOf("Road trip"), lists.playlists.first().map { it.name })
    }

    @Test
    fun `moving a track changes the stored order and survives a new playlists instance`() = runTest {
        val lists = playlists()
        val id = lists.create("Mix")
        lists.add(id, listOf(track(1), track(2), track(3)))

        lists.move(id, from = 0, to = 2)

        assertEquals(listOf(track(2), track(3), track(1)), playlists().current(id).tracks)
    }

    @Test
    fun `moving outside the playlist or onto the same place changes nothing`() = runTest {
        val lists = playlists()
        val id = lists.create("Mix")
        lists.add(id, listOf(track(1), track(2)))

        lists.move(id, from = 0, to = 5)
        lists.move(id, from = -1, to = 1)
        lists.move(id, from = 1, to = 1)

        assertEquals(listOf(track(1), track(2)), lists.current(id).tracks)
    }

    @Test
    fun `removing a track takes it out of that playlist only`() = runTest {
        val lists = playlists()
        val a = lists.create("A")
        val b = lists.create("B")
        lists.add(a, listOf(track(1), track(2)))
        lists.add(b, listOf(track(1)))

        lists.remove(a, track(1))

        assertEquals(listOf(track(2)), lists.current(a).tracks)
        assertEquals(listOf(track(1)), lists.current(b).tracks)
    }

    @Test
    fun `a removed track can be added again`() = runTest {
        val lists = playlists()
        val id = lists.create("Mix")
        lists.add(id, listOf(track(1), track(2)))
        lists.remove(id, track(1))

        val result = lists.add(id, listOf(track(1)))

        assertEquals(listOf(track(1)), result.added)
        assertEquals(listOf(track(2), track(1)), lists.current(id).tracks)
    }

    @Test
    fun `renaming a playlist trims the name and keeps its tracks`() = runTest {
        val lists = playlists()
        val id = lists.create("Mix")
        lists.add(id, listOf(track(1)))

        lists.rename(id, "  Road trip ")

        assertEquals("Road trip", lists.current(id).name)
        assertEquals(listOf(track(1)), lists.current(id).tracks)
    }

    @Test
    fun `renaming to a blank name is rejected and keeps the old name`() = runTest {
        val lists = playlists()
        val id = lists.create("Mix")

        assertFailsWith<IllegalArgumentException> { lists.rename(id, " ") }
        assertEquals("Mix", lists.current(id).name)
    }

    @Test
    fun `deleting a playlist leaves the others, and the library untouched`() = runTest {
        val lists = playlists()
        val a = lists.create("A")
        val b = lists.create("B")
        lists.add(a, listOf(track(1)))
        lists.add(b, listOf(track(1)))

        lists.delete(a)

        assertEquals(listOf("B"), lists.playlists.first().map { it.name })
        assertEquals(listOf(track(1)), lists.current(b).tracks)
        assertEquals(6, library.snapshot.value.tracks.size)
    }

    @Test
    fun `a library refresh that no longer has a track drops it from every playlist for good`() = runTest {
        val lists = playlists()
        val a = lists.create("A")
        val b = lists.create("B")
        lists.add(a, listOf(track(1), track(2)))
        lists.add(b, listOf(track(1), track(3)))
        val pruning = lists.pruneOnRefresh(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

        source.tracks = source.tracks.filter { it.id != 1L }
        library.refresh()
        // The file comes back (say, a restored backup): it must not reappear in the playlists.
        source.tracks = source.tracks + rawTrack(1, title = "Alpha")
        library.refresh()
        pruning.cancel()

        assertEquals(listOf(track(2)), lists.current(a).tracks)
        assertEquals(listOf(track(3)), lists.current(b).tracks)
    }

    @Test
    fun `a library that scanned as empty does not wipe the playlists`() = runTest {
        val lists = playlists()
        val id = lists.create("Mix")
        lists.add(id, listOf(track(1)))
        val pruning = lists.pruneOnRefresh(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

        source.tracks = emptyList()
        library.refresh()
        source.tracks = listOf(rawTrack(1, title = "Alpha"))
        library.refresh()
        pruning.cancel()

        assertEquals(listOf(track(1)), lists.current(id).tracks)
    }
}
