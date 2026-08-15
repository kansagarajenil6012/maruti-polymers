package com.marutipolymer.billing.api

import com.marutipolymer.billing.models.ApiResponse
import com.marutipolymer.billing.models.CancelInvoiceRequest
import com.marutipolymer.billing.models.Customer
import com.marutipolymer.billing.models.CustomerPrice
import com.marutipolymer.billing.models.CustomerPricesRequest
import com.marutipolymer.billing.models.DashboardSummary
import com.marutipolymer.billing.models.Invoice
import com.marutipolymer.billing.models.InvoiceRequest
import com.marutipolymer.billing.models.LedgerEntry
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

    @GET("api/customers/{id}/prices")
    suspend fun getCustomerPrices(@Path("id") id: String): ApiResponse<List<CustomerPrice>>

    @PUT("api/customers/{id}/prices")
    suspend fun updateCustomerPrices(@Path("id") id: String, @Body request: CustomerPricesRequest): ApiResponse<Any>

    @GET("api/customers/{id}/ledger")
    suspend fun getCustomerLedger(@Path("id") id: String): ApiResponse<List<LedgerEntry>>

    @GET("api/products")
    suspend fun getProducts(): ApiResponse<List<Product>>

    @POST("api/products")
    suspend fun createProduct(@Body product: Product): ApiResponse<Product>

    @PUT("api/products/{id}")
    suspend fun updateProduct(@Path("id") id: String, @Body product: Product): ApiResponse<Product>

    @GET("api/invoices")
    suspend fun getInvoices(): ApiResponse<List<Invoice>>

    @GET("api/invoices/{id}")
    suspend fun getInvoiceById(@Path("id") id: String): ApiResponse<Invoice>

    @POST("api/invoices")
    suspend fun createInvoice(@Body request: InvoiceRequest): ApiResponse<Any>

    @POST("api/invoices/{id}/cancel")
    suspend fun cancelInvoice(@Path("id") id: String, @Body request: CancelInvoiceRequest): ApiResponse<Any>

    @POST("api/payments")
    suspend fun receivePayment(@Body request: PaymentRequest): ApiResponse<Any>
}
