package com.example.musicplayer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import musicplayer.core.LibrarySnapshot
import musicplayer.core.Track
import musicplayer.core.search
import musicplayer.core.tracksToAdd

const val ADD_SONGS_LABEL = "Add songs"
const val DONE_LABEL = "Done"
const val ADD_SONGS_SEARCH_TAG = "add_songs_search"
const val ADD_SONGS_LIST_TAG = "add_songs_list"

/**
 * A checklist of every Track in the Library, with search. Tracks in [alreadyIn] show ticked and cannot be unticked.
 * Done hands the newly ticked Tracks, in Library order, to [onDone]; with none ticked it just closes.
 */
@Composable
fun AddSongsScreen(
    snapshot: LibrarySnapshot,
    alreadyIn: Set<Long>,
    onDone: (List<Track>) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onClose)
    var query by rememberSaveable { mutableStateOf("") }
    // Only ids are saved, so the ticks survive rotation; Tracks are looked up again in the current Library.
    var tickedIds by rememberSaveable { mutableStateOf<List<Long>>(ArrayList()) }
    val ticked = tickedIds.toSet()
    val shown = remember(snapshot, query) {
        if (query.isBlank()) snapshot.tracks else snapshot.search(query).tracks
    }
    Column(modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = BACK_DESCRIPTION)
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f).testTag(ADD_SONGS_SEARCH_TAG),
                placeholder = { Text("Search songs") },
                singleLine = true,
            )
            TextButton(
                onClick = { onDone(tracksToAdd(snapshot.tracks, ticked, alreadyIn)) },
                modifier = Modifier.padding(end = 8.dp),
            ) { Text(DONE_LABEL) }
        }
        LazyColumn(Modifier.fillMaxSize().testTag(ADD_SONGS_LIST_TAG)) {
            items(shown, key = { it.id }) { track ->
                val locked = track.id in alreadyIn
                val checked = locked || track.id in ticked
                Row(
                    Modifier.fillMaxWidth()
                        .toggleable(
                            value = checked,
                            enabled = !locked,
                            role = Role.Checkbox,
                            onValueChange = { on ->
                                tickedIds = if (on) tickedIds + track.id else tickedIds - track.id
                            },
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = checked, onCheckedChange = null, enabled = !locked)
                    Column(Modifier.weight(1f).padding(start = 16.dp, top = 8.dp, bottom = 8.dp)) {
                        Text(track.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                        Text(track.artist, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    }
                }
            }
        }
    }
}
