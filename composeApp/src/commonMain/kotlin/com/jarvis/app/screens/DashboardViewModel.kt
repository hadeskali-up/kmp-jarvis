package com.jarvis.app.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.app.core.UiState
import com.jarvis.app.models.SnapshotResponse
import com.jarvis.app.services.DashboardService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val service: DashboardService
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<SnapshotResponse>>(UiState.Loading)
    val state: StateFlow<UiState<SnapshotResponse>> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            service.fetchSnapshot().fold(
                onSuccess = { _state.value = UiState.Success(it) },
                onFailure = { _state.value = UiState.Error(it.message ?: "Unknown error") }
            )
        }
    }
}
