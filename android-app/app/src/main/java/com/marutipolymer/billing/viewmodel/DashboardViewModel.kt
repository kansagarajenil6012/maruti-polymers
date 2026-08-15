package com.marutipolymer.billing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marutipolymer.billing.api.RetrofitClient
import com.marutipolymer.billing.models.DashboardSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        fetchDashboardData()
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                val response = RetrofitClient.apiService.getDashboardSummary()
                if (response.success) {
                    _uiState.value = DashboardUiState.Success(response.data)
                } else {
                    _uiState.value = DashboardUiState.Error(response.message)
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }
}

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val data: DashboardSummary) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
