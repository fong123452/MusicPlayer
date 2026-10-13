package musicplayer.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** A Playlist as the store keeps it: Tracks are only MediaStore ids, in Playlist order. */
data class StoredPlaylist(val id: Long, val name: String, val trackIds: List<Long>)

/** Persistence port for Playlists. The real adapter is Room; tests use an in-memory fake. */
interface PlaylistStore {
    val playlists: Flow<List<StoredPlaylist>>

    suspend fun create(name: String): Long

    /** Appends the ids not yet in the Playlist, in the given order, and returns the ones it added. */
    suspend fun add(playlistId: Long, trackIds: List<Long>): List<Long>
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
    }

    suspend fun create(name: String): Long = store.create(name)

    /** Adds the Tracks that are not yet in the Playlist, in order, and reports which were already there. */
    suspend fun add(playlistId: Long, tracks: List<Track>): AddResult {
        val addedIds = store.add(playlistId, tracks.map { it.id }).toSet()
        val (added, alreadyThere) = tracks.distinctBy { it.id }.partition { it.id in addedIds }
        return AddResult(added, alreadyThere)
    }

    suspend fun addAlbum(playlistId: Long, album: Album): AddResult = add(playlistId, album.tracks)

    /** Replaces the Queue with a copy of the Playlist's Tracks. */
    fun play(playlist: Playlist, queue: Queue) {
        if (playlist.tracks.isNotEmpty()) queue.replace(playlist.tracks, 0)
    }
}
