package musicplayer.core

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeStoreTest {
    private val file = Files.createTempDirectory("theme").resolve("theme.preferences_pb").toFile()

    /** A fresh store over the same file, as after an app restart. */
    private fun openStore(): Pair<ThemeStore, CoroutineScope> {
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        return DataStoreThemeStore(PreferenceDataStoreFactory.create(scope = scope) { file }) to scope
    }

    @Test
    fun a_new_store_follows_the_system_with_the_first_accent() = runBlocking {
        val (store, scope) = openStore()
        assertEquals(Theme(ThemeMode.FollowSystem, AccentPalette.first()), store.theme.first())
        scope.cancel()
    }

    @Test
    fun the_chosen_mode_and_accent_survive_a_restart() = runBlocking {
        val (first, firstScope) = openStore()
        first.setMode(ThemeMode.Dark)
        first.setAccent(AccentPalette[3])
        firstScope.cancel()

        val (second, secondScope) = openStore()
        assertEquals(Theme(ThemeMode.Dark, AccentPalette[3]), second.theme.first())
        secondScope.cancel()
    }

    @Test
    fun a_corrupt_file_reads_as_the_default_theme_and_can_be_saved_over() = runBlocking {
        file.writeBytes(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        val store = DataStoreThemeStore(themeDataStore(scope, file))
        assertEquals(Theme(ThemeMode.FollowSystem, AccentPalette.first()), store.theme.first())
        store.setMode(ThemeMode.Dark)
        assertEquals(ThemeMode.Dark, store.theme.first().mode)
        scope.cancel()
    }

    @Test
    fun an_unknown_stored_mode_reads_as_follow_system() = runBlocking {
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        val dataStore = PreferenceDataStoreFactory.create(scope = scope) { file }
        dataStore.edit { it[stringPreferencesKey("mode")] = "bogus" }
        assertEquals(ThemeMode.FollowSystem, DataStoreThemeStore(dataStore).theme.first().mode)
        scope.cancel()
    }

    @Test
    fun an_accent_that_is_no_longer_in_the_palette_falls_back_to_the_first() = runBlocking {
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        val dataStore = PreferenceDataStoreFactory.create(scope = scope) { file }
        dataStore.edit { it[stringPreferencesKey("accent")] = "retired" }
        assertEquals(AccentPalette.first(), DataStoreThemeStore(dataStore).theme.first().accent)
        scope.cancel()
    }
}
