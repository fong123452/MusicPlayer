package musicplayer.core

/** Everything credited to one name: the Albums it is Album Artist of, and the Tracks it is Track Artist of. */
data class Artist(
    val key: String,
    val name: String,
    val albums: List<Album>,
    /** Includes Tracks on compilations; empty for an Album Artist who has no Track of their own. */
    val tracks: List<Track>,
)
