package com.marutipolymer.billing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marutipolymer.billing.api.RetrofitClient
import com.marutipolymer.billing.models.Customer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CustomerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<CustomerUiState>(CustomerUiState.Loading)
    val uiState: StateFlow<CustomerUiState> = _uiState

    init {
        fetchCustomers()
    }

    fun fetchCustomers() {
        viewModelScope.launch {
            _uiState.value = CustomerUiState.Loading
            try {
                val response = RetrofitClient.apiService.getCustomers()
                if (response.success) {
                    _uiState.value = CustomerUiState.Success(response.data)
                } else {
                    _uiState.value = CustomerUiState.Error(response.message)
                }
            } catch (e: Exception) {
                _uiState.value = CustomerUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }
}

sealed class CustomerUiState {
    object Loading : CustomerUiState()
    data class Success(val data: List<Customer>) : CustomerUiState()
    data class Error(val message: String) : CustomerUiState()
}
