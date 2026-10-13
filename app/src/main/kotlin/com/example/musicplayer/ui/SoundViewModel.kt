package com.example.musicplayer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import musicplayer.core.Sound
import musicplayer.core.SoundSettings
import musicplayer.core.SoundSupport
import javax.inject.Inject

@HiltViewModel
class SoundViewModel @Inject constructor(private val sound: Sound) : ViewModel() {
    /** Null until the stored sound has been read, so Settings does not flash the defaults. */
    val settings: StateFlow<SoundSettings?> = sound.settings.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val support: StateFlow<SoundSupport> = sound.support

    fun setPreset(preset: String?) {
        viewModelScope.launch { sound.setPreset(preset) }
    }

    fun setBassBoost(on: Boolean) {
        viewModelScope.launch { sound.setBassBoost(on) }
    }
}
