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
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import musicplayer.core.PlaybackEngine
import musicplayer.core.PlaybackEvent
import musicplayer.core.Track
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
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
    private val connection = ReconnectingController(
        connect = {
            MediaController.Builder(
                context,
                SessionToken(context, ComponentName(context, PlaybackService::class.java)),
            ).buildAsync()
        },
        executor = mainExecutor,
        onConnected = ::onConnected,
        onFailure = { eventFlow.tryEmit(PlaybackEvent.Error(it.message)) },
    )

    init {
        connection.run { } // connect eagerly so isPlaying and events track the service from the start
    }

    private fun onConnected(controller: MediaController) {
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
    private fun withController(block: (MediaController) -> Unit) = connection.run(block)
}

/**
 * Runs [block] with the future's value once it is ready. If the connection failed or was cancelled
 * there is no controller, so [block] is skipped and playback commands become no-ops.
 */
internal fun <T> whenConnected(future: ListenableFuture<T>, executor: Executor, block: (T) -> Unit) {
    future.addListener({
        val value = try {
            future.get()
        } catch (_: ExecutionException) {
            return@addListener
        } catch (_: CancellationException) {
            return@addListener
        }
        block(value)
    }, executor)
}

/**
 * Owns the controller connection and rebuilds it after a failure, so one failed connect does not
 * disable playback for the process lifetime. Commands run in order once connected; a command issued
 * while the connection is failing is dropped, and the next one reconnects. [onFailure] fires once per
 * failed connection (not for cancellation). Main thread only.
 */
internal class ReconnectingController<C>(
    private val connect: () -> ListenableFuture<C>,
    private val executor: Executor,
    private val onConnected: (C) -> Unit,
    private val onFailure: (Throwable) -> Unit,
) {
    private var future: ListenableFuture<C>? = null

    fun run(block: (C) -> Unit) {
        val current = future ?: connect().also { fresh ->
            future = fresh
            // Registered first, so it runs before any queued command and resets before they are skipped.
            fresh.addListener({
                try {
                    onConnected(fresh.get())
                } catch (e: ExecutionException) {
                    if (future === fresh) future = null
                    onFailure(e.cause ?: e)
                } catch (_: CancellationException) {
                    if (future === fresh) future = null
                }
            }, executor)
        }
        whenConnected(current, executor, block)
    }
}
