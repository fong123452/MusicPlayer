package musicplayer.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class QueueState(val tracks: List<Track>, val currentIndex: Int) {
    val current: Track? get() = tracks.getOrNull(currentIndex)
}

/** Past this position, previous restarts the current Track instead of leaving it. */
const val RESTART_THRESHOLD_MS = 3_000L

/** What happens when a Track or the Queue ends. */
enum class RepeatMode { Off, All, One }

/** The ordered Tracks that play now. It drives the [PlaybackEngine] one Track at a time. */
class Queue(private val engine: PlaybackEngine, scope: CoroutineScope, private val random: Random = Random.Default) {
    // A queued Track, by identity: the same Track can be queued twice, and shuffle must tell the copies apart.
    private class Entry(val track: Track)

    private var entries: List<Entry> = emptyList()

    // The Queue in the order it was given, plus later additions; shuffle off returns to it.
    private var original: List<Entry> = emptyList()

    private val state = MutableStateFlow(QueueState(emptyList(), -1))
    val queue: StateFlow<QueueState> = state

    private val shuffleState = MutableStateFlow(false)
    val shuffle: StateFlow<Boolean> = shuffleState

    private val repeatState = MutableStateFlow(RepeatMode.Off)
    val repeat: StateFlow<RepeatMode> = repeatState

    private var failures = 0
    val isPlaying: StateFlow<Boolean> = engine.isPlaying

    private var ranOut = false

    // Whether playback was asked for. The engine's isPlaying is false while it buffers, so it cannot tell a Track
    // that is about to start from a paused one; reportedPlaying lets an outside pause (notification) clear the intent.
    private var wantsPlay = false
    private var reportedPlaying = false

    private val skippedEvents = MutableSharedFlow<String>(extraBufferCapacity = 16)

    /** One-shot: the title of each Track that could not play and was skipped. */
    val skipped: Flow<String> = skippedEvents

