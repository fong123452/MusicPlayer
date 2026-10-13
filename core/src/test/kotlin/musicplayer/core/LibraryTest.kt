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

    @Test
    fun `a refresh that fails keeps the Tracks from the previous refresh`() = runTest {
        val source = FakeTrackSource(listOf(rawTrack(1, title = "Kept")))
        val library = Library(source)
        library.refresh()

        source.failure = SecurityException("permission revoked")
        val succeeded = library.refresh()

        assertEquals(false, succeeded)
        assertEquals(listOf("Kept"), library.snapshot.value.tracks.map { it.title })
        assertEquals(true, library.snapshot.value.loaded)
    }

    @Test
    fun `a refresh that fails before any success leaves the Library not loaded`() = runTest {
        val source = FakeTrackSource().apply { failure = IllegalStateException("no cursor") }
        val library = Library(source)

        assertEquals(false, library.refresh())

        assertEquals(false, library.snapshot.value.loaded)
    }

    @Test
    fun `a refresh works again after a failed one`() = runTest {
        val source = FakeTrackSource(listOf(rawTrack(1))).apply { failure = IllegalStateException() }
        val library = Library(source)
        library.refresh()

        source.failure = null
        assertEquals(true, library.refresh())

        assertEquals(1, library.snapshot.value.tracks.size)
    }

    @Test
    fun `tracks with the same album title and Album Artist but different Track Artists form one Album`() = runTest {
        val library = libraryOf(
            rawTrack(1, artist = "Alice", album = "Hits", albumArtist = "Various Artists", albumId = 10),
            rawTrack(2, artist = "Bob", album = "Hits", albumArtist = "Various Artists", albumId = 11),
            rawTrack(3, artist = "Alice", album = "Hits", albumArtist = "Alice", albumId = 12),
        )

        val albums = library.snapshot.value.albums
        assertEquals(2, albums.size)
        val compilation = albums.single { it.albumArtist == "Various Artists" }
        assertEquals("Hits", compilation.title)
        assertEquals(setOf(1L, 2L), compilation.tracks.map { it.id }.toSet())
    }

    @Test
    fun `tracks with no Album Artist group by MediaStore album id`() = runTest {
        val library = libraryOf(
            rawTrack(1, artist = "Alice", album = "Mix", albumId = 5),
            rawTrack(2, artist = "Bob", album = "Mix", albumId = 5),
            rawTrack(3, artist = "Alice", album = "Mix", albumId = 6),
        )

        val albums = library.snapshot.value.albums
        assertEquals(2, albums.size)
        assertEquals(setOf(1L, 2L), albums.single { it.albumId == 5L }.tracks.map { it.id }.toSet())
    }

    @Test
    fun `an Album sorts its Tracks by disc then track number`() = runTest {
        val library = libraryOf(
            rawTrack(1, albumArtist = "A", discNumber = 2, trackNumber = 1),
            rawTrack(2, albumArtist = "A", discNumber = 1, trackNumber = 2),
            rawTrack(3, albumArtist = "A", discNumber = 1, trackNumber = 1),
            rawTrack(4, albumArtist = "A", discNumber = null, trackNumber = 9),
        )

        assertEquals(listOf(3L, 2L, 4L, 1L), library.snapshot.value.albums.single().tracks.map { it.id })
    }

    @Test
    fun `albums are listed by title ignoring case`() = runTest {
        val library = libraryOf(
            rawTrack(1, album = "beta", albumArtist = "A"),
            rawTrack(2, album = "Alpha", albumArtist = "A"),
        )

        assertEquals(listOf("Alpha", "beta"), library.snapshot.value.albums.map { it.title })
    }

    @Test
    fun `an Album without an Album Artist is credited to its Track Artist, or Various Artists when they differ`() = runTest {
        val library = libraryOf(
            rawTrack(1, artist = "Solo", album = "One", albumId = 1),
            rawTrack(2, artist = "Solo", album = "One", albumId = 1),
            rawTrack(3, artist = "X", album = "Mix", albumId = 2),
            rawTrack(4, artist = "Y", album = "Mix", albumId = 2),
        )

        val byTitle = library.snapshot.value.albums.associateBy { it.title }
        assertEquals("Solo", byTitle.getValue("One").albumArtist)
        assertEquals("Various Artists", byTitle.getValue("Mix").albumArtist)
    }

    @Test
    fun `an Album is credited to its Album Artist, not to its Track Artists`() = runTest {
        val library = libraryOf(
            rawTrack(1, artist = "Guest One", album = "Record", albumArtist = "The Band"),
            rawTrack(2, artist = "Guest Two", album = "Record", albumArtist = "The Band"),
        )

        assertEquals("The Band", library.snapshot.value.albums.single().albumArtist)
    }

    @Test
    fun `a Track with no track number sorts after the numbered Tracks of its disc`() = runTest {
        val library = libraryOf(
            rawTrack(1, albumArtist = "A", discNumber = 1, trackNumber = null),
            rawTrack(2, albumArtist = "A", discNumber = 1, trackNumber = 3),
        )

        assertEquals(listOf(2L, 1L), library.snapshot.value.albums.single().tracks.map { it.id })
    }
}
