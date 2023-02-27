package com.arcxp.commerce.repositories

import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commerce.models.ArcXPActivePaywallRules
import com.arcxp.commerce.retrofit.RetailService
import com.arcxp.commerce.retrofit.RetrofitController
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.DependencyFactory.createArcXPException
import com.arcxp.commons.util.Either
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success

/**
 * @suppress
 */
class RetailRepository(private val retailService: RetailService = RetrofitController.getRetailService()) {

    suspend fun getActivePaywallRules(): Either<ArcXPException, ArcXPActivePaywallRules> =
        try {
            val response = retailService.getActivePaywallRules()
            with (response) {
                when {
                    isSuccessful -> Success(ArcXPActivePaywallRules(body()!!))
                    else -> Failure(createArcXPException(
                        type = ArcXPSDKErrorType.SERVER_ERROR,
                        message = response.message(),
                        value = response
                    ))
                }
            }
        } catch (e: Exception) {
            Failure(createArcXPException(
                type = ArcXPSDKErrorType.SERVER_ERROR,
                message = e.message,
                value = e
            ))
        }

}