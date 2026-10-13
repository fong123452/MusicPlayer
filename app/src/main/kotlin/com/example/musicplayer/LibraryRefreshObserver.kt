package com.example.musicplayer

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.musicplayer.di.ApplicationScope
import com.example.musicplayer.permission.AudioPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import musicplayer.core.Library
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Refreshes the Library whenever the app comes to the foreground (ON_START of the process
 * lifecycle), as long as the audio permission has been granted. Deliberately no ContentObserver.
 */
@Singleton
class LibraryRefreshObserver @Inject constructor(
    private val library: Library,
    private val permission: AudioPermission,
    @ApplicationScope private val scope: CoroutineScope,
) : DefaultLifecycleObserver {
    override fun onStart(owner: LifecycleOwner) {
        if (permission.isGranted()) scope.launch { library.refresh() }
    }
}
