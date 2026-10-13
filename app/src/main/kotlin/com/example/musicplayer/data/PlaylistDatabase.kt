package com.example.musicplayer.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "playlist")
data class PlaylistRow(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
)

@Entity(tableName = "playlist_entry", primaryKeys = ["playlistId", "trackId"])
data class PlaylistEntryRow(
    val playlistId: Long,
    val trackId: Long,
    val position: Int,
)

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlist ORDER BY createdAt, id")
    fun observePlaylists(): Flow<List<PlaylistRow>>

    @Query("SELECT * FROM playlist_entry ORDER BY playlistId, position")
    fun observeEntries(): Flow<List<PlaylistEntryRow>>

    @Insert
    suspend fun insertPlaylist(row: PlaylistRow): Long

    @Query("SELECT trackId FROM playlist_entry WHERE playlistId = :playlistId")
    suspend fun trackIds(playlistId: Long): List<Long>

    @Query("SELECT COALESCE(MAX(position) + 1, 0) FROM playlist_entry WHERE playlistId = :playlistId")
    suspend fun nextPosition(playlistId: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEntries(rows: List<PlaylistEntryRow>)

    @Transaction
    suspend fun addEntries(playlistId: Long, trackIds: List<Long>): List<Long> {
        val existing = trackIds(playlistId).toSet()
        val fresh = trackIds.distinct().filter { it !in existing }
        val start = nextPosition(playlistId)
        insertEntries(fresh.mapIndexed { i, id -> PlaylistEntryRow(playlistId, id, start + i) })
        return fresh
    }
}

@Database(entities = [PlaylistRow::class, PlaylistEntryRow::class], version = 1, exportSchema = false)
abstract class PlaylistDatabase : RoomDatabase() {
    abstract fun playlists(): PlaylistDao
}
