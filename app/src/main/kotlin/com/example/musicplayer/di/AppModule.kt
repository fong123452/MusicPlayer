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
import musicplayer.core.Library
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
    fun audioPermission(impl: AndroidAudioPermission): AudioPermission
}
