package musicplayer.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory SoundStore for tests. */
class FakeSoundStore : SoundStore {
    private val state = MutableStateFlow(SoundSettings())

    override val settings: Flow<SoundSettings> = state

    override suspend fun setPreset(preset: String?) {
        state.value = state.value.copy(preset = preset)
    }

    override suspend fun setBassBoost(on: Boolean) {
        state.value = state.value.copy(bassBoost = on)
    }
}
