package com.example.musicplayer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import musicplayer.core.Accent
import musicplayer.core.Theme
import musicplayer.core.ThemeMode
import musicplayer.core.ThemeStore
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(private val store: ThemeStore) : ViewModel() {
    /** Null until the stored Theme has been read, so the app does not flash the default before the saved one. */
    val theme: StateFlow<Theme?> = store.theme.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setMode(mode: ThemeMode) {
        viewModelScope.launch { store.setMode(mode) }
    }

    fun setAccent(accent: Accent) {
        viewModelScope.launch { store.setAccent(accent) }
    }
}
