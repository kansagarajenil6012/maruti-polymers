package com.marutipolymer.billing.models

data class Product(
    val id: String,
    val product_name: String,
    val size: String?,
    val colour: String?,
    val default_buy_price: Double,
    val default_sell_price: Double,
    val is_active: Boolean
)
