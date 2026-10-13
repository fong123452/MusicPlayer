package com.example.musicplayer.playback

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import musicplayer.core.PlaybackEngine
import musicplayer.core.PlaybackEvent
import musicplayer.core.Track
import javax.inject.Inject
import javax.inject.Singleton

/** Real [PlaybackEngine]: drives the ExoPlayer inside [PlaybackService] through a Media3 controller. Main thread only. */
@Singleton
class ControllerPlaybackEngine @Inject constructor(
    @ApplicationContext context: Context,
) : PlaybackEngine {
    private val playing = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = playing

    private val eventFlow = MutableSharedFlow<PlaybackEvent>(extraBufferCapacity = 16)
    override val events = eventFlow

    private val mainExecutor = ContextCompat.getMainExecutor(context)
    private val controllerFuture = MediaController.Builder(
        context,
        SessionToken(context, ComponentName(context, PlaybackService::class.java)),
    ).buildAsync()

    init {
        withController { controller ->
            playing.value = controller.isPlaying
            controller.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    playing.value = isPlaying
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) eventFlow.tryEmit(PlaybackEvent.Completed)
                }

                override fun onPlayerError(error: PlaybackException) {
                    eventFlow.tryEmit(PlaybackEvent.Error(error.message))
                }
            })
        }
    }

    override fun load(track: Track) = withController {
        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album)
            .build()
        it.setMediaItem(
            MediaItem.Builder()
                .setMediaId(track.id.toString())
                .setUri(track.uri)
                .setMediaMetadata(metadata)
                .build(),
        )
        it.prepare()
    }

    override fun play() = withController { it.play() }

    override fun pause() = withController { it.pause() }

    override fun seekTo(positionMs: Long) = withController { it.seekTo(positionMs) }

    /** Runs [block] once the controller is connected; blocks queued before then run in order. */
    private fun withController(block: (MediaController) -> Unit) {
        controllerFuture.addListener({ block(controllerFuture.get()) }, mainExecutor)
    }
}
