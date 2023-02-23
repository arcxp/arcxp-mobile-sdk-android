package com.arcxp.commerce.repositories

import com.arcxp.commerce.ArcXPCommerceSDKErrorType
import com.arcxp.commerce.models.ArcXPActivePaywallRules
import com.arcxp.commerce.retrofit.RetailService
import com.arcxp.commerce.retrofit.RetrofitController
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commons.util.Either
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success

/**
 * @suppress
 */
class RetailRepository(private val retailService: RetailService = RetrofitController.getRetailService()) {

    suspend fun getActivePaywallRules(): Either<Any?, ArcXPActivePaywallRules?> =
        try {
            val response = retailService.getActivePaywallRules()
            with (response) {
                when {
                    isSuccessful -> Success(ArcXPActivePaywallRules(body()!!))
                    else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

}