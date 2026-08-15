package com.marutipolymer.billing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marutipolymer.billing.api.RetrofitClient
import com.marutipolymer.billing.models.Customer
import com.marutipolymer.billing.models.InvoiceItemRequest
import com.marutipolymer.billing.models.InvoiceRequest
import com.marutipolymer.billing.models.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BillingViewModel : ViewModel() {

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _uiState = MutableStateFlow<BillingUiState>(BillingUiState.Idle)
    val uiState: StateFlow<BillingUiState> = _uiState

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                val custRes = RetrofitClient.apiService.getCustomers()
                if (custRes.success) _customers.value = custRes.data.filter { it.is_active }

                val prodRes = RetrofitClient.apiService.getProducts()
                if (prodRes.success) _products.value = prodRes.data.filter { it.is_active }
            } catch (e: Exception) {
                _uiState.value = BillingUiState.Error("Failed to load initial data")
            }
        }
    }

    fun submitInvoice(
        customerId: String,
        items: List<InvoiceItemRequest>,
        discount: Double,
        paidAmount: Double,
        paymentMode: String?
    ) {
        if (customerId.isBlank() || items.isEmpty()) {
            _uiState.value = BillingUiState.Error("Customer and Items are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = BillingUiState.Loading
            try {
                val request = InvoiceRequest(
                    customer_id = customerId,
                    invoice_date = null,
                    discount = discount,
                    paid_amount = paidAmount,
                    payment_mode = if (paidAmount > 0) paymentMode ?: "CASH" else null,
                    payment_reference = null,
                    remarks = null,
                    items = items
                )
                
                val response = RetrofitClient.apiService.createInvoice(request)
                if (response.success) {
                    _uiState.value = BillingUiState.Success
                } else {
                    _uiState.value = BillingUiState.Error(response.message)
                }
            } catch (e: Exception) {
                _uiState.value = BillingUiState.Error(e.message ?: "Failed to generate invoice")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = BillingUiState.Idle
    }
}

sealed class BillingUiState {
    object Idle : BillingUiState()
    object Loading : BillingUiState()
    object Success : BillingUiState()
    data class Error(val message: String) : BillingUiState()
}
