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

    @Test
    fun `reorder, remove, rename and delete are stored and survive reopening`() = runBlocking {
        val first = open()
        val keep = first.create("Keep")
        val drop = first.create("Drop")
        first.add(keep, tracksOf(1, 2, 3))
        first.add(drop, tracksOf(1))

        first.move(keep, from = 2, to = 0)
        first.remove(keep, tracksOf(1).single())
        first.rename(keep, " Road trip ")
        first.delete(drop)

        val reopened = open().playlists.first()
        assertEquals(listOf("Road trip"), reopened.map { it.name })
        assertEquals(listOf(3L, 2L), reopened.single().tracks.map { it.id })
    }

    @Test
    fun `a track added after a reorder lands at the end`() = runBlocking {
        val lists = open()
        val id = lists.create("Mix")
        lists.add(id, tracksOf(1, 2))
        lists.move(id, from = 1, to = 0)

        lists.add(id, tracksOf(3))

        assertEquals(listOf(2L, 1L, 3L), lists.playlists.first().single().tracks.map { it.id })
    }

    @Test
    fun `retaining only some tracks deletes the other entries from every playlist`() = runBlocking {
        val lists = open()
        val store = RoomPlaylistStore(db!!.playlists())
        val a = lists.create("A")
        val b = lists.create("B")
        lists.add(a, tracksOf(1, 2))
        lists.add(b, tracksOf(1, 3))

        store.retainOnly(setOf(2L, 3L))

        // Read from the store itself: the Library still knows track 1, so only a real delete shows here.
        assertEquals(listOf(listOf(2L), listOf(3L)), store.playlists.first().map { it.trackIds })
    }

    private fun tracksOf(vararg ids: Long) = ids.map { id -> library.snapshot.value.tracks.first { it.id == id } }
}
