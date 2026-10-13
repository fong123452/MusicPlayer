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

    private var ranOut = false

    private val skippedEvents = MutableSharedFlow<String>(extraBufferCapacity = 16)

    /** One-shot: the title of each Track that could not play and was skipped. */
    val skipped: Flow<String> = skippedEvents

    init {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            engine.events.collect { event ->
                when (event) {
                    PlaybackEvent.Completed -> next()
                    is PlaybackEvent.Error -> skipCurrent()
                    PlaybackEvent.ConnectionFailed -> Unit
                }
            }
        }
    }

    /** Replaces the Queue with [tracks] and starts playing the Track at [startIndex]. */
    fun replace(tracks: List<Track>, startIndex: Int) {
        state.value = QueueState(tracks, startIndex)
        loadCurrentAndPlay()
    }

    /** Resumes; once the Queue has run out, plays the current Track again, since the engine cannot resume from its end. */
    fun play() = if (ranOut) loadCurrentAndPlay() else engine.play()

    fun pause() = engine.pause()

    fun seekTo(positionMs: Long) = engine.seekTo(positionMs)

    fun positionMs(): Long = engine.currentPositionMs()

    /** Moves to the next Track; on the last Track playback stops. */
    fun next() {
        val s = state.value
        if (s.currentIndex + 1 > s.tracks.lastIndex) {
            ranOut = true
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

    /** Inserts [track] right after the current one; on an empty Queue it starts playing. */
    fun playNext(track: Track) {
        val s = state.value
        if (s.tracks.isEmpty()) return replace(listOf(track), 0)
        val at = s.currentIndex + 1
        state.value = s.copy(tracks = s.tracks.toMutableList().apply { add(at, track) })
    }

    /** Appends [track] to the end; on an empty Queue it starts playing. */
    fun addToEnd(track: Track) {
        val s = state.value
        if (s.tracks.isEmpty()) return replace(listOf(track), 0)
        state.value = s.copy(tracks = s.tracks + track)
    }

    /** Moves the Track at [from] to [to]. The current Track keeps playing; indices outside the Queue are ignored. */
    fun move(from: Int, to: Int) {
        val s = state.value
        if (from !in s.tracks.indices || to !in s.tracks.indices || from == to) return
        val tracks = s.tracks.toMutableList().apply { add(to, removeAt(from)) }
        val current = when {
            s.currentIndex == from -> to
            from < s.currentIndex && to >= s.currentIndex -> s.currentIndex - 1
            from > s.currentIndex && to <= s.currentIndex -> s.currentIndex + 1
            else -> s.currentIndex
        }
        state.value = QueueState(tracks, current)
    }

    /**
     * Removes the Track at [index]. Removing the current Track moves playback to the one after it, or
     * stops when none is left. Removing any other Track keeps the same Track playing.
     */
    fun remove(index: Int) {
        val s = state.value
        if (index !in s.tracks.indices) return
        val tracks = s.tracks.toMutableList().apply { removeAt(index) }
        when {
            index < s.currentIndex -> state.value = QueueState(tracks, s.currentIndex - 1)
            index > s.currentIndex -> state.value = s.copy(tracks = tracks)
            tracks.isEmpty() -> {
                state.value = QueueState(emptyList(), -1)
                stopPlayback()
            }
            index > tracks.lastIndex -> {
                state.value = QueueState(tracks, tracks.size)
                stopPlayback()
            }
            else -> {
                state.value = QueueState(tracks, index)
                loadCurrentAndPlay()
            }
        }
    }

    private fun stopPlayback() {
        ranOut = true
        engine.pause()
    }

    private fun skipCurrent() {
        val failed = state.value.current ?: return
        skippedEvents.tryEmit(failed.title)
        next()
    }

    private fun loadCurrentAndPlay() {
        val track = state.value.current ?: return
        ranOut = false
        engine.load(track)
        engine.play()
    }
}
