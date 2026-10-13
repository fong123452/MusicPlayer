package com.example.musicplayer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import musicplayer.core.Album
import musicplayer.core.Artist
import musicplayer.core.Track

const val ARTISTS_LIST_TAG = "artists_list"
const val ARTIST_PAGE_TAG = "artist_page"
const val PLAY_ARTIST_LABEL = "Play artist"

@Composable
fun ArtistsScreen(artists: List<Artist>, onArtistClick: (Artist) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize().testTag(ARTISTS_LIST_TAG)) {
        items(artists, key = { it.key }) { artist ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .clickable { onArtistClick(artist) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(artist.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                Text(
                    "${artist.albums.size} albums • ${artist.tracks.size} songs",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun ArtistDetailScreen(
    artist: Artist,
    onPlay: (startIndex: Int) -> Unit,
    onPlayNext: (Track) -> Unit,
    onAddToQueue: (Track) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onClose)
    Column(modifier.fillMaxSize()) {
        IconButton(onClick = onClose) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = BACK_DESCRIPTION)
        }
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text(artist.name, style = MaterialTheme.typography.titleLarge, maxLines = 2)
            if (artist.tracks.isNotEmpty()) {
                Button(onClick = { onPlay(0) }, Modifier.padding(top = 8.dp)) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Text(PLAY_ARTIST_LABEL)
                }
            }
        }
        LazyColumn(Modifier.fillMaxSize().testTag(ARTIST_PAGE_TAG)) {
            if (artist.albums.isNotEmpty()) {
                item(key = "albums-header") { SectionHeader("Albums") }
                items(artist.albums, key = { "album-${it.key}" }) { album ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onAlbumClick(album) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AlbumArt(album.albumId, 56.dp, ROW_ART_PLACEHOLDER_TAG)
                        Text(album.title, Modifier.padding(start = 12.dp), style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                    }
                }
            }
            if (artist.tracks.isNotEmpty()) {
                item(key = "tracks-header") { SectionHeader("Songs") }
                itemsIndexed(artist.tracks, key = { _, track -> "track-${track.id}" }) { index, track ->
                    TrackRow(
                        track,
                        onClick = { onPlay(index) },
                        onPlayNext = { onPlayNext(track) },
                        onAddToQueue = { onAddToQueue(track) },
                        onAddToPlaylist = { onAddToPlaylist(track) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}

private fun pluralOf(count: Int, noun: String) = if (count == 1) "1 $noun" else "$count ${noun}s"
