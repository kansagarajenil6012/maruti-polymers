package com.marutipolymer.billing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marutipolymer.billing.api.RetrofitClient
import com.marutipolymer.billing.models.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val uiState: StateFlow<ProductUiState> = _uiState

    init {
        fetchProducts()
    }

    fun fetchProducts() {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            try {
                val response = RetrofitClient.apiService.getProducts()
                if (response.success) {
                    _uiState.value = ProductUiState.Success(response.data)
                } else {
                    _uiState.value = ProductUiState.Error(response.message)
                }
            } catch (e: Exception) {
                _uiState.value = ProductUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }
}

sealed class ProductUiState {
    object Loading : ProductUiState()
    data class Success(val data: List<Product>) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
}
