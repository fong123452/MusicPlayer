package musicplayer.core

/** In-memory TrackSource for tests. Change [tracks] between refreshes to simulate files appearing. */
class FakeTrackSource(var tracks: List<RawTrack> = emptyList()) : TrackSource {
    /** When set, [loadTracks] throws it, as a source does when the device cannot be read. */
    var failure: Exception? = null

    var loadCount = 0
        private set

    override suspend fun loadTracks(): List<RawTrack> {
        loadCount++
        failure?.let { throw it }
        return tracks
    }
}

fun rawTrack(
    id: Long,
    title: String? = "Title $id",
    artist: String? = "Artist",
    album: String? = "Album",
    albumArtist: String? = null,
    albumId: Long? = null,
    discNumber: Int? = null,
    trackNumber: Int? = null,
    durationMs: Long = 180_000,
    displayName: String = "file$id.mp3",
) = RawTrack(id, "content://media/audio/$id", title, artist, album, albumArtist, albumId, discNumber, trackNumber, durationMs, displayName)
