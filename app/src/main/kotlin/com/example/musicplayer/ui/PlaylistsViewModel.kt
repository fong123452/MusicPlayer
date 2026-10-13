package com.example.musicplayer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import musicplayer.core.AddResult
import musicplayer.core.Playlist
import musicplayer.core.Playlists
import musicplayer.core.Queue
import musicplayer.core.Track
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val core: Playlists,
    private val queue: Queue,
) : ViewModel() {
    val playlists: StateFlow<List<Playlist>> =
        core.playlists.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val messageEvents = MutableSharedFlow<String>(extraBufferCapacity = 16)

    /** Short confirmations of adds, one per add, even when the screen was recreated while it ran. */
    val messages: Flow<String> = messageEvents

    fun create(name: String) {
        viewModelScope.launch { core.create(name) }
    }

    /** Creates a Playlist named [name] and adds [tracks] to it. */
    fun createAndAdd(name: String, tracks: List<Track>) {
        viewModelScope.launch {
            val id = core.create(name)
            messageEvents.emit(core.add(id, tracks).describe(name.trim()))
        }
    }

    fun add(playlist: Playlist, tracks: List<Track>) {
        viewModelScope.launch { messageEvents.emit(core.add(playlist.id, tracks).describe(playlist.name)) }
    }

    fun rename(playlist: Playlist, name: String) {
        viewModelScope.launch { core.rename(playlist.id, name) }
    }

    fun delete(playlist: Playlist) {
        viewModelScope.launch { core.delete(playlist.id) }
    }

    fun remove(playlist: Playlist, track: Track) {
        viewModelScope.launch { core.remove(playlist.id, track) }
    }

    fun move(playlist: Playlist, from: Int, to: Int) {
        viewModelScope.launch { core.move(playlist.id, from, to) }
    }

    /** Returns false, playing nothing, when the Playlist has no Tracks. */
    fun play(playlist: Playlist): Boolean = core.play(playlist, queue)
}

/** The short confirmation shown after adding Tracks to the Playlist called [playlistName]. */
fun AddResult.describe(playlistName: String): String = when {
    added.isEmpty() && alreadyThere.size == 1 -> "Already in $playlistName"
    added.isEmpty() -> "All ${alreadyThere.size} songs already in $playlistName"
    alreadyThere.isEmpty() -> "Added to $playlistName"
    else -> "Added ${added.size} to $playlistName, ${alreadyThere.size} already there"
}
