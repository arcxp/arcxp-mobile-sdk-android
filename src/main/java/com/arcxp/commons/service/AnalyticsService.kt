package com.arcxp.commons.service

import androidx.annotation.Keep
import com.arcxp.commons.models.ArcxpAnalytics
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit interface class that defines the API for the ArcMediaClient
 * @suppress
 */
interface AnalyticsService {

    @Keep
    @POST("/services/collector")
    suspend fun postAnalytics(@Body data: List<ArcxpAnalytics>): Response<Void>
}