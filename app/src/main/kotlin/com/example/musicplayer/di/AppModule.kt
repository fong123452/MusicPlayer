package com.example.musicplayer.di

import com.example.musicplayer.data.MediaStoreTrackSource
import com.example.musicplayer.permission.AndroidAudioPermission
import com.example.musicplayer.permission.AudioPermission
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import com.example.musicplayer.playback.ControllerPlaybackEngine
import musicplayer.core.Library
import musicplayer.core.PlaybackEngine
import musicplayer.core.Queue
import musicplayer.core.TrackSource
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun library(source: TrackSource): Library = Library(source)

    @Provides
    @Singleton
    fun queue(engine: PlaybackEngine): Queue =
        Queue(engine, CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate))

    @Provides
    @Singleton
    @ApplicationScope
    fun applicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}

/** Real device adapters; instrumented tests replace this module with fakes. */
@Module
@InstallIn(SingletonComponent::class)
interface DeviceModule {
    @Binds
    fun trackSource(impl: MediaStoreTrackSource): TrackSource

    @Binds
    fun playbackEngine(impl: ControllerPlaybackEngine): PlaybackEngine

    @Binds
    fun audioPermission(impl: AndroidAudioPermission): AudioPermission
}
