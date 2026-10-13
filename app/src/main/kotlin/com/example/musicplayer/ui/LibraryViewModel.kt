package com.example.musicplayer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import musicplayer.core.Library
import musicplayer.core.LibrarySnapshot
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val library: Library,
) : ViewModel() {
    val snapshot: StateFlow<LibrarySnapshot> = library.snapshot

    private val refreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = refreshing

    fun refresh() {
        viewModelScope.launch {
            refreshing.value = true
            try {
                library.refresh()
            } finally {
                refreshing.value = false
            }
        }
    }
}