    init {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            engine.isPlaying.collect { playing ->
                if (playing) {
                    reportedPlaying = true
                    wantsPlay = true // an outside play (notification) or recovery from buffering is intent too
                } else if (reportedPlaying) {
                    reportedPlaying = false
                    wantsPlay = false
                }
            }
        }
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            engine.events.collect { event ->
                when (event) {
                    PlaybackEvent.Completed -> completed()
                    is PlaybackEvent.Error -> skipCurrent()
                    PlaybackEvent.ConnectionFailed -> Unit
                }
            }
        }
    }

    /** Replaces the Queue with [tracks] and starts playing the Track at [startIndex]. */
    fun replace(tracks: List<Track>, startIndex: Int) {
        failures = 0
        original = tracks.map(::Entry)
        entries = original
        publish(startIndex)
        if (shuffleState.value) shuffleKeepingCurrent()
        loadCurrentAndPlay()
    }

    /** Turning shuffle on keeps the current Track first and shuffles the rest; off restores the given order. */
    fun setShuffle(on: Boolean) {
        if (on == shuffleState.value) return
        shuffleState.value = on
        if (on) {
            shuffleKeepingCurrent()
        } else {
            val current = currentEntry()
            entries = original
            publish(if (current == null) -1 else entries.indexOf(current))
        }
    }

    fun cycleRepeat() {
        repeatState.value = when (repeatState.value) {
            RepeatMode.Off -> RepeatMode.All
            RepeatMode.All -> RepeatMode.One
            RepeatMode.One -> RepeatMode.Off
        }
    }

    /** Resumes; once the Queue has run out, plays the current Track again, since the engine cannot resume from its end. */
    fun play() {
        if (ranOut) return loadCurrentAndPlay()
        wantsPlay = true
        engine.play()
    }

    fun pause() {
        wantsPlay = false
        engine.pause()
    }

    fun seekTo(positionMs: Long) = engine.seekTo(positionMs)

    fun positionMs(): Long = engine.currentPositionMs()

    /** Moves to the next Track; on the last Track it wraps to the first under Repeat All, otherwise playback stops. */
    fun next() {
        failures = 0
        advance()
    }

    private fun advance() {
        val s = state.value
        val target = when {
            s.currentIndex + 1 <= s.tracks.lastIndex -> s.currentIndex + 1
            repeatState.value == RepeatMode.All && s.tracks.isNotEmpty() -> 0
            else -> return stopPlayback()
        }
        state.value = s.copy(currentIndex = target)
        loadCurrentAndPlay()
    }

    private fun completed() {
        failures = 0
        if (repeatState.value == RepeatMode.One) loadCurrentAndPlay() else advance()
    }

    /** Restarts the current Track once it has played past [RESTART_THRESHOLD_MS], otherwise moves back. */
    fun previous(positionMs: Long) {
        failures = 0
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
        val entry = Entry(track)
        entries = entries.toMutableList().apply { add(s.currentIndex + 1, entry) }
        original = original.toMutableList().apply { add(indexOf(currentEntry()) + 1, entry) }
        publish(s.currentIndex)
    }

    /** Appends [track] to the end; on an empty Queue it starts playing. */
    fun addToEnd(track: Track) {
        val s = state.value
        if (s.tracks.isEmpty()) return replace(listOf(track), 0)
        val entry = Entry(track)
        entries = entries + entry
        original = original + entry
        publish(s.currentIndex)
    }

    /** Moves the Track at [from] to [to]. The current Track keeps playing; indices outside the Queue are ignored. */
    fun move(from: Int, to: Int) {
        val s = state.value
        if (from !in s.tracks.indices || to !in s.tracks.indices || from == to) return
        entries = entries.toMutableList().apply { add(to, removeAt(from)) }
        if (!shuffleState.value) original = entries // a reorder is the new given order; shuffled moves stay out of it
        val current = when {
            s.currentIndex == from -> to
            from < s.currentIndex && to >= s.currentIndex -> s.currentIndex - 1
            from > s.currentIndex && to <= s.currentIndex -> s.currentIndex + 1
            else -> s.currentIndex
        }
        publish(current)
    }

    /**
     * Removing the current Track moves playback to the one after it (paused if it was paused). When none follows,
     * playback stops and the last remaining Track is current. Removing any other Track keeps the same Track playing.
     */
    fun remove(index: Int) {
        val s = state.value
        if (index !in s.tracks.indices) return
        failures = 0
        val removed = entries[index]
        entries = entries.toMutableList().apply { removeAt(index) }
        original = original - removed
        val lastIndex = entries.lastIndex
        when {
            index < s.currentIndex -> publish(s.currentIndex - 1)
            index > s.currentIndex -> publish(s.currentIndex)
            entries.isEmpty() -> {
                publish(-1)
                stopPlayback()
            }
            index > lastIndex -> {
                // Nothing follows: the last remaining Track becomes current, paused, so the Queue stays reachable.
                publish(lastIndex)
                loadCurrentPaused()
            }
            else -> {
                val resume = wantsPlay
                publish(index)
                if (resume) loadCurrentAndPlay() else loadCurrentPaused()
            }
        }
    }

    private fun stopPlayback() {
        ranOut = true
        wantsPlay = false
        engine.pause()
    }

    private fun skipCurrent() {
        val failed = state.value.current ?: return
        skippedEvents.tryEmit(failed.title)
        // Under Repeat All, a Queue where every Track fails would otherwise skip around it forever.
        if (++failures >= entries.size) return stopPlayback()
        advance()
    }

    private fun currentEntry(): Entry? = entries.getOrNull(state.value.currentIndex)

    private fun publish(currentIndex: Int) {
        state.value = QueueState(entries.map { it.track }, currentIndex)
    }

    private fun shuffleKeepingCurrent() {
        val current = currentEntry() ?: return
        entries = listOf(current) + (entries - current).shuffled(random)
        publish(0)
    }

    private fun loadCurrentPaused() {
        val track = state.value.current ?: return
        ranOut = false
        wantsPlay = false
        engine.load(track)
        engine.pause() // a player that was playing would otherwise start the loaded Track on its own
    }

    private fun loadCurrentAndPlay() {
        val track = state.value.current ?: return
        ranOut = false
        engine.load(track)
        // After load: the engine's own pause report for the old Track must not clear this intent.
        reportedPlaying = false
        wantsPlay = true
        engine.play()
    }
}
