package com.marutipolymer.billing.models

data class Customer(
    val id: String,
    val customer_code: String,
    val customer_name: String,
    val mobile: String?,
    val address: String?,
    val city: String?,
    val opening_balance: Double?,
    val is_active: Boolean
)
