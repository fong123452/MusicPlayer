package musicplayer.core

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LibraryTest {
    private suspend fun libraryOf(vararg tracks: RawTrack): Library {
        val library = Library(FakeTrackSource(tracks.toList()))
        library.refresh()
        return library
    }

    @Test
    fun `a track with no tags shows as Unknown Artist, Unknown Album and its file name`() = runTest {
        val library = libraryOf(
            rawTrack(1, title = null, artist = null, album = null, displayName = "mystery.flac"),
        )

        val track = library.snapshot.value.tracks.single()
        assertEquals("mystery.flac", track.title)
        assertEquals("Unknown Artist", track.artist)
        assertEquals("Unknown Album", track.album)
    }

    @Test
    fun `tags that are present are kept as they are`() = runTest {
        val library = libraryOf(
            rawTrack(7, title = "Song", artist = "Band", album = "Record", albumArtist = "Various Artists", albumId = 3, discNumber = 2, trackNumber = 5),
        )

        val track = library.snapshot.value.tracks.single()
        assertEquals(7L, track.id)
        assertEquals("Song", track.title)
        assertEquals("Band", track.artist)
        assertEquals("Record", track.album)
        assertEquals("Various Artists", track.albumArtist)
        assertEquals(3L, track.albumId)
        assertEquals(2, track.discNumber)
        assertEquals(5, track.trackNumber)
    }

    @Test
    fun `a blank title falls back to the file name`() = runTest {
        val library = libraryOf(rawTrack(1, title = "  ", displayName = "untitled.mp3"))

        assertEquals("untitled.mp3", library.snapshot.value.tracks.single().title)
    }

    @Test
    fun `tracks are sorted by title ignoring case`() = runTest {
        val library = libraryOf(
            rawTrack(1, title = "banana"),
            rawTrack(2, title = "Cherry"),
            rawTrack(3, title = "apple"),
            rawTrack(4, title = "Apricot"),
        )

        assertEquals(
            listOf("apple", "Apricot", "banana", "Cherry"),
            library.snapshot.value.tracks.map { it.title },
        )
    }

    @Test
    fun `the library is not loaded until the first refresh, and empty when nothing is found`() = runTest {
        val library = Library(FakeTrackSource())
        assertEquals(false, library.snapshot.value.loaded)

        library.refresh()

        assertEquals(true, library.snapshot.value.loaded)
        assertEquals(emptyList(), library.snapshot.value.tracks)
    }

    @Test
    fun `refresh picks up files added since the last scan`() = runTest {
        val source = FakeTrackSource(listOf(rawTrack(1, title = "Old")))
        val library = Library(source)
        library.refresh()

        source.tracks = listOf(rawTrack(1, title = "Old"), rawTrack(2, title = "New"))
        library.refresh()

        assertEquals(listOf("New", "Old"), library.snapshot.value.tracks.map { it.title })
    }

    @Test
    fun `tracks with the same title keep a stable order`() = runTest {
        val library = libraryOf(rawTrack(9, title = "Same"), rawTrack(2, title = "same"), rawTrack(5, title = "Same"))

        assertEquals(listOf(2L, 5L, 9L), library.snapshot.value.tracks.map { it.id })
    }
}
