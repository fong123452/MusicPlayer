package com.example.musicplayer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import musicplayer.core.LibrarySnapshot
import musicplayer.core.Track

const val TRACKS_LIST_TAG = "tracks_list"
const val EMPTY_LIBRARY_MESSAGE = "No music found on this device"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracksScreen(
    snapshot: LibrarySnapshot,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onTrackClick: (index: Int) -> Unit,
    onPlayNext: (Track) -> Unit,
    onAddToQueue: (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh, modifier = modifier.fillMaxSize()) {
        if (snapshot.loaded && snapshot.tracks.isEmpty()) {
            // Scrollable so pull-to-refresh still works on an empty Library.
            Box(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).testTag(TRACKS_LIST_TAG),
                contentAlignment = Alignment.Center,
            ) {
                Text(EMPTY_LIBRARY_MESSAGE, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(32.dp))
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().testTag(TRACKS_LIST_TAG)) {
                itemsIndexed(snapshot.tracks, key = { _, track -> track.id }) { index, track ->
                    TrackRow(
                        track,
                        onClick = { onTrackClick(index) },
                        onPlayNext = { onPlayNext(track) },
                        onAddToQueue = { onAddToQueue(track) },
                    )
                }
            }
        }
    }
}
