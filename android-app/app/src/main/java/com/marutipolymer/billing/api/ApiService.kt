package com.marutipolymer.billing.api

import com.marutipolymer.billing.models.ApiResponse
import com.marutipolymer.billing.models.DashboardSummary
import retrofit2.http.GET

interface ApiService {
    @GET("api/dashboard/summary")
    suspend fun getDashboardSummary(): ApiResponse<DashboardSummary>
}
