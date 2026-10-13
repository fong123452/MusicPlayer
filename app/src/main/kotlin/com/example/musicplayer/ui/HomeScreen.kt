package com.example.musicplayer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

enum class HomeTab(val label: String) {
    Songs("Songs"),
    Albums("Albums"),
    Artists("Artists"),
    Playlists("Playlists"),
}

@Composable
fun HomeScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    player: PlayerViewModel = hiltViewModel(),
    onTrackPlayed: () -> Unit = {},
) {
    val snapshot by viewModel.snapshot.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val queueState by player.queueState.collectAsState()
    val isPlaying by player.isPlaying.collectAsState()
    var selected by rememberSaveable { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        queueState.current?.let { MiniPlayer(it, isPlaying, player::togglePlayPause) }
        PrimaryTabRow(selectedTabIndex = selected) {
            HomeTab.entries.forEachIndexed { index, tab ->
                Tab(selected = selected == index, onClick = { selected = index }, text = { Text(tab.label) })
            }
        }
        when (HomeTab.entries[selected]) {
            HomeTab.Songs -> SongsScreen(
                snapshot = snapshot,
                isRefreshing = isRefreshing,
                onRefresh = viewModel::refresh,
                onTrackClick = { index ->
                    player.playFrom(snapshot.tracks, index)
                    onTrackPlayed()
                },
            )
            else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Coming soon")
            }
        }
    }
}
