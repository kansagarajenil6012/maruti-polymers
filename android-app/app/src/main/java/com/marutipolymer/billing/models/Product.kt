package com.marutipolymer.billing.models

data class Product(
    val id: String,
    val product_name: String,
    val size: String?,
    val default_sell_price: Double,
    val is_active: Boolean
)
