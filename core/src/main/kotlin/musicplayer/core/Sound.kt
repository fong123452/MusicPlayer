package musicplayer.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** The sound the user chose: an [EQ Preset][preset] by the name the device gives it (null = equalizer off) and [Bass Boost][bassBoost]. */
data class SoundSettings(val preset: String? = null, val bassBoost: Boolean = false)

/** What the device can do: its EQ Preset names (null when it has no equalizer) and whether it has Bass Boost. */
data class SoundSupport(val presets: List<String>?, val bassBoost: Boolean)

/** Port: the device's audio effects, applied to whatever is playing. */
interface SoundEffects {
    val support: StateFlow<SoundSupport>

    fun apply(settings: SoundSettings)
}

/** Persistence port for [SoundSettings]. */
interface SoundStore {
    val settings: Flow<SoundSettings>

    suspend fun setPreset(preset: String?)

    suspend fun setBassBoost(on: Boolean)
}

/**
 * Keeps the chosen sound and applies it through [SoundEffects] whenever it changes and whenever playback
 * changes, so the effects follow every Track the engine starts.
 */
class Sound(private val store: SoundStore, private val effects: SoundEffects, isPlaying: Flow<Boolean>, scope: CoroutineScope) {
    val settings: Flow<SoundSettings> = store.settings
    val support: StateFlow<SoundSupport> = effects.support

    init {
        scope.launch {
            combine(store.settings, isPlaying) { settings, _ -> settings }.collect(effects::apply)
        }
    }

    /** Chooses an EQ Preset by name; null turns the equalizer off. */
    suspend fun setPreset(preset: String?) = store.setPreset(preset)

    suspend fun setBassBoost(on: Boolean) = store.setBassBoost(on)
}

/** Keeps [SoundSettings] in Preferences DataStore. */
class DataStoreSoundStore(private val dataStore: DataStore<Preferences>) : SoundStore {
    private val presetKey = stringPreferencesKey("preset")
    private val bassBoostKey = booleanPreferencesKey("bass_boost")

    override val settings: Flow<SoundSettings> = dataStore.data.map { prefs ->
        SoundSettings(preset = prefs[presetKey], bassBoost = prefs[bassBoostKey] ?: false)
    }

    override suspend fun setPreset(preset: String?) {
        dataStore.edit { if (preset == null) it.remove(presetKey) else it[presetKey] = preset }
    }

    override suspend fun setBassBoost(on: Boolean) {
        dataStore.edit { it[bassBoostKey] = on }
    }
}
