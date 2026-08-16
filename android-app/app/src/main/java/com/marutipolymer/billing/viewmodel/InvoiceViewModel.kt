package com.marutipolymer.billing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marutipolymer.billing.api.RetrofitClient
import com.marutipolymer.billing.models.CancelInvoiceRequest
import com.marutipolymer.billing.models.Invoice
import com.marutipolymer.billing.models.PaymentRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InvoiceViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<InvoiceUiState>(InvoiceUiState.Loading)
    val uiState: StateFlow<InvoiceUiState> = _uiState

    init {
        fetchInvoices()
    }

    fun fetchInvoices() {
        viewModelScope.launch {
            _uiState.value = InvoiceUiState.Loading
            try {
                val response = RetrofitClient.apiService.getInvoices()
                if (response.success) {
                    _uiState.value = InvoiceUiState.Success(response.data)
                } else {
                    _uiState.value = InvoiceUiState.Error(response.message)
                }
            } catch (e: Exception) {
                _uiState.value = InvoiceUiState.Error(e.message ?: "Failed to fetch invoices")
            }
        }
    }

    fun cancelInvoice(invoiceId: String, reason: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.cancelInvoice(invoiceId, CancelInvoiceRequest(reason))
                if (response.success) {
                    fetchInvoices()
                    onResult(true, "Invoice cancelled successfully")
                } else {
                    onResult(false, response.message)
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to cancel invoice")
            }
        }
    }

    fun receivePayment(request: PaymentRequest, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.receivePayment(request)
                if (response.success) {
                    fetchInvoices()
                    onResult(true, "Payment recorded successfully")
                } else {
                    onResult(false, response.message)
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to record payment")
            }
        }
    }
}

sealed class InvoiceUiState {
    object Loading : InvoiceUiState()
    data class Success(val data: List<Invoice>) : InvoiceUiState()
    data class Error(val message: String) : InvoiceUiState()
}
