package com.example.musicplayer

import com.example.musicplayer.di.DeviceModule
import com.example.musicplayer.permission.AudioPermission
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import musicplayer.core.FakeTrackSource
import musicplayer.core.TrackSource
import javax.inject.Singleton

class FakeAudioPermission : AudioPermission {
    override val permission = "android.permission.READ_MEDIA_AUDIO"
    var granted = true
    override fun isGranted() = granted
}

/** Swaps the device adapters for the fakes from :core's test fixtures. */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DeviceModule::class])
object FakeDeviceModule {
    @Provides
    @Singleton
    fun fakeTrackSource() = FakeTrackSource()

    @Provides
    fun trackSource(fake: FakeTrackSource): TrackSource = fake

    @Provides
    @Singleton
    fun fakeAudioPermission() = FakeAudioPermission()

    @Provides
    fun audioPermission(fake: FakeAudioPermission): AudioPermission = fake
}
