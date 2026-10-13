package com.example.musicplayer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import musicplayer.core.Album
import musicplayer.core.Artist
import musicplayer.core.LibrarySnapshot
import musicplayer.core.Track
import musicplayer.core.search

const val SEARCH_FIELD_TAG = "search_field"
const val SEARCH_RESULTS_TAG = "search_results"
const val SEARCH_DESCRIPTION = "Search"
const val NO_RESULTS_MESSAGE = "No results"
const val TRACKS_HEADER = "Tracks"
const val ALBUMS_HEADER = "Albums"
const val ARTISTS_HEADER = "Artists"

@Composable
fun SearchScreen(
    snapshot: LibrarySnapshot,
    query: String,
    onQueryChange: (String) -> Unit,
    onTrackClick: (results: List<Track>, index: Int) -> Unit,
    onPlayNext: (Track) -> Unit,
    onAddToQueue: (Track) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onClose)
    val results = remember(snapshot, query) { snapshot.search(query) }
    Column(modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = BACK_DESCRIPTION)
            }
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f).padding(end = 16.dp).testTag(SEARCH_FIELD_TAG),
                placeholder = { Text("Search songs, albums, artists") },
                singleLine = true,
            )
        }
        when {
            query.isBlank() || !snapshot.loaded -> Unit
            results.isEmpty -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(NO_RESULTS_MESSAGE) }
            else -> LazyColumn(Modifier.fillMaxSize().testTag(SEARCH_RESULTS_TAG)) {
                if (results.tracks.isNotEmpty()) {
                    item(key = "tracks-header") { ResultHeader(TRACKS_HEADER) }
                    itemsIndexed(results.tracks, key = { _, track -> "track-${track.id}" }) { index, track ->
                        TrackRow(
                            track,
                            onClick = { onTrackClick(results.tracks, index) },
                            onPlayNext = { onPlayNext(track) },
                            onAddToQueue = { onAddToQueue(track) },
                        onAddToPlaylist = { onAddToPlaylist(track) },
                        )
                    }
                }
                if (results.albums.isNotEmpty()) {
                    item(key = "albums-header") { ResultHeader(ALBUMS_HEADER) }
                    items(results.albums, key = { "album-${it.key}" }) { album ->
                        Row(
                            Modifier.fillMaxWidth().clickable { onAlbumClick(album) }.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AlbumArt(album.albumId, 48.dp, ROW_ART_PLACEHOLDER_TAG)
                            Column(Modifier.padding(start = 12.dp)) {
                                Text(album.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                                Text(album.albumArtist, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            }
                        }
                    }
                }
                if (results.artists.isNotEmpty()) {
                    item(key = "artists-header") { ResultHeader(ARTISTS_HEADER) }
                    items(results.artists, key = { "artist-${it.key}" }) { artist ->
                        Text(
                            artist.name,
                            Modifier.fillMaxWidth().clickable { onArtistClick(artist) }.padding(horizontal = 16.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultHeader(text: String) {
    Text(
        text,
        Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}
