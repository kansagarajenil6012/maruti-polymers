package com.marutipolymer.billing.models

data class DashboardSummary(
    val today_sales: Double,
    val month_sales: Double,
    val today_collection: Double,
    val total_outstanding: Double,
    val total_profit: Double,
    val today_profit: Double,
    val pending_invoices_count: Int,
    val total_customers: Int,
    val total_products: Int,
    val top_outstanding_customers: List<CustomerOutstanding>,
    val recent_invoices: List<RecentInvoice>
)

data class CustomerOutstanding(
    val id: String,
    val customer_name: String,
    val outstanding: Double
)

data class RecentInvoice(
    val id: String,
    val invoice_no: String,
    val total_amount: Double,
    val status: String,
    val customer_name: String
)
