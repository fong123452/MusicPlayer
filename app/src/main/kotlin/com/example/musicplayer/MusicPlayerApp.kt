package com.example.musicplayer

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.example.musicplayer.ui.AlbumArtFetcher
import com.example.musicplayer.ui.AlbumArtKeyer
import dagger.hilt.android.HiltAndroidApp
import musicplayer.core.Sound
import javax.inject.Inject

@HiltAndroidApp
class MusicPlayerApp : Application(), SingletonImageLoader.Factory {
    @Inject lateinit var refreshObserver: LibraryRefreshObserver

    // Injected so the chosen sound is applied from process start, before any screen asks for it.
    @Inject lateinit var sound: Sound

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(refreshObserver)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(AlbumArtKeyer)
                add(AlbumArtFetcher.Factory())
            }
            .build()
}
