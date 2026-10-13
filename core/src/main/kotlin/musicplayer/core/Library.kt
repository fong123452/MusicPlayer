package musicplayer.core

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class LibrarySnapshot(
    val tracks: List<Track>,
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    /** False until the first refresh has completed. */
    val loaded: Boolean,
)

class Library(private val source: TrackSource) {
    private val state = MutableStateFlow(LibrarySnapshot(emptyList(), emptyList(), emptyList(), loaded = false))
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
        val tracks = raw.map(::toTrack).sortedWith(byTitle)
        val albums = groupIntoAlbums(tracks)
        state.value = LibrarySnapshot(tracks, albums, groupIntoArtists(tracks, albums), loaded = true)
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

    private fun groupIntoAlbums(tracks: List<Track>): List<Album> =
        tracks.groupBy(::albumKey).map { (key, members) ->
            val sorted = members.sortedWith(byDiscAndTrack)
            Album(
                key = key,
                title = sorted.first().album,
                albumArtist = sorted.firstNotNullOfOrNull { it.albumArtist } ?: creditFor(sorted),
                albumId = sorted.firstNotNullOfOrNull { it.albumId },
                tracks = sorted,
            )
        }.sortedWith(compareBy<Album, String>(String.CASE_INSENSITIVE_ORDER) { it.title }.thenBy { it.key })

    private fun groupIntoArtists(tracks: List<Track>, albums: List<Album>): List<Artist> {
        val tracksByArtist = tracks.groupBy { it.artist.lowercase() }
        val albumsByArtist = albums.groupBy { it.albumArtist.lowercase() }
        return (tracksByArtist.keys + albumsByArtist.keys).map { key ->
            val ownTracks = tracksByArtist[key].orEmpty()
            val ownAlbums = albumsByArtist[key].orEmpty()
            Artist(
                key = key,
                name = ownTracks.minOfOrNull { it.artist } ?: ownAlbums.minOf { it.albumArtist },
                albums = ownAlbums,
                tracks = ownTracks,
            )
        }.sortedWith(compareBy<Artist, String>(String.CASE_INSENSITIVE_ORDER) { it.name }.thenBy { it.key })
    }

    private fun albumKey(track: Track): String = when {
        track.albumArtist != null -> "artist:${track.album.lowercase()}\u0000${track.albumArtist.lowercase()}"
        track.albumId != null -> "id:${track.albumId}"
        else -> "track:${track.id}"
    }

    private fun creditFor(tracks: List<Track>): String =
        tracks.map { it.artist }.distinct().singleOrNull() ?: VARIOUS_ARTISTS

    private fun String?.orFallback(fallback: String) = if (isNullOrBlank()) fallback else this

    private val byTitle = compareBy<Track, String>(String.CASE_INSENSITIVE_ORDER) { it.title }.thenBy { it.id }

    private val byDiscAndTrack = compareBy<Track> { it.discNumber ?: 1 }
        .thenBy { it.trackNumber ?: Int.MAX_VALUE }
        .thenBy { it.title.lowercase() }
        .thenBy { it.id }

    companion object {
        const val VARIOUS_ARTISTS = "Various Artists"
        const val UNKNOWN_ARTIST = "Unknown Artist"
        const val UNKNOWN_ALBUM = "Unknown Album"
    }
}
