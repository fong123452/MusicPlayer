package com.example.musicplayer.playback

import android.content.Context
import android.media.AudioManager
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import musicplayer.core.SoundEffects
import musicplayer.core.SoundSettings
import musicplayer.core.SoundSupport
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.thread

/**
 * Real [SoundEffects]: an android.media.audiofx Equalizer and BassBoost on the player's audio session.
 * [PlaybackService] calls [attach] with the session id, again whenever it changes. An effect the device
 * refuses to create is reported unsupported.
 */
@Singleton
class AndroidSoundEffects internal constructor(
    private val factory: EffectFactory,
    probeSession: Int,
    runInBackground: (() -> Unit) -> Unit,
) : SoundEffects {
    @Inject
    constructor(@ApplicationContext context: Context) : this(
        PlatformEffectFactory,
        context.getSystemService(AudioManager::class.java).generateAudioSessionId(),
        { block -> thread(name = "sound-effects-probe") { block() } },
    )

    // Unsupported until the probe has run: it makes binder calls, so it stays off the main thread.
    private val supportState = MutableStateFlow(SoundSupport(presets = null, bassBoost = false))
    override val support: StateFlow<SoundSupport> = supportState

    private var settings = SoundSettings()
    private var equalizer: EqualizerHandle? = null
    private var bassBoost: BassBoostHandle? = null
    private var sessionId = NO_SESSION

    init {
        runInBackground { supportState.value = probe(probeSession) }
    }

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
        equalizer = create { factory.equalizer(sessionId) }
        if (equalizer == null) supportState.update { it.copy(presets = null) }
        bassBoost = create { factory.bassBoost(sessionId) }
        if (bassBoost == null) supportState.update { it.copy(bassBoost = false) }
        applyToEffects()
    }

    // An effect dies with its session (media server restart, player released); one that throws is dropped and reported unsupported.
    private fun applyToEffects() {
        equalizer?.let { eq ->
            try {
                val index = settings.preset?.let { name -> eq.presets.indexOf(name).takeIf { it >= 0 } }
                if (index == null) {
                    eq.enabled = false
                } else {
                    eq.usePreset(index)
                    eq.enabled = true
                }
            } catch (_: RuntimeException) {
                runCatching { eq.release() }
                equalizer = null
                supportState.update { it.copy(presets = null) }
            }
        }
        bassBoost?.let { boost ->
            try {
                if (settings.bassBoost && boost.strengthSupported) boost.setStrength(BASS_BOOST_STRENGTH)
                boost.enabled = settings.bassBoost
            } catch (_: RuntimeException) {
                runCatching { boost.release() }
                bassBoost = null
                supportState.update { it.copy(bassBoost = false) }
            }
        }
    }

    private fun release() {
        equalizer?.let { runCatching { it.release() } }
        bassBoost?.let { runCatching { it.release() } }
        equalizer = null
        bassBoost = null
    }

    private fun <T> create(block: () -> T): T? = try {
        block()
    } catch (_: RuntimeException) {
        null
    }

    private fun probe(sessionId: Int): SoundSupport {
        val eq = create { factory.equalizer(sessionId) }
        val presets = eq?.presets
        eq?.release()
        val boost = create { factory.bassBoost(sessionId) }
        boost?.release()
        return SoundSupport(presets, bassBoost = boost != null)
    }

    private companion object {
        const val NO_SESSION = 0
        const val BASS_BOOST_STRENGTH: Short = 800
    }
}

/** The two audiofx effects, as far as [AndroidSoundEffects] uses them. Creating one throws when the device refuses. */
internal interface EqualizerHandle {
    val presets: List<String>
    var enabled: Boolean

    fun usePreset(index: Int)

    fun release()
}

internal interface BassBoostHandle {
    val strengthSupported: Boolean
    var enabled: Boolean

    fun setStrength(strength: Short)

    fun release()
}

internal interface EffectFactory {
    fun equalizer(sessionId: Int): EqualizerHandle

    fun bassBoost(sessionId: Int): BassBoostHandle
}

private object PlatformEffectFactory : EffectFactory {
    override fun equalizer(sessionId: Int): EqualizerHandle = object : EqualizerHandle {
        private val eq = Equalizer(0, sessionId)
        override val presets = (0 until eq.numberOfPresets).map { eq.getPresetName(it.toShort()) }
        override var enabled: Boolean
            get() = eq.enabled
            set(value) { eq.enabled = value }

        override fun usePreset(index: Int) = eq.usePreset(index.toShort())

        override fun release() = eq.release()
    }

    override fun bassBoost(sessionId: Int): BassBoostHandle = object : BassBoostHandle {
        private val boost = BassBoost(0, sessionId)
        override val strengthSupported get() = boost.strengthSupported
        override var enabled: Boolean
            get() = boost.enabled
            set(value) { boost.enabled = value }

        override fun setStrength(strength: Short) = boost.setStrength(strength)

        override fun release() = boost.release()
    }
}
