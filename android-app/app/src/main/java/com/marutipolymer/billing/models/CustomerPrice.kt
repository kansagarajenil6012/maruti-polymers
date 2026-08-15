package com.marutipolymer.billing.models

data class CustomerPrice(
    val id: String?,
    val customer_id: String?,
    val product_id: String,
    val selling_price: Double,
    val products: ProductDetails? = null
)

data class ProductDetails(
    val product_name: String,
    val size: String?,
    val colour: String?,
    val default_sell_price: Double
)

data class PriceUpdateItem(
    val product_id: String,
    val selling_price: Double
)

data class CustomerPricesRequest(
    val prices: List<PriceUpdateItem>
)

data class LedgerEntry(
    val id: String?,
    val date: String,
    val particulars: String,
    val debit: Double,
    val credit: Double,
    val balance: Double,
    val type: String
)

data class Invoice(
    val id: String,
    val invoice_no: String,
    val customer_id: String,
    val customer_name: String?,
    val invoice_date: String,
    val subtotal: Double,
    val discount: Double,
    val total_amount: Double,
    val paid_amount: Double,
    val pending_amount: Double,
    val status: String,
    val remarks: String?,
    val invoice_items: List<InvoiceItemDetail>? = null
)

data class InvoiceItemDetail(
    val id: String,
    val product_id: String,
    val product_name: String,
    val qty: Double,
    val rate: Double,
    val discount: Double,
    val amount: Double
)

data class CancelInvoiceRequest(
    val cancel_reason: String
)
