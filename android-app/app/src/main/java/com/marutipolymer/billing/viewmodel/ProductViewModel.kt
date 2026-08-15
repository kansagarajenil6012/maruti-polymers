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

    fun addProduct(product: Product, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.createProduct(product)
                if (response.success) {
                    fetchProducts()
                    onResult(true, "Product added successfully")
                } else {
                    onResult(false, response.message)
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Error adding product")
            }
        }
    }

    fun updateProduct(id: String, product: Product, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.updateProduct(id, product)
                if (response.success) {
                    fetchProducts()
                    onResult(true, "Product updated successfully")
                } else {
                    onResult(false, response.message)
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Error updating product")
            }
        }
    }
}

sealed class ProductUiState {
    object Loading : ProductUiState()
    data class Success(val data: List<Product>) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
}
