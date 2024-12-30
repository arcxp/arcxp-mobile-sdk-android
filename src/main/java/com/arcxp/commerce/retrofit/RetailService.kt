package com.arcxp.commerce.retrofit

import androidx.annotation.Keep
import com.arcxp.commerce.models.ActivePaywallRule
import retrofit2.Response
import retrofit2.http.GET

/**
 * RetailService is a Retrofit service interface for handling retail-related API calls.
 * It provides methods to fetch active paywall rules.
 *
 * The interface defines the following operations:
 * - Fetch active paywall rules
 *
 * Usage:
 * - Implement this interface using Retrofit to create an instance of the service.
 * - Call the provided methods to perform retail operations and handle the results through Retrofit's response handling.
 *
 * Example:
 *
 * val retailService = retrofit.create(RetailService::class.java)
 * val response = retailService.getActivePaywallRules()
 *
 * Note: Ensure that the Retrofit instance is properly configured before creating an instance of RetailService.
 *
 * @method getActivePaywallRules Fetch the active paywall rules.
 */
interface RetailService {

    @Keep
    @GET("paywall/active")
    suspend fun getActivePaywallRules(): Response<List<ActivePaywallRule>>
}