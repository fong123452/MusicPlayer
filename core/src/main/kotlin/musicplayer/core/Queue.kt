package musicplayer.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class QueueState(val tracks: List<Track>, val currentIndex: Int) {
    val current: Track? get() = tracks.getOrNull(currentIndex)
}

/** Past this position, previous restarts the current Track instead of leaving it. */
const val RESTART_THRESHOLD_MS = 3_000L

/** The ordered Tracks that play now. It drives the [PlaybackEngine] one Track at a time. */
class Queue(private val engine: PlaybackEngine, scope: CoroutineScope) {
    private val state = MutableStateFlow(QueueState(emptyList(), -1))
    val queue: StateFlow<QueueState> = state
    val isPlaying: StateFlow<Boolean> = engine.isPlaying

    private val skippedEvents = MutableSharedFlow<String>(extraBufferCapacity = 16)

    /** One-shot: the title of each Track that could not play and was skipped. */
    val skipped: Flow<String> = skippedEvents

    init {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            engine.events.collect { event ->
                when (event) {
                    PlaybackEvent.Completed -> next()
                    is PlaybackEvent.Error -> skipCurrent()
                }
            }
        }
    }

    /** Replaces the Queue with [tracks] and starts playing the Track at [startIndex]. */
    fun replace(tracks: List<Track>, startIndex: Int) {
        state.value = QueueState(tracks, startIndex)
        loadCurrentAndPlay()
    }

    fun play() = engine.play()

    fun pause() = engine.pause()

    fun seekTo(positionMs: Long) = engine.seekTo(positionMs)

    /** Moves to the next Track; on the last Track playback stops. */
    fun next() {
        val s = state.value
        if (s.currentIndex + 1 > s.tracks.lastIndex) {
            engine.pause()
            return
        }
        state.value = s.copy(currentIndex = s.currentIndex + 1)
        loadCurrentAndPlay()
    }

    /** Restarts the current Track once it has played past [RESTART_THRESHOLD_MS], otherwise moves back. */
    fun previous(positionMs: Long) {
        val s = state.value
        if (positionMs > RESTART_THRESHOLD_MS || s.currentIndex <= 0) {
            engine.seekTo(0)
            return
        }
        state.value = s.copy(currentIndex = s.currentIndex - 1)
        loadCurrentAndPlay()
    }

    private fun skipCurrent() {
        val failed = state.value.current ?: return
        skippedEvents.tryEmit(failed.title)
        next()
    }

    private fun loadCurrentAndPlay() {
        val track = state.value.current ?: return
        engine.load(track)
        engine.play()
    }
}
