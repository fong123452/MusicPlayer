package musicplayer.core

/** Port: where the Library finds out which Tracks exist on the device. */
interface TrackSource {
    /** Returns the current set of music files. Already filtered to music of at least 30 seconds. */
    suspend fun loadTracks(): List<RawTrack>
}
