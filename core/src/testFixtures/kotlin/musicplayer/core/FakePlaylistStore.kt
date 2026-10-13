package musicplayer.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory PlaylistStore for tests. */
class FakePlaylistStore : PlaylistStore {
    private data class Entry(val id: Long, val name: String, val trackIds: List<Long>)

    private val state = MutableStateFlow<List<Entry>>(emptyList())
    private var nextId = 1L

    override val playlists: Flow<List<StoredPlaylist>> =
        state.map { list -> list.map { StoredPlaylist(it.id, it.name, it.trackIds) } }

    override suspend fun create(name: String): Long {
        val id = nextId++
        state.value += Entry(id, name, emptyList())
        return id
    }

    override suspend fun add(playlistId: Long, trackIds: List<Long>): List<Long> {
        val entry = state.value.first { it.id == playlistId }
        val fresh = trackIds.distinct().filter { it !in entry.trackIds }
        state.value = state.value.map { if (it.id == playlistId) Entry(it.id, it.name, it.trackIds + fresh) else it }
        return fresh
    }

    override suspend fun remove(playlistId: Long, trackId: Long) = update(playlistId) { it.copy(trackIds = it.trackIds - trackId) }

    override suspend fun reorder(playlistId: Long, trackIds: List<Long>) = update(playlistId) { entry ->
        val listed = trackIds.distinct().filter { it in entry.trackIds }
        entry.copy(trackIds = listed + (entry.trackIds - listed.toSet()))
    }

    override suspend fun rename(playlistId: Long, name: String) = update(playlistId) { it.copy(name = name) }

    override suspend fun delete(playlistId: Long) {
        state.value = state.value.filter { it.id != playlistId }
    }

    override suspend fun retainOnly(keep: Set<Long>) {
        state.value = state.value.map { it.copy(trackIds = it.trackIds.filter { id -> id in keep }) }
    }

    private fun update(playlistId: Long, change: (Entry) -> Entry) {
        state.value = state.value.map { if (it.id == playlistId) change(it) else it }
    }
}
