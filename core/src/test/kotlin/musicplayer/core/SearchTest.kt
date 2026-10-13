package musicplayer.core

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchTest {
    private suspend fun snapshotOf(vararg tracks: RawTrack): LibrarySnapshot {
        val library = Library(FakeTrackSource(tracks.toList()))
        library.refresh()
        return library.snapshot.value
    }

    private suspend fun sample() = snapshotOf(
        rawTrack(1, title = "Blue Monday", artist = "New Order", album = "Power", albumArtist = "New Order"),
        rawTrack(2, title = "Sunrise", artist = "Norah Jones", album = "Come Away With Me", albumArtist = "Norah Jones"),
        rawTrack(3, title = "Monday Blues", artist = "Someone", album = "Compilation", albumArtist = "Various Artists"),
    )

    @Test
    fun `matches substrings of Track titles ignoring case`() = runTest {
        val result = sample().search("MONDAY")

        assertEquals(listOf("Blue Monday", "Monday Blues"), result.tracks.map { it.title })
    }

    @Test
    fun `matches Album titles and Artist names, grouped separately`() = runTest {
        val snapshot = sample()

        assertEquals(listOf("Come Away With Me"), snapshot.search("away").albums.map { it.title })
        assertEquals(listOf("Norah Jones"), snapshot.search("norah").artists.map { it.name })
        assertTrue(snapshot.search("norah").tracks.isEmpty())
    }

    @Test
    fun `a query is matched per group, so each group can be empty independently`() = runTest {
        val result = sample().search("order")

        assertEquals(emptyList(), result.tracks.map { it.title })
        assertEquals(listOf("New Order"), result.artists.map { it.name })
        assertEquals(emptyList(), result.albums.map { it.title })

        val power = sample().search("pow")
        assertEquals(listOf("Power"), power.albums.map { it.title })
    }

    @Test
    fun `an Artist who only has Albums can be found`() = runTest {
        val result = sample().search("various")

        assertEquals(listOf("Various Artists"), result.artists.map { it.name })
    }

    @Test
    fun `an empty or blank query returns no results`() = runTest {
        val snapshot = sample()

        assertTrue(snapshot.search("").isEmpty)
        assertTrue(snapshot.search("   ").isEmpty)
    }

    @Test
    fun `a query with no matches returns no results`() = runTest {
        assertTrue(sample().search("zzz").isEmpty)
    }

    @Test
    fun `surrounding whitespace in the query is ignored`() = runTest {
        assertEquals(listOf("Sunrise"), sample().search("  sunrise ").tracks.map { it.title })
    }
}
