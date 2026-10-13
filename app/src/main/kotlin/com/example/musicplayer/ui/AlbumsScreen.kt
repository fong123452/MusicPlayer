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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import musicplayer.core.Album
import musicplayer.core.Track

const val ALBUMS_LIST_TAG = "albums_list"
const val ALBUM_TRACKS_TAG = "album_tracks"
const val PLAY_ALBUM_LABEL = "Play album"

@Composable
fun AlbumsScreen(
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit,
    onAddToPlaylist: (Album) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier.fillMaxSize().testTag(ALBUMS_LIST_TAG)) {
        items(albums, key = { it.key }) { album ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onAlbumClick(album) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AlbumArt(album.albumId, 56.dp, ROW_ART_PLACEHOLDER_TAG)
                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(album.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                    Text(album.albumArtist, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
                AlbumMenuButton { onAddToPlaylist(album) }
            }
        }
    }
}

@Composable
fun AlbumDetailScreen(
    album: Album,
    onPlay: (startIndex: Int) -> Unit,
    onPlayNext: (Track) -> Unit,
    onAddToQueue: (Track) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
    onAddAlbumToPlaylist: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onClose)
    Column(modifier.fillMaxSize()) {
        IconButton(onClick = onClose) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = BACK_DESCRIPTION)
        }
        Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            AlbumArt(album.albumId, 96.dp, ROW_ART_PLACEHOLDER_TAG)
            Column(Modifier.padding(start = 16.dp)) {
                Text(album.title, style = MaterialTheme.typography.titleLarge, maxLines = 2)
                Text(album.albumArtist, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                Button(onClick = { onPlay(0) }, Modifier.padding(top = 8.dp)) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Text(PLAY_ALBUM_LABEL)
                }
                TextButton(onClick = onAddAlbumToPlaylist) { Text(ADD_TO_PLAYLIST_LABEL) }
            }
        }
        LazyColumn(Modifier.fillMaxSize().testTag(ALBUM_TRACKS_TAG)) {
            itemsIndexed(album.tracks, key = { _, track -> track.id }) { index, track ->
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
