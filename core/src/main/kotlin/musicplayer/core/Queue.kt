package musicplayer.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class QueueState(val tracks: List<Track>, val currentIndex: Int) {
    val current: Track? get() = tracks.getOrNull(currentIndex)
}

/** The ordered Tracks that play now. It drives the [PlaybackEngine] one Track at a time. */
class Queue(private val engine: PlaybackEngine, scope: CoroutineScope) {
    private val state = MutableStateFlow(QueueState(emptyList(), -1))
    val queue: StateFlow<QueueState> = state
    val isPlaying: StateFlow<Boolean> = engine.isPlaying

    init {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            engine.events.collect { event ->
                if (event is PlaybackEvent.Completed) advanceAfterCompletion()
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

    private fun advanceAfterCompletion() {
        val s = state.value
        if (s.currentIndex + 1 > s.tracks.lastIndex) return
        state.value = s.copy(currentIndex = s.currentIndex + 1)
        loadCurrentAndPlay()
    }

    private fun loadCurrentAndPlay() {
        val track = state.value.current ?: return
        engine.load(track)
        engine.play()
    }
}
