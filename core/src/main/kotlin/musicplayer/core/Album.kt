package musicplayer.core

/** Tracks that share an album title and Album Artist, or MediaStore's album id when there is no Album Artist. */
data class Album(
    val id: String,
    val title: String,
    val artist: String,
    /** MediaStore's album id of the first Track that has one; used to find the art. */
    val albumId: Long?,
    /** In disc, then track number order. */
    val tracks: List<Track>,
)
