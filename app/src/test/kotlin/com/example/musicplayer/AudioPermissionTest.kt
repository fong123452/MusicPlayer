package com.example.musicplayer

import com.example.musicplayer.permission.requiredAudioPermission
import kotlin.test.Test
import kotlin.test.assertEquals

class AudioPermissionTest {
    @Test
    fun `API 33 and above asks for READ_MEDIA_AUDIO`() {
        assertEquals("android.permission.READ_MEDIA_AUDIO", requiredAudioPermission(33))
        assertEquals("android.permission.READ_MEDIA_AUDIO", requiredAudioPermission(36))
    }

    @Test
    fun `API 32 and below asks for READ_EXTERNAL_STORAGE`() {
        assertEquals("android.permission.READ_EXTERNAL_STORAGE", requiredAudioPermission(32))
        assertEquals("android.permission.READ_EXTERNAL_STORAGE", requiredAudioPermission(26))
    }
}
