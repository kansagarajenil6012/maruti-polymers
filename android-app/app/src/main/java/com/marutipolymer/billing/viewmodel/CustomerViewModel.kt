package com.marutipolymer.billing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marutipolymer.billing.api.RetrofitClient
import com.marutipolymer.billing.models.Customer
import com.marutipolymer.billing.models.CustomerPrice
import com.marutipolymer.billing.models.CustomerPricesRequest
import com.marutipolymer.billing.models.PaymentRequest
import com.marutipolymer.billing.models.PriceUpdateItem
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

    fun addCustomer(customer: Customer, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.createCustomer(customer)
                if (response.success) {
                    fetchCustomers()
                    onResult(true, "Customer added successfully")
                } else {
                    onResult(false, response.message)
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Error adding customer")
            }
        }
    }

    fun updateCustomer(id: String, customer: Customer, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.updateCustomer(id, customer)
                if (response.success) {
                    fetchCustomers()
                    onResult(true, "Customer updated successfully")
                } else {
                    onResult(false, response.message)
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Error updating customer")
            }
        }
    }

    fun fetchCustomerPrices(customerId: String, onResult: (List<CustomerPrice>) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getCustomerPrices(customerId)
                if (response.success) {
                    onResult(response.data)
                } else {
                    onResult(emptyList())
                }
            } catch (e: Exception) {
                onResult(emptyList())
            }
        }
    }

    fun saveCustomerPrices(customerId: String, prices: List<PriceUpdateItem>, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.updateCustomerPrices(customerId, CustomerPricesRequest(prices))
                if (response.success) {
                    onResult(true, "Custom prices saved successfully")
                } else {
                    onResult(false, response.message)
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Error saving custom prices")
            }
        }
    }

    fun receivePayment(request: PaymentRequest, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.receivePayment(request)
                if (response.success) {
                    fetchCustomers()
                    onResult(true, "Payment received successfully")
                } else {
                    onResult(false, response.message)
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Error recording payment")
            }
        }
    }
}

sealed class CustomerUiState {
    object Loading : CustomerUiState()
    data class Success(val data: List<Customer>) : CustomerUiState()
    data class Error(val message: String) : CustomerUiState()
}
