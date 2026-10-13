package com.example.musicplayer

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.musicplayer.data.PlaylistDatabase
import com.example.musicplayer.data.RoomPlaylistStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import musicplayer.core.FakeTrackSource
import musicplayer.core.Library
import musicplayer.core.Playlists
import musicplayer.core.rawTrack
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class RoomPlaylistsTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val dbName = "playlists-test.db"
    private var db: PlaylistDatabase? = null

    private lateinit var library: Library

    private fun open(): Playlists {
        db?.close()
        val database = Room.databaseBuilder(context, PlaylistDatabase::class.java, dbName)
            .allowMainThreadQueries().build()
        db = database
        library = Library(FakeTrackSource(listOf(rawTrack(1, "One"), rawTrack(2, "Two"), rawTrack(3, "Three"))))
        runBlocking { library.refresh() }
        return Playlists(RoomPlaylistStore(database.playlists()), library)
    }

    @After
    fun tearDown() {
        db?.close()
        context.deleteDatabase(dbName)
    }

    @Test
    fun `a playlist and its tracks survive closing and reopening the database`() = runBlocking {
        val first = open()
        val id = first.create("Road trip")
        first.add(id, tracksOf(3, 1))

        val reopened = open().playlists.first()

        assertEquals(listOf("Road trip"), reopened.map { it.name })
        assertEquals(listOf(3L, 1L), reopened.single().tracks.map { it.id })
    }

    @Test
    fun `adding a track already in the playlist is reported and not stored twice`() = runBlocking {
        val lists = open()
        val id = lists.create("Mix")
        lists.add(id, tracksOf(1, 2))

        val result = lists.add(id, tracksOf(2, 3))

        assertEquals(listOf(2L), result.alreadyThere.map { it.id })
        assertEquals(listOf(3L), result.added.map { it.id })
        assertEquals(listOf(1L, 2L, 3L), lists.playlists.first().single().tracks.map { it.id })
    }

    private fun tracksOf(vararg ids: Long) = ids.map { id -> library.snapshot.value.tracks.first { it.id == id } }
}
