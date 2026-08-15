package com.marutipolymer.billing.api

import com.marutipolymer.billing.models.ApiResponse
import com.marutipolymer.billing.models.Customer
import com.marutipolymer.billing.models.DashboardSummary
import com.marutipolymer.billing.models.InvoiceRequest
import com.marutipolymer.billing.models.PaymentRequest
import com.marutipolymer.billing.models.Product
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("api/dashboard/summary")
    suspend fun getDashboardSummary(): ApiResponse<DashboardSummary>

    @GET("api/customers")
    suspend fun getCustomers(): ApiResponse<List<Customer>>

    @GET("api/products")
    suspend fun getProducts(): ApiResponse<List<Product>>

    @POST("api/invoices")
    suspend fun createInvoice(@Body request: InvoiceRequest): ApiResponse<Any>

    @POST("api/payments")
    suspend fun receivePayment(@Body request: PaymentRequest): ApiResponse<Any>
}
