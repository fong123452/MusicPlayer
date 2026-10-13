package com.example.musicplayer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import musicplayer.core.Playlist

const val PLAYLISTS_LIST_TAG = "playlists_list"
const val PLAYLIST_NAME_FIELD_TAG = "playlist_name_field"
const val NEW_PLAYLIST_LABEL = "New playlist"
const val CREATE_LABEL = "Create"
const val CANCEL_LABEL = "Cancel"
const val ADD_TO_PLAYLIST_LABEL = "Add to playlist"

fun trackCountLabel(count: Int) = if (count == 1) "1 song" else "$count songs"

@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    onOpen: (Playlist) -> Unit,
    onCreate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var naming by rememberSaveable { mutableStateOf(false) }
    Column(modifier.fillMaxSize()) {
        TextButton(onClick = { naming = true }, Modifier.padding(horizontal = 8.dp)) { Text(NEW_PLAYLIST_LABEL) }
        LazyColumn(Modifier.fillMaxSize().testTag(PLAYLISTS_LIST_TAG)) {
            items(playlists, key = { it.id }) { playlist ->
                Column(
                    Modifier.fillMaxWidth().clickable { onOpen(playlist) }.padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Text(playlist.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                    Text(trackCountLabel(playlist.tracks.size), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
    if (naming) NamePlaylistDialog(onConfirm = { onCreate(it); naming = false }, onDismiss = { naming = false })
}

@Composable
fun NamePlaylistDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    title: String = NEW_PLAYLIST_LABEL,
    confirmLabel: String = CREATE_LABEL,
    initialName: String = "",
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                placeholder = { Text("Name") },
                modifier = Modifier.testTag(PLAYLIST_NAME_FIELD_TAG),
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(CANCEL_LABEL) } },
    )
}

/** Lets the user pick the Playlist to add to, or start a new one. */
@Composable
fun AddToPlaylistDialog(
    playlists: List<Playlist>,
    onPick: (Playlist) -> Unit,
    onCreate: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var naming by rememberSaveable { mutableStateOf(false) }
    if (naming) {
        NamePlaylistDialog(onConfirm = onCreate, onDismiss = onDismiss)
        return
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(ADD_TO_PLAYLIST_LABEL) },
        text = {
            Column {
                TextButton(onClick = { naming = true }) { Text(NEW_PLAYLIST_LABEL) }
                LazyColumn {
                    items(playlists, key = { it.id }) { playlist ->
                        Text(
                            playlist.name,
                            Modifier.fillMaxWidth().clickable { onPick(playlist) }.padding(vertical = 12.dp),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(CANCEL_LABEL) } },
    )
}

const val ALBUM_MENU_DESCRIPTION = "Album options"

/** The overflow menu on an Album row, so a whole Album can be added to a Playlist from any list. */
@Composable
fun AlbumMenuButton(onAddToPlaylist: () -> Unit) {
    var open by remember { mutableStateOf(false) }
    IconButton(onClick = { open = true }) {
        Icon(Icons.Filled.MoreVert, contentDescription = ALBUM_MENU_DESCRIPTION)
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(text = { Text(ADD_TO_PLAYLIST_LABEL) }, onClick = { open = false; onAddToPlaylist() })
        }
    }
}
