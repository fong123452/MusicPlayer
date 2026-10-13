package musicplayer.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** SoundEffects for tests: records every apply. */
class FakeSoundEffects(support: SoundSupport = SoundSupport(listOf("Normal", "Rock", "Jazz"), bassBoost = true)) : SoundEffects {
    override val support: StateFlow<SoundSupport> = MutableStateFlow(support)

    val applied = mutableListOf<SoundSettings>()

    override fun apply(settings: SoundSettings) {
        applied += settings
    }
}
