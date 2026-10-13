package com.example.musicplayer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val playlists: Playlists,
    private val queue: Queue,
) : ViewModel() {
    val all: StateFlow<List<Playlist>> =
        playlists.playlists.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun create(name: String) {
        viewModelScope.launch { playlists.create(name.trim()) }
    }

    /** Creates a Playlist named [name] and adds [tracks] to it. [onResult] gets the message to show. */
    fun createAndAdd(name: String, tracks: List<Track>, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val id = playlists.create(name.trim())
            onResult(playlists.add(id, tracks).describe(name.trim()))
        }
    }

    fun add(playlist: Playlist, tracks: List<Track>, onResult: (String) -> Unit) {
        viewModelScope.launch { onResult(playlists.add(playlist.id, tracks).describe(playlist.name)) }
    }

    fun play(playlist: Playlist) = playlists.play(playlist, queue)
}

/** The short confirmation shown after adding Tracks to the Playlist called [playlistName]. */
fun AddResult.describe(playlistName: String): String = when {
    added.isEmpty() && alreadyThere.size == 1 -> "Already in $playlistName"
    added.isEmpty() -> "All ${alreadyThere.size} songs already in $playlistName"
    alreadyThere.isEmpty() -> "Added to $playlistName"
    else -> "Added ${added.size} to $playlistName, ${alreadyThere.size} already there"
}
