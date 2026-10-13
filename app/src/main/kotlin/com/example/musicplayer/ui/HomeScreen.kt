package com.example.musicplayer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.launch
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
import musicplayer.core.Track

enum class HomeTab(val label: String) {
    Tracks("Songs"),
    Albums("Albums"),
    Artists("Artists"),
    Playlists("Playlists"),
}

@Composable
fun HomeScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    player: PlayerViewModel = hiltViewModel(),
    playlistsViewModel: PlaylistsViewModel = hiltViewModel(),
    onTrackPlayed: () -> Unit = {},
) {
    val snapshot by viewModel.snapshot.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val queueState by player.queueState.collectAsState()
    val isPlaying by player.isPlaying.collectAsState()
    val shuffle by player.shuffle.collectAsState()
    val repeat by player.repeat.collectAsState()
    val playlists by playlistsViewModel.playlists.collectAsState()
    // Only ids are saved, so the dialog survives rotation; the Tracks are looked up again in the current Library.
    var pendingIds by rememberSaveable { mutableStateOf<ArrayList<Long>?>(null) }
    val pendingAdd = pendingIds?.let { ids ->
        val byId = snapshot.tracks.associateBy { it.id }
        ids.mapNotNull(byId::get)
    }
    fun addToPlaylist(tracks: List<Track>) { pendingIds = ArrayList(tracks.map { it.id }) }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var openAlbumId by rememberSaveable { mutableStateOf<String?>(null) }
    val openAlbum = snapshot.albums.firstOrNull { it.key == openAlbumId }
    // An Album that a refresh removed (or that never came back after a restart) must not leave a stale id behind.
    LaunchedEffect(snapshot) { if (snapshot.loaded && openAlbum == null) openAlbumId = null }
    var openArtistKey by rememberSaveable { mutableStateOf<String?>(null) }
    val openArtist = snapshot.artists.firstOrNull { it.key == openArtistKey }
    LaunchedEffect(snapshot) { if (snapshot.loaded && openArtist == null) openArtistKey = null }
    var openPlaylistId by rememberSaveable { mutableStateOf<Long?>(null) }
    val openPlaylist = playlists.firstOrNull { it.id == openPlaylistId }
    // A Playlist that was deleted must not leave a stale id behind. Playlists load asynchronously, so wait for the Library.
    LaunchedEffect(playlists, snapshot.loaded) { if (snapshot.loaded && openPlaylist == null) openPlaylistId = null }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showNowPlaying by rememberSaveable { mutableStateOf(false) }
    // With nothing current, Now Playing is hidden; clear the flag so it does not reappear when a Track is queued later.
    LaunchedEffect(queueState.current == null) { if (queueState.current == null) showNowPlaying = false }
    val snackbarHost = remember { SnackbarHostState() }
    LaunchedEffect(player) {
        // Each message gets its own coroutine, so a new failure replaces the one on screen instead of queueing behind it.
        player.skipped.collect { title ->
            snackbarHost.currentSnackbarData?.dismiss()
            launch { snackbarHost.showSnackbar("Couldn't play $title, skipped") }
        }
    }
    LaunchedEffect(playlistsViewModel) {
        playlistsViewModel.messages.collect { message ->
            snackbarHost.currentSnackbarData?.dismiss()
            launch { snackbarHost.showSnackbar(message) }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            queueState.current?.let { MiniPlayer(it, isPlaying, player::togglePlayPause, onOpen = { showNowPlaying = true }) }
            if (openAlbum != null) {
                AlbumDetailScreen(
                    album = openAlbum,
                    onPlay = { index ->
                        player.playFrom(openAlbum.tracks, index)
                        onTrackPlayed()
                    },
                    onPlayNext = player::playNext,
                    onAddToQueue = player::addToQueue,
                    onAddToPlaylist = { addToPlaylist(listOf(it)) },
                    onAddAlbumToPlaylist = { addToPlaylist(openAlbum.tracks) },
                    onClose = { openAlbumId = null },
                )
            } else if (openArtist != null) {
                ArtistDetailScreen(
                    artist = openArtist,
                    onPlay = { index ->
                        player.playFrom(openArtist.tracks, index)
                        onTrackPlayed()
                    },
                    onPlayNext = player::playNext,
                    onAddToQueue = player::addToQueue,
                    onAddToPlaylist = { addToPlaylist(listOf(it)) },
                    onAlbumClick = { openAlbumId = it.key },
                    onAddAlbumToPlaylist = { addToPlaylist(it.tracks) },
                    onClose = { openArtistKey = null },
                )
            } else if (openPlaylist != null) {
                PlaylistDetailScreen(
                    playlist = openPlaylist,
                    onPlayAll = { if (playlistsViewModel.play(openPlaylist)) onTrackPlayed() },
                    onPlay = { index ->
                        player.playFrom(openPlaylist.tracks, index)
                        onTrackPlayed()
                    },
                    onRename = { playlistsViewModel.rename(openPlaylist, it) },
                    onDelete = { playlistsViewModel.delete(openPlaylist) },
                    onMove = { from, to -> playlistsViewModel.move(openPlaylist, from, to) },
                    onRemove = { playlistsViewModel.remove(openPlaylist, it) },
                    onClose = { openPlaylistId = null },
                )
            } else if (showSearch) {
                SearchScreen(
                    snapshot = snapshot,
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onTrackClick = { results, index ->
                        player.playFrom(results, index)
                        onTrackPlayed()
                    },
                    onPlayNext = player::playNext,
                    onAddToQueue = player::addToQueue,
                    onAddToPlaylist = { addToPlaylist(listOf(it)) },
                    onAlbumClick = { openAlbumId = it.key },
                    onAddAlbumToPlaylist = { addToPlaylist(it.tracks) },
                    onArtistClick = { openArtistKey = it.key },
                    onClose = { showSearch = false; searchQuery = "" },
                )
            } else {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    PrimaryTabRow(selectedTabIndex = selected, modifier = Modifier.weight(1f)) {
                        HomeTab.entries.forEachIndexed { index, tab ->
                            Tab(selected = selected == index, onClick = { selected = index }, text = { Text(tab.label) })
                        }
                    }
                    IconButton(onClick = { showSearch = true }) {
                        Icon(Icons.Filled.Search, contentDescription = SEARCH_DESCRIPTION)
                    }
                }
                when (HomeTab.entries[selected]) {
                    HomeTab.Tracks -> TracksScreen(
                        snapshot = snapshot,
                        isRefreshing = isRefreshing,
                        onRefresh = viewModel::refresh,
                        onTrackClick = { index ->
                            player.playFrom(snapshot.tracks, index)
                            onTrackPlayed()
                        },
                        onPlayNext = player::playNext,
                        onAddToQueue = player::addToQueue,
                        onAddToPlaylist = { addToPlaylist(listOf(it)) },
                    )
                    HomeTab.Albums -> AlbumsScreen(
                        snapshot.albums,
                        onAlbumClick = { openAlbumId = it.key },
                        onAddToPlaylist = { addToPlaylist(it.tracks) },
                    )
                    HomeTab.Artists -> ArtistsScreen(snapshot.artists, onArtistClick = { openArtistKey = it.key })
                    HomeTab.Playlists -> PlaylistsScreen(
                        playlists = playlists,
                        onOpen = { openPlaylistId = it.id },
                        onCreate = playlistsViewModel::create,
                    )
                }
            }
        }
        if (showNowPlaying && queueState.current != null) {
            Surface(Modifier.fillMaxSize()) {
                NowPlayingScreen(
                    queueState = queueState,
                    isPlaying = isPlaying,
                    positionMs = player::positionMs,
                    onTogglePlayPause = player::togglePlayPause,
                    onNext = player::next,
                    onPrevious = player::previous,
                    shuffle = shuffle,
                    repeat = repeat,
                    onToggleShuffle = player::toggleShuffle,
                    onCycleRepeat = player::cycleRepeat,
                    onSeek = player::seekTo,
                    onMove = player::moveInQueue,
                    onRemove = player::removeFromQueue,
                    onClose = { showNowPlaying = false },
                )
            }
        }
        pendingAdd?.let { tracks ->
            AddToPlaylistDialog(
                playlists = playlists,
                onPick = { playlist ->
                    playlistsViewModel.add(playlist, tracks)
                    pendingIds = null
                },
                onCreate = { name ->
                    playlistsViewModel.createAndAdd(name, tracks)
                    pendingIds = null
                },
                onDismiss = { pendingIds = null },
            )
        }
        SnackbarHost(snackbarHost, Modifier.align(Alignment.BottomCenter))
    }
}
