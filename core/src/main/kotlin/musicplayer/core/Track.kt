package musicplayer.core

/** A Track record exactly as the device reports it; tags may be missing. */
data class RawTrack(
    val id: Long,
    val uri: String,
    val title: String?,
    val artist: String?,
    val album: String?,
    val albumArtist: String?,
    val albumId: Long?,
    val discNumber: Int?,
    val trackNumber: Int?,
    val durationMs: Long,
    val displayName: String,
)

/** A music file in the Library, with tag fallbacks already applied. */
data class Track(
    val id: Long,
    val uri: String,
    val title: String,
    val artist: String,
    val album: String,
    /** Null when the file has no Album Artist tag. */
    val albumArtist: String?,
    val albumId: Long?,
    val discNumber: Int?,
    val trackNumber: Int?,
    val durationMs: Long,
)
