package com.example.musicplayer.playback

import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import musicplayer.core.Queue

/**
 * The session's player. The notification, lock screen and headset buttons call next and previous on it;
 * those go to the [Queue], so its previous-restarts rule applies to every control. The wrapped player only
 * ever holds one Track, so it would otherwise report that there is nothing to skip to.
 */
@androidx.annotation.OptIn(UnstableApi::class)
internal class QueueForwardingPlayer(player: Player, private val queue: () -> Queue) : ForwardingPlayer(player) {
    override fun getAvailableCommands(): Player.Commands = super.getAvailableCommands().buildUpon()
        .addAll(
            Player.COMMAND_SEEK_TO_NEXT,
            Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
            Player.COMMAND_SEEK_TO_PREVIOUS,
            Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
        )
        .build()

    override fun isCommandAvailable(command: Int): Boolean = availableCommands.contains(command)

    override fun hasNextMediaItem(): Boolean = true

    override fun hasPreviousMediaItem(): Boolean = true

    override fun seekToNext() = queue().next()

    override fun seekToNextMediaItem() = queue().next()

    override fun seekToPrevious() = queue().previous(currentPosition)

    override fun seekToPreviousMediaItem() = queue().previous(currentPosition)
}
