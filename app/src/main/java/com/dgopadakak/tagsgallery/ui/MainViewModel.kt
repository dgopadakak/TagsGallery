package com.dgopadakak.tagsgallery.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import com.dgopadakak.tagsgallery.core.compose.models.FullscreenContentModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    @Stable
    data class UiState(
        val fullscreenContent: FullscreenContentModel? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    fun setFullscreenContent(fullscreenContent: FullscreenContentModel?) {
        _uiState.update { it.copy(fullscreenContent = fullscreenContent) }
    }
}
