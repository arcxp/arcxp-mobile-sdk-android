package com.arcxp.content.retrofit

import androidx.annotation.Keep
import retrofit2.http.Body
import retrofit2.http.POST

interface AnalyticsService {

    @Keep
    @POST("services/collector")
    suspend fun sendAnalytics(@Body bodyContent: String)
}