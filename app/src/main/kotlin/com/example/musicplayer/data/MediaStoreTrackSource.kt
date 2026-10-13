package com.example.musicplayer.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import musicplayer.core.RawTrack
import musicplayer.core.TrackSource
import javax.inject.Inject

/** What counts as a Track on the device: marked as music, and at least 30 seconds long. */
internal object MediaStoreQuery {
    const val MIN_DURATION_MS = 30_000L
    const val SELECTION =
        "${MediaStore.Audio.Media.IS_MUSIC} = 1 AND ${MediaStore.Audio.Media.DURATION} >= ?"
    val SELECTION_ARGS = arrayOf(MIN_DURATION_MS.toString())

    // ALBUM_ARTIST, DISC_NUMBER and CD_TRACK_NUMBER only exist as columns from API 30.
    fun projection(sdkInt: Int): Array<String> {
        val columns = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DISPLAY_NAME,
        )
        if (sdkInt >= Build.VERSION_CODES.R) {
            columns += MediaStore.Audio.Media.ALBUM_ARTIST
            columns += MediaStore.Audio.Media.DISC_NUMBER
            columns += MediaStore.Audio.Media.CD_TRACK_NUMBER
        }
        return columns.toTypedArray()
    }
}

class MediaStoreTrackSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : TrackSource {

    override suspend fun loadTracks(): List<RawTrack> = withContext(Dispatchers.IO) {
        // A SecurityException (permission revoked) or a null cursor must not read as "no music":
        // they propagate and Library.refresh keeps the previous snapshot.
        query(context.contentResolver)
    }

    private fun query(resolver: ContentResolver): List<RawTrack> {
        val sdk = Build.VERSION.SDK_INT
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor = resolver.query(
            collection,
            MediaStoreQuery.projection(sdk),
            MediaStoreQuery.SELECTION,
            MediaStoreQuery.SELECTION_ARGS,
            null,
        ) ?: error("MediaStore returned no cursor")
        cursor.use { c ->
            val id = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val title = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artist = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val album = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumId = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val track = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val duration = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val displayName = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val hasR = sdk >= Build.VERSION_CODES.R
            val albumArtist = c.optionalColumn(hasR, MediaStore.Audio.Media.ALBUM_ARTIST)
            val disc = c.optionalColumn(hasR, MediaStore.Audio.Media.DISC_NUMBER)
            val cdTrack = c.optionalColumn(hasR, MediaStore.Audio.Media.CD_TRACK_NUMBER)

            val result = ArrayList<RawTrack>(c.count)
            while (c.moveToNext()) {
                val trackId = c.getLong(id)
                // Before API 30 the TRACK column packs the disc number into the thousands.
                val packedTrack = c.intOrNull(track)
                result += RawTrack(
                    id = trackId,
                    uri = ContentUris.withAppendedId(collection, trackId).toString(),
                    title = c.stringOrNull(title),
                    artist = c.stringOrNull(artist),
                    album = c.stringOrNull(album),
                    albumArtist = if (albumArtist >= 0) c.stringOrNull(albumArtist) else null,
                    albumId = c.longOrNull(albumId),
                    discNumber = if (disc >= 0) c.intOrNull(disc) else packedTrack?.div(1000)?.takeIf { it > 0 },
                    trackNumber = if (cdTrack >= 0) c.intOrNull(cdTrack) else packedTrack?.rem(1000),
                    durationMs = c.getLong(duration),
                    displayName = c.getString(displayName).orEmpty(),
                )
            }
            return result
        }
    }

    private fun Cursor.optionalColumn(available: Boolean, name: String) =
        if (available) getColumnIndex(name) else -1

    private fun Cursor.stringOrNull(i: Int) = if (isNull(i)) null else getString(i)
    private fun Cursor.intOrNull(i: Int) = if (isNull(i)) null else getInt(i)
    private fun Cursor.longOrNull(i: Int) = if (isNull(i)) null else getLong(i)
}
