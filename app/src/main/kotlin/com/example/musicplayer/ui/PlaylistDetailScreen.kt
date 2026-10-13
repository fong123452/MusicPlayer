package com.example.musicplayer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import musicplayer.core.Playlist
import musicplayer.core.Track

const val PLAYLIST_TRACKS_TAG = "playlist_tracks"
const val PLAY_PLAYLIST_LABEL = "Play playlist"
const val RENAME_LABEL = "Rename"
const val DELETE_LABEL = "Delete"
const val REMOVE_FROM_PLAYLIST_DESCRIPTION = "Remove from playlist"
const val EMPTY_PLAYLIST_TEXT = "No songs yet. Use \"$ADD_TO_PLAYLIST_LABEL\" on a song or album."

/** One Playlist: play it, rename or delete it, and reorder or remove its Tracks. */
@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    onPlayAll: () -> Unit,
    onPlay: (startIndex: Int) -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onMove: (from: Int, to: Int) -> Unit,
    onRemove: (Track) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onClose)
    var renaming by rememberSaveable { mutableStateOf(false) }
    var confirmingDelete by rememberSaveable { mutableStateOf(false) }
    Column(modifier.fillMaxSize()) {
        IconButton(onClick = onClose) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = BACK_DESCRIPTION)
        }
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text(playlist.name, style = MaterialTheme.typography.titleLarge, maxLines = 2)
            Text(trackCountLabel(playlist.tracks.size), style = MaterialTheme.typography.bodySmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onPlayAll, enabled = playlist.tracks.isNotEmpty()) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Text(PLAY_PLAYLIST_LABEL)
                }
                TextButton(onClick = { renaming = true }) { Text(RENAME_LABEL) }
                TextButton(onClick = { confirmingDelete = true }) { Text(DELETE_LABEL) }
            }
        }
        if (playlist.tracks.isEmpty()) {
            Text(EMPTY_PLAYLIST_TEXT, Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
        }
        ReorderableList(
            items = playlist.tracks,
            key = { _, track -> track.id },
            onMove = onMove,
            modifier = Modifier.fillMaxSize().testTag(PLAYLIST_TRACKS_TAG),
        ) { index, track, handle ->
            Row(
                Modifier.fillMaxWidth().clickable { onPlay(index) }.padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                    Text(track.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                    Text(track.artist, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
                IconButton(onClick = { onRemove(track) }) {
                    Icon(Icons.Filled.Close, contentDescription = REMOVE_FROM_PLAYLIST_DESCRIPTION)
                }
                Icon(Icons.Filled.Menu, contentDescription = REORDER_DESCRIPTION, modifier = handle.padding(end = 16.dp))
            }
        }
    }
    if (renaming) {
        NamePlaylistDialog(
            onConfirm = { onRename(it); renaming = false },
            onDismiss = { renaming = false },
            title = RENAME_LABEL,
            confirmLabel = RENAME_LABEL,
            initialName = playlist.name,
        )
    }
    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text("Delete \"${playlist.name}\"?") },
            text = { Text("The songs stay on your phone.") },
            confirmButton = { TextButton(onClick = { confirmingDelete = false; onDelete() }) { Text(DELETE_LABEL) } },
            dismissButton = { TextButton(onClick = { confirmingDelete = false }) { Text(CANCEL_LABEL) } },
        )
    }
}
