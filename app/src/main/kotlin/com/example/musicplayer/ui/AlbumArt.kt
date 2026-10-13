package com.example.musicplayer.ui

import android.content.ContentUris
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import coil3.ImageLoader
import coil3.Image
import coil3.asImage
import coil3.compose.SubcomposeAsyncImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.key.Keyer
import coil3.request.Options
import java.util.concurrent.ConcurrentHashMap

/** Largest power-of-two divisor that keeps the short side of a decoded cover at or above [targetPx]. */
internal fun artSampleSize(width: Int, height: Int, targetPx: Int): Int {
    val shortSide = minOf(width, height)
    var sample = 1
    while (shortSide / (sample * 2) >= targetPx) sample *= 2
    return sample
}

/** Placeholder tag for the art in list rows and the mini player; Now Playing uses [ART_PLACEHOLDER_TAG]. */
const val ROW_ART_PLACEHOLDER_TAG = "row_art_placeholder"

/** Asks Coil for the MediaStore art of one album. */
data class AlbumArtRequest(val albumId: Long)

/** Album art from MediaStore: loadThumbnail on API 29+, the albumart content URI below that. */
class AlbumArtFetcher(
    private val context: Context,
    private val request: AlbumArtRequest,
) : Fetcher {
    override suspend fun fetch(): FetchResult? {
        if (request.albumId in withoutArt) return null
        val bitmap = loadBitmap()
        if (bitmap == null) {
            withoutArt += request.albumId
            return null
        }
        return ImageFetchResult(bitmap.asImage(), isSampled = false, dataSource = DataSource.DISK)
    }

    private fun loadBitmap(): Bitmap? = try {
        val resolver = context.contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, request.albumId)
            resolver.loadThumbnail(uri, Size(ART_PX, ART_PX), null)
        } else {
            val uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), request.albumId)
            decodeSampled(resolver, uri)
        }
    } catch (_: Exception) {
        // No art for this album, or the file cannot be read: the caller shows the placeholder.
        null
    } catch (_: OutOfMemoryError) {
        null
    }

    private fun decodeSampled(resolver: ContentResolver, uri: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) } ?: return null
        val options = BitmapFactory.Options().apply {
            inSampleSize = artSampleSize(bounds.outWidth, bounds.outHeight, ART_PX)
        }
        return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    }

    class Factory : Fetcher.Factory<AlbumArtRequest> {
        override fun create(data: AlbumArtRequest, options: Options, imageLoader: ImageLoader): Fetcher =
            AlbumArtFetcher(options.context, data)
    }

    private companion object {
        const val ART_PX = 512

        /** Albums known to have no art, so scrolling past them does not repeat a failing lookup. */
        val withoutArt: MutableSet<Long> = ConcurrentHashMap.newKeySet()
    }
}

object AlbumArtKeyer : Keyer<AlbumArtRequest> {
    override fun key(data: AlbumArtRequest, options: Options) = "album-art:${data.albumId}"
}

@Composable
fun AlbumArt(albumId: Long?, size: Dp, placeholderTag: String, modifier: Modifier = Modifier) {
    val box = modifier.size(size)
    val placeholder: @Composable () -> Unit = {
        Box(
            box.background(MaterialTheme.colorScheme.surfaceVariant).testTag(placeholderTag),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, Modifier.size(size / 2))
        }
    }
    if (albumId == null) {
        placeholder()
    } else {
        SubcomposeAsyncImage(
            model = AlbumArtRequest(albumId),
            contentDescription = null,
            modifier = box,
            contentScale = ContentScale.Crop,
            loading = { placeholder() },
            error = { placeholder() },
        )
    }
}
