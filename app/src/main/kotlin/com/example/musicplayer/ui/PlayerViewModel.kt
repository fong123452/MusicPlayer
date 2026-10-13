package com.example.musicplayer.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import musicplayer.core.Queue
import musicplayer.core.QueueState
import musicplayer.core.Track
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val queue: Queue,
) : ViewModel() {
    val queueState: StateFlow<QueueState> = queue.queue
    val isPlaying: StateFlow<Boolean> = queue.isPlaying

    /** Titles of Tracks that failed to play and were skipped, one per failure. */
    val skipped: Flow<String> = queue.skipped

    fun playFrom(tracks: List<Track>, index: Int) = queue.replace(tracks, index)

    fun togglePlayPause() = if (queue.isPlaying.value) queue.pause() else queue.play()

    fun next() = queue.next()

    fun previous() = queue.previous(queue.positionMs())

    fun seekTo(positionMs: Long) = queue.seekTo(positionMs)

    fun positionMs(): Long = queue.positionMs()
}
