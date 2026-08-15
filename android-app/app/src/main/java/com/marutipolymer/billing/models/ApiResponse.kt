package com.marutipolymer.billing.models

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T
)
