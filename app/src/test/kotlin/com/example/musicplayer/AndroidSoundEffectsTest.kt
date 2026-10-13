package com.example.musicplayer

import com.example.musicplayer.playback.AndroidSoundEffects
import com.example.musicplayer.playback.BassBoostHandle
import com.example.musicplayer.playback.EffectFactory
import com.example.musicplayer.playback.EqualizerHandle
import musicplayer.core.SoundSettings
import musicplayer.core.SoundSupport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AndroidSoundEffectsTest {
    private class FakeEqualizer(override val presets: List<String>, val session: Int) : EqualizerHandle {
        override var enabled = false
        var preset: Int? = null
        var released = false
        var dead = false

        override fun usePreset(index: Int) {
            if (dead) throw IllegalStateException("dead")
            preset = index
        }

        override fun release() { released = true }
    }

    private class FakeBassBoost(val session: Int) : BassBoostHandle {
        override val strengthSupported = true
        override var enabled = false
        override fun setStrength(strength: Short) {}
        override fun release() {}
    }

    private class FakeFactory : EffectFactory {
        val equalizers = mutableListOf<FakeEqualizer>()
        val boosts = mutableListOf<FakeBassBoost>()
        var equalizerFails = false
        var bassBoostFails = false

        override fun equalizer(sessionId: Int): EqualizerHandle {
            if (equalizerFails) throw RuntimeException("no equalizer")
            return FakeEqualizer(listOf("Normal", "Rock", "Jazz"), sessionId).also { equalizers += it }
        }

        override fun bassBoost(sessionId: Int): BassBoostHandle {
            if (bassBoostFails) throw RuntimeException("no bass boost")
            return FakeBassBoost(sessionId).also { boosts += it }
        }
    }

    private fun effects(factory: FakeFactory) = AndroidSoundEffects(factory, probeSession = 99) { it() }

    @Test
    fun the_device_presets_are_reported_after_the_probe() {
        assertEquals(SoundSupport(listOf("Normal", "Rock", "Jazz"), bassBoost = true), effects(FakeFactory()).support.value)
    }

    @Test
    fun the_chosen_sound_moves_to_the_new_session_when_the_session_id_changes() {
        val factory = FakeFactory()
        val effects = effects(factory)
        effects.attach(1)
        effects.apply(SoundSettings("Rock", bassBoost = true))

        effects.attach(2)

        val before = factory.equalizers.first { it.session == 1 }
        val after = factory.equalizers.first { it.session == 2 }
        assertTrue(before.released)
        assertEquals(1, after.preset)
        assertTrue(after.enabled)
        assertTrue(factory.boosts.first { it.session == 2 }.enabled)
    }

    @Test
    fun off_or_an_unknown_preset_disables_the_equalizer() {
        val factory = FakeFactory()
        val effects = effects(factory)
        effects.attach(1)
        effects.apply(SoundSettings("Jazz", false))
        val eq = factory.equalizers.first { it.session == 1 }
        assertTrue(eq.enabled)

        effects.apply(SoundSettings("Retired", false))
        assertEquals(false, eq.enabled)
    }

    @Test
    fun an_effect_that_cannot_be_created_is_reported_unsupported() {
        val factory = FakeFactory()
        val effects = effects(factory)
        factory.equalizerFails = true
        factory.bassBoostFails = true

        effects.attach(1)

        assertEquals(SoundSupport(presets = null, bassBoost = false), effects.support.value)
    }

    @Test
    fun an_effect_that_dies_is_dropped_and_reported_without_throwing() {
        val factory = FakeFactory()
        val effects = effects(factory)
        effects.attach(1)
        factory.equalizers.single { it.session == 1 }.dead = true

        effects.apply(SoundSettings("Rock", false))

        assertEquals(null, effects.support.value.presets)
        assertEquals(true, effects.support.value.bassBoost)
    }
}
