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
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @GET("api/dashboard/summary")
    suspend fun getDashboardSummary(): ApiResponse<DashboardSummary>

    @GET("api/customers")
    suspend fun getCustomers(): ApiResponse<List<Customer>>

    @POST("api/customers")
    suspend fun createCustomer(@Body customer: Customer): ApiResponse<Customer>

    @PUT("api/customers/{id}")
    suspend fun updateCustomer(@Path("id") id: String, @Body customer: Customer): ApiResponse<Customer>

    @GET("api/products")
    suspend fun getProducts(): ApiResponse<List<Product>>

    @POST("api/products")
    suspend fun createProduct(@Body product: Product): ApiResponse<Product>

    @PUT("api/products/{id}")
    suspend fun updateProduct(@Path("id") id: String, @Body product: Product): ApiResponse<Product>

    @POST("api/invoices")
    suspend fun createInvoice(@Body request: InvoiceRequest): ApiResponse<Any>

    @POST("api/payments")
    suspend fun receivePayment(@Body request: PaymentRequest): ApiResponse<Any>
}
