package com.marutipolymer.billing.models

data class InvoiceRequest(
    val customer_id: String,
    val invoice_date: String?,
    val discount: Double,
    val paid_amount: Double,
    val payment_mode: String?,
    val payment_reference: String?,
    val remarks: String?,
    val items: List<InvoiceItemRequest>
)

data class InvoiceItemRequest(
    val product_id: String,
    val qty: Double,
    val discount: Double = 0.0
)

data class PaymentRequest(
    val customer_id: String,
    val invoice_id: String? = null,
    val amount: Double,
    val payment_mode: String,
    val payment_date: String? = null,
    val reference_no: String? = null,
    val remarks: String? = null
)
