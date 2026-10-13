package musicplayer.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/** A Playlist as the store keeps it: Tracks are only MediaStore ids, in Playlist order. */
data class StoredPlaylist(val id: Long, val name: String, val trackIds: List<Long>)

/** Persistence port for Playlists. The real adapter is Room; tests use an in-memory fake. */
interface PlaylistStore {
    val playlists: Flow<List<StoredPlaylist>>

    suspend fun create(name: String): Long

    /** Appends the ids not yet in the Playlist, in the given order, and returns the ones it added. */
    suspend fun add(playlistId: Long, trackIds: List<Long>): List<Long>

    suspend fun remove(playlistId: Long, trackId: Long)

    /** Puts the listed ids at the front of the Playlist in the given order; ids not listed keep their relative order after them. */
    suspend fun reorder(playlistId: Long, trackIds: List<Long>)

    suspend fun rename(playlistId: Long, name: String)

    suspend fun delete(playlistId: Long)

    /** Removes every entry, in every Playlist, whose id is not in [keep]. */
    suspend fun retainOnly(keep: Set<Long>)
}

/** The order a Playlist holding [existing] takes when [wanted] is put first: the ones it holds, then the rest as they were. */
fun reorderedIds(existing: List<Long>, wanted: List<Long>): List<Long> {
    val listed = wanted.distinct().filter { it in existing }
    return listed + (existing - listed.toSet())
}

/** A named, ordered list of Tracks that the user keeps. */
data class Playlist(val id: Long, val name: String, val tracks: List<Track>)

/** What happened when Tracks were added to a Playlist. */
data class AddResult(val added: List<Track>, val alreadyThere: List<Track>)

class Playlists(private val store: PlaylistStore, private val library: Library) {
    /** The Playlists with their Tracks resolved from the current Library; ids the Library no longer has are left out. */
    val playlists: Flow<List<Playlist>> = combine(store.playlists, library.snapshot) { stored, snapshot ->
        val byId = snapshot.tracks.associateBy { it.id }
        stored.map { Playlist(it.id, it.name, it.trackIds.mapNotNull(byId::get)) }
    }.flowOn(Dispatchers.Default)

    /** Creates a Playlist named [name] without surrounding spaces. */
    suspend fun create(name: String): Long {
        require(name.isNotBlank()) { "A Playlist needs a name" }
        return store.create(name.trim())
    }

    /** Adds the Tracks that are not yet in the Playlist, in order, and reports which were already there. */
    suspend fun add(playlistId: Long, tracks: List<Track>): AddResult {
        val addedIds = store.add(playlistId, tracks.map { it.id }).toSet()
        val (added, alreadyThere) = tracks.distinctBy { it.id }.partition { it.id in addedIds }
        return AddResult(added, alreadyThere)
    }

    /** Renames the Playlist to [name] without surrounding spaces. */
    suspend fun rename(playlistId: Long, name: String) {
        require(name.isNotBlank()) { "A Playlist needs a name" }
        store.rename(playlistId, name.trim())
    }

    suspend fun delete(playlistId: Long) = store.delete(playlistId)

    suspend fun remove(playlistId: Long, track: Track) = store.remove(playlistId, track.id)

    /** Moves the Track at [from] to [to] in the Playlist as the user sees it; indices outside it are ignored. */
    suspend fun move(playlistId: Long, from: Int, to: Int) {
        val ids = playlists.first().firstOrNull { it.id == playlistId }?.tracks?.map { it.id } ?: return
        if (from !in ids.indices || to !in ids.indices || from == to) return
        store.reorder(playlistId, ids.toMutableList().apply { add(to, removeAt(from)) })
    }

    /**
     * Removes from every Playlist the Tracks that a Library refresh no longer finds. A Library that scanned as empty
     * is skipped, so a glitchy scan cannot wipe the user's Playlists.
     */
    fun pruneOnRefresh(scope: CoroutineScope): Job = scope.launch {
        library.snapshot.filter { it.loaded && it.tracks.isNotEmpty() }.collect { snapshot ->
            store.retainOnly(snapshot.tracks.mapTo(HashSet()) { it.id })
        }
    }

    /** Replaces the Queue with a copy of the Playlist's Tracks. Returns false, leaving the Queue alone, when it has none. */
    fun play(playlist: Playlist, queue: Queue): Boolean {
        if (playlist.tracks.isEmpty()) return false
        queue.replace(playlist.tracks, 0)
        return true
    }
}
