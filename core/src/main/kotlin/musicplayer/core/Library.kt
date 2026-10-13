package musicplayer.core

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class LibrarySnapshot(
    val tracks: List<Track>,
    /** False until the first refresh has completed. */
    val loaded: Boolean,
)

class Library(private val source: TrackSource) {
    private val state = MutableStateFlow(LibrarySnapshot(emptyList(), loaded = false))
    val snapshot: StateFlow<LibrarySnapshot> = state

    /** Rescans the device. Returns false, keeping the previous snapshot, if the scan failed. */
    suspend fun refresh(): Boolean {
        val raw = try {
            source.loadTracks()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            return false
        }
        state.value = LibrarySnapshot(raw.map(::toTrack).sortedWith(byTitle), loaded = true)
        return true
    }

    private fun toTrack(raw: RawTrack) = Track(
        id = raw.id,
        uri = raw.uri,
        title = raw.title.orFallback(raw.displayName),
        artist = raw.artist.orFallback(UNKNOWN_ARTIST),
        album = raw.album.orFallback(UNKNOWN_ALBUM),
        albumArtist = raw.albumArtist?.takeIf { it.isNotBlank() },
        albumId = raw.albumId,
        discNumber = raw.discNumber,
        trackNumber = raw.trackNumber,
        durationMs = raw.durationMs,
    )

    private fun String?.orFallback(fallback: String) = if (isNullOrBlank()) fallback else this

    private val byTitle = compareBy<Track, String>(String.CASE_INSENSITIVE_ORDER) { it.title }.thenBy { it.id }

    companion object {
        const val UNKNOWN_ARTIST = "Unknown Artist"
        const val UNKNOWN_ALBUM = "Unknown Album"
    }
}
