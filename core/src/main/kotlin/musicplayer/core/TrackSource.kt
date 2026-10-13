package musicplayer.core

/** Port: where the Library finds out which Tracks exist on the device. */
interface TrackSource {
    /**
     * Returns the current set of music files, already filtered to music of at least 30 seconds.
     * An empty list means the device really has none; throws if the device could not be read.
     */
    suspend fun loadTracks(): List<RawTrack>
}
