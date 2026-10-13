package musicplayer.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** What a [PlaybackEngine] reports besides its playing state. */
sealed interface PlaybackEvent {
    /** The loaded Track played to its end on its own. */
    data object Completed : PlaybackEvent

    /** The engine could not reach its player, so no Track was tried. */
    data object ConnectionFailed : PlaybackEvent

    /** The loaded Track could not be played. */
    data class Error(val message: String?) : PlaybackEvent
}

/** Port: plays one Track at a time. The [Queue] decides which Track comes next. */
interface PlaybackEngine {
    val isPlaying: StateFlow<Boolean>
    val events: Flow<PlaybackEvent>

    /** Replaces the loaded Track with [track]; does not start playing it. */
    fun load(track: Track)
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)

    /** Where the loaded Track is now, in milliseconds. */
    fun currentPositionMs(): Long
}
