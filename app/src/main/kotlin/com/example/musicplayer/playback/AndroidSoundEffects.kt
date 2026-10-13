package com.example.musicplayer.playback

import android.content.Context
import android.media.AudioManager
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import musicplayer.core.SoundEffects
import musicplayer.core.SoundSettings
import musicplayer.core.SoundSupport
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Real [SoundEffects]: an android.media.audiofx Equalizer and BassBoost on the player's audio session.
 * [PlaybackService] calls [attach] with the session id, again whenever it changes. An effect the device
 * refuses to create is reported unsupported.
 */
@Singleton
class AndroidSoundEffects @Inject constructor(@ApplicationContext context: Context) : SoundEffects {
    private val supportState = MutableStateFlow(probe(context.getSystemService(AudioManager::class.java).generateAudioSessionId()))
    override val support: StateFlow<SoundSupport> = supportState

    private var settings = SoundSettings()
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var sessionId = NO_SESSION

    @Synchronized
    override fun apply(settings: SoundSettings) {
        this.settings = settings
        applyToEffects()
    }

    /** Moves the effects to [sessionId] (or drops them for [NO_SESSION]) and re-applies the chosen sound. */
    @Synchronized
    fun attach(sessionId: Int) {
        if (sessionId == this.sessionId) return
        release()
        this.sessionId = sessionId
        if (sessionId == NO_SESSION) return
        equalizer = create { Equalizer(0, sessionId) }.also { if (it == null) supportState.value = supportState.value.copy(presets = null) }
        bassBoost = create { BassBoost(0, sessionId) }.also { if (it == null) supportState.value = supportState.value.copy(bassBoost = false) }
        applyToEffects()
    }

    private fun applyToEffects() {
        equalizer?.let { eq ->
            val index = settings.preset?.let { name -> (0 until eq.numberOfPresets).firstOrNull { eq.getPresetName(it.toShort()) == name } }
            if (index == null) {
                eq.enabled = false
            } else {
                eq.usePreset(index.toShort())
                eq.enabled = true
            }
        }
        bassBoost?.let { boost ->
            if (settings.bassBoost && boost.strengthSupported) boost.setStrength(BASS_BOOST_STRENGTH)
            boost.enabled = settings.bassBoost
        }
    }

    private fun release() {
        equalizer?.release()
        bassBoost?.release()
        equalizer = null
        bassBoost = null
    }

    private fun <T> create(block: () -> T): T? = try {
        block()
    } catch (_: RuntimeException) {
        null
    }

    private fun probe(sessionId: Int): SoundSupport {
        val eq = create { Equalizer(0, sessionId) }
        val presets = eq?.let { e -> (0 until e.numberOfPresets).map { e.getPresetName(it.toShort()) } }
        eq?.release()
        val boost = create { BassBoost(0, sessionId) }
        boost?.release()
        return SoundSupport(presets, bassBoost = boost != null)
    }

    private companion object {
        const val NO_SESSION = 0
        const val BASS_BOOST_STRENGTH: Short = 800
    }
}
