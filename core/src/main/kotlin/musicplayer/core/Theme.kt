package musicplayer.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.IOException

enum class ThemeMode { Light, Dark, FollowSystem }

/** The app's look: a mode plus one accent from [AccentPalette]. */
data class Theme(val mode: ThemeMode, val accent: Accent)

/** Persistence port for the [Theme]. */
interface ThemeStore {
    val theme: Flow<Theme>

    suspend fun setMode(mode: ThemeMode)

    suspend fun setAccent(accent: Accent)
}

/** Keeps the [Theme] in Preferences DataStore. An unknown or missing value reads as the default. */
class DataStoreThemeStore(private val dataStore: DataStore<Preferences>) : ThemeStore {
    private val modeKey = stringPreferencesKey("mode")
    private val accentKey = stringPreferencesKey("accent")

    override val theme: Flow<Theme> = dataStore.data.catch { if (it is IOException) emit(emptyPreferences()) else throw it }.map { prefs ->
        Theme(
            mode = ThemeMode.entries.firstOrNull { it.name == prefs[modeKey] } ?: ThemeMode.FollowSystem,
            accent = AccentPalette.firstOrNull { it.id == prefs[accentKey] } ?: AccentPalette.first(),
        )
    }

    override suspend fun setMode(mode: ThemeMode) {
        dataStore.edit { it[modeKey] = mode.name }
    }

    override suspend fun setAccent(accent: Accent) {
        dataStore.edit { it[accentKey] = accent.id }
    }
}

/** The Preferences DataStore for the [Theme] at [file]. A corrupt file is replaced by an empty one rather than crashing. */
fun themeDataStore(scope: CoroutineScope, file: File): DataStore<Preferences> =
    PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        scope = scope,
    ) { file }
