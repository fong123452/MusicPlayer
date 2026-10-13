package musicplayer.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory ThemeStore for tests. */
class FakeThemeStore : ThemeStore {
    private val state = MutableStateFlow(Theme(ThemeMode.FollowSystem, AccentPalette.first()))

    override val theme: Flow<Theme> = state

    override suspend fun setMode(mode: ThemeMode) {
        state.value = state.value.copy(mode = mode)
    }

    override suspend fun setAccent(accent: Accent) {
        state.value = state.value.copy(accent = accent)
    }
}
