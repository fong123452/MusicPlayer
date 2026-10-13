package com.example.musicplayer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import musicplayer.core.Track

const val TRACK_MENU_DESCRIPTION = "More options"
const val PLAY_NEXT_LABEL = "Play next"
const val ADD_TO_QUEUE_LABEL = "Add to queue"

@Composable
fun TrackRow(
    track: Track,
    onClick: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AlbumArt(track.albumId, 48.dp, ROW_ART_PLACEHOLDER_TAG)
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text(track.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
            Text("${track.artist} • ${track.album}", style = MaterialTheme.typography.bodySmall, maxLines = 1)
        }
        var menuOpen by remember { mutableStateOf(false) }
        IconButton(onClick = { menuOpen = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = TRACK_MENU_DESCRIPTION)
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text(PLAY_NEXT_LABEL) },
                    onClick = { menuOpen = false; onPlayNext() },
                )
                DropdownMenuItem(
                    text = { Text(ADD_TO_QUEUE_LABEL) },
                    onClick = { menuOpen = false; onAddToQueue() },
                )
            }
        }
    }
}
