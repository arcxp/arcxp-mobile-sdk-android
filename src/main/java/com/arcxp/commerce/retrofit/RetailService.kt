package com.arcxp.commerce.retrofit

import androidx.annotation.Keep
import com.arcxp.commerce.models.ActivePaywallRule
import retrofit2.Response
import retrofit2.http.GET

/**
 * @suppress
 */
interface RetailService {

    @Keep
    @GET("paywall/active")
    suspend fun getActivePaywallRules(): Response<List<ActivePaywallRule>>
}