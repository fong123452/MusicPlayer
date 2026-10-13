package com.example.musicplayer

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MusicPlayerApp : Application() {
    @Inject lateinit var refreshObserver: LibraryRefreshObserver

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(refreshObserver)
    }
}
