package musicplayer.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** PlaybackEngine for tests: records what it was asked to do; the test fires completion and error. */
class FakePlaybackEngine : PlaybackEngine {
    private val playing = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = playing

    private val eventFlow = MutableSharedFlow<PlaybackEvent>(extraBufferCapacity = 16)
    override val events = eventFlow

    val loaded = mutableListOf<String>()
    var positionMs = 0L
        private set

    override fun load(track: Track) {
        loaded += track.uri
        positionMs = 0
        playing.value = false
    }

    override fun play() { playing.value = true }
    override fun pause() { playing.value = false }
    override fun seekTo(positionMs: Long) { this.positionMs = positionMs }

    /** The loaded Track played to its end. */
    fun complete() {
        playing.value = false
        eventFlow.tryEmit(PlaybackEvent.Completed)
    }

    fun fail(message: String? = null) {
        playing.value = false
        eventFlow.tryEmit(PlaybackEvent.Error(message))
    }
}
