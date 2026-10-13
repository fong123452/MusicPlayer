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

    /** Like a real player: false while "buffering" a Track that play was asked for, until [finishBuffering]. */
    var startsImmediately = true

    /** Whether play was asked for and not undone by pause, even if the Track is still buffering. */
    var playRequested = false
        private set
    var positionMs = 0L
        private set

    override fun load(track: Track) {
        loaded += track.uri
        positionMs = 0
        playing.value = false
    }

    override fun play() {
        playRequested = true
        if (startsImmediately) playing.value = true
    }

    override fun pause() {
        playRequested = false
        playing.value = false
    }

    fun finishBuffering() { if (playRequested) playing.value = true }
    override fun seekTo(positionMs: Long) { this.positionMs = positionMs }
    override fun currentPositionMs() = positionMs

    /** The loaded Track played to its end. */
    fun complete() {
        playing.value = false
        eventFlow.tryEmit(PlaybackEvent.Completed)
    }

    fun failConnection() {
        playing.value = false
        eventFlow.tryEmit(PlaybackEvent.ConnectionFailed)
    }

    fun fail(message: String? = null) {
        playing.value = false
        eventFlow.tryEmit(PlaybackEvent.Error(message))
    }
}
