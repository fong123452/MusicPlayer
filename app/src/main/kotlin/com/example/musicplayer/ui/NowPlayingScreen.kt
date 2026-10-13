package com.example.musicplayer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import musicplayer.core.QueueState
import musicplayer.core.Track

const val NOW_PLAYING_TAG = "now_playing"
const val ART_PLACEHOLDER_TAG = "art_placeholder"
const val SEEK_BAR_TAG = "seek_bar"
const val QUEUE_LIST_TAG = "queue_list"
const val NEXT_DESCRIPTION = "Next"
const val PREVIOUS_DESCRIPTION = "Previous"
const val BACK_DESCRIPTION = "Back"

private const val POSITION_POLL_MS = 500L

@Composable
fun NowPlayingScreen(
    queueState: QueueState,
    isPlaying: Boolean,
    positionMs: () -> Long,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onClose)
    val track = queueState.current
    Column(modifier.fillMaxSize().testTag(NOW_PLAYING_TAG)) {
        IconButton(onClick = onClose) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = BACK_DESCRIPTION)
        }
        if (track != null) {
            Header(track)
            SeekBar(track, isPlaying, positionMs, onSeek)
            Controls(isPlaying, onTogglePlayPause, onNext, onPrevious)
        }
        QueueList(queueState, Modifier.weight(1f))
    }
}

@Composable
private fun Header(track: Track) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        AlbumArt(track.albumId, 200.dp, ART_PLACEHOLDER_TAG)
        Text(
            track.title,
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 1,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(track.artist, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
    }
}

@Composable
private fun SeekBar(track: Track, isPlaying: Boolean, positionMs: () -> Long, onSeek: (Long) -> Unit) {
    var position by remember(track) { mutableLongStateOf(0L) }
    var dragFraction by remember(track) { mutableStateOf<Float?>(null) }
    LaunchedEffect(track, isPlaying) {
        while (true) {
            position = positionMs()
            if (!isPlaying) break
            delay(POSITION_POLL_MS)
        }
    }
    val duration = track.durationMs.coerceAtLeast(1)
    val fraction = dragFraction ?: (position.toFloat() / duration).coerceIn(0f, 1f)
    Slider(
        value = fraction,
        onValueChange = { dragFraction = it },
        onValueChangeFinished = {
            dragFraction?.let { final ->
                val target = (final * duration).toLong()
                position = target
                onSeek(target)
            }
            dragFraction = null
        },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag(SEEK_BAR_TAG),
    )
    Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(formatTime((fraction * duration).toLong()), style = MaterialTheme.typography.bodySmall)
        Text(formatTime(track.durationMs), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun Controls(isPlaying: Boolean, onTogglePlayPause: () -> Unit, onNext: () -> Unit, onPrevious: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrevious) {
            Icon(SkipPreviousIcon, contentDescription = PREVIOUS_DESCRIPTION)
        }
        IconButton(onClick = onTogglePlayPause) {
            Icon(
                imageVector = if (isPlaying) PauseIcon else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) PAUSE_DESCRIPTION else PLAY_DESCRIPTION,
                modifier = Modifier.size(40.dp),
            )
        }
        IconButton(onClick = onNext) {
            Icon(SkipNextIcon, contentDescription = NEXT_DESCRIPTION)
        }
    }
}

@Composable
private fun QueueList(queueState: QueueState, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxWidth().testTag(QUEUE_LIST_TAG)) {
        itemsIndexed(queueState.tracks, key = { index, track -> "$index-${track.id}" }) { index, track ->
            val isCurrent = index == queueState.currentIndex
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    track.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                )
                Text(track.artist, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
        }
    }
}

internal fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
