package com.example.musicplayer.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import musicplayer.core.Queue
import musicplayer.core.QueueState
import musicplayer.core.RepeatMode
import musicplayer.core.Track
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val queue: Queue,
) : ViewModel() {
    val queueState: StateFlow<QueueState> = queue.queue
    val isPlaying: StateFlow<Boolean> = queue.isPlaying
    val shuffle: StateFlow<Boolean> = queue.shuffle
    val repeat: StateFlow<RepeatMode> = queue.repeat

    /** Titles of Tracks that failed to play and were skipped, one per failure. */
    val skipped: Flow<String> = queue.skipped

    fun playFrom(tracks: List<Track>, index: Int) = queue.replace(tracks, index)

    fun playNext(track: Track) = queue.playNext(track)

    fun addToQueue(track: Track) = queue.addToEnd(track)

    fun moveInQueue(from: Int, to: Int) = queue.move(from, to)

    fun removeFromQueue(index: Int) = queue.remove(index)

    fun toggleShuffle() = queue.setShuffle(!queue.shuffle.value)

    fun cycleRepeat() = queue.cycleRepeat()

    fun togglePlayPause() = if (queue.isPlaying.value) queue.pause() else queue.play()

    fun next() = queue.next()

    fun previous() = queue.previous(queue.positionMs())

    fun seekTo(positionMs: Long) = queue.seekTo(positionMs)

    fun positionMs(): Long = queue.positionMs()
}
