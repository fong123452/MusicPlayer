package com.example.musicplayer.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import musicplayer.core.PlaylistStore
import musicplayer.core.StoredPlaylist

class RoomPlaylistStore(private val dao: PlaylistDao, private val clock: () -> Long = System::currentTimeMillis) : PlaylistStore {
    override val playlists: Flow<List<StoredPlaylist>> = combine(dao.observePlaylists(), dao.observeEntries()) { rows, entries ->
        val idsByPlaylist = entries.groupBy({ it.playlistId }, { it.trackId })
        rows.map { StoredPlaylist(it.id, it.name, idsByPlaylist[it.id].orEmpty()) }
    }

    override suspend fun create(name: String): Long = dao.insertPlaylist(PlaylistRow(name = name, createdAt = clock()))

    override suspend fun add(playlistId: Long, trackIds: List<Long>): List<Long> = dao.addEntries(playlistId, trackIds)

    override suspend fun remove(playlistId: Long, trackId: Long) = dao.deleteEntry(playlistId, trackId)

    override suspend fun reorder(playlistId: Long, trackIds: List<Long>) = dao.reorder(playlistId, trackIds)

    override suspend fun rename(playlistId: Long, name: String) = dao.rename(playlistId, name)

    override suspend fun delete(playlistId: Long) = dao.deletePlaylist(playlistId)

    override suspend fun retainOnly(keep: Set<Long>) = dao.retainOnly(keep)
}
