package musicplayer.core

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class SoundTest {
    private val file = Files.createTempDirectory("sound").resolve("sound.preferences_pb").toFile()

    /** A fresh store, engine and effects over the same file, as after an app restart. */
    private class Session(file: java.io.File, support: SoundSupport? = null) {
        val scope = CoroutineScope(Job() + Dispatchers.Unconfined)
        private val storeScope = CoroutineScope(Job() + Dispatchers.IO)
        val store = DataStoreSoundStore(PreferenceDataStoreFactory.create(scope = storeScope) { file })
        val engine = FakePlaybackEngine()
        val effects = if (support == null) FakeSoundEffects() else FakeSoundEffects(support)
        val sound = Sound(store, effects, engine.isPlaying, scope)

        fun close() {
            scope.cancel()
            storeScope.cancel()
        }
    }

    /** Effects are applied from a collector on another thread; wait for the last one to match. */
    private fun awaitApplied(effects: FakeSoundEffects, expected: SoundSettings) {
        val deadline = System.currentTimeMillis() + 3_000
        while (effects.applied.lastOrNull() != expected && System.currentTimeMillis() < deadline) Thread.sleep(10)
        assertEquals(expected, effects.applied.lastOrNull())
    }

    @Test
    fun the_chosen_sound_survives_a_restart_and_is_applied_when_playback_starts() = runBlocking {
        val first = Session(file)
        first.sound.setPreset("Rock")
        first.sound.setBassBoost(true)
        first.close()

        val second = Session(file)
        second.engine.play()
        awaitApplied(second.effects, SoundSettings("Rock", true))
        second.close()
    }

    @Test
    fun a_new_store_has_no_preset_and_no_bass_boost() = runBlocking {
        val session = Session(file)
        assertEquals(SoundSettings(null, false), session.sound.settings.first())
        session.close()
    }

    @Test
    fun choosing_off_turns_the_equalizer_off_while_playing() = runBlocking {
        val session = Session(file)
        session.engine.play()
        session.sound.setPreset("Jazz")
        awaitApplied(session.effects, SoundSettings("Jazz", false))

        session.sound.setPreset(null)
        awaitApplied(session.effects, SoundSettings(null, false))
        session.close()
    }

    @Test
    fun what_the_device_cannot_do_is_reported() = runBlocking {
        val unsupported = SoundSupport(presets = null, bassBoost = false)
        val session = Session(file, unsupported)
        assertEquals(unsupported, session.sound.support.value)
        session.close()
    }
}
