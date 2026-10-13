package com.example.musicplayer.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Whether the app may read the device's audio files. */
interface AudioPermission {
    /** The runtime permission to request. */
    val permission: String
    fun isGranted(): Boolean
}

/** READ_MEDIA_AUDIO on API 33+, READ_EXTERNAL_STORAGE below. */
fun requiredAudioPermission(sdkInt: Int): String =
    if (sdkInt >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE

@Singleton
class AndroidAudioPermission @Inject constructor(
    @ApplicationContext private val context: Context,
) : AudioPermission {
    override val permission: String = requiredAudioPermission(Build.VERSION.SDK_INT)

    override fun isGranted(): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}
