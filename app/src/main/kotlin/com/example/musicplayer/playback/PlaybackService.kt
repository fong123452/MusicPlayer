package com.example.musicplayer.playback

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import musicplayer.core.Queue
import javax.inject.Inject

/**
 * Hosts the ExoPlayer so playback survives leaving the app or turning the screen off. Media3 shows the
 * foreground media notification and wires lock-screen and headset buttons through the session.
 */
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    @Inject lateinit var queue: Lazy<Queue>
    @Inject lateinit var soundEffects: AndroidSoundEffects

    private var session: MediaSession? = null

    // The audio session id is how the effects find the player; Media3 still marks that API unstable.
    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
            .apply {
                // The Queue decides what plays next, never ExoPlayer.
                repeatMode = Player.REPEAT_MODE_OFF
                shuffleModeEnabled = false
            }
        player.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) = soundEffects.attach(audioSessionId)
        })
        soundEffects.attach(player.audioSessionId)
        session = MediaSession.Builder(this, QueueForwardingPlayer(player, queue::get)).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = session?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        session?.run {
            player.release()
            release()
        }
        session = null
        soundEffects.attach(0)
        super.onDestroy()
    }
}
