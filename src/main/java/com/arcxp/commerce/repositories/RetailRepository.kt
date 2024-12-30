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
 * RetailRepository is responsible for handling retail-related data operations within the ArcXP Commerce module.
 * It interacts with the RetailService to perform operations such as fetching active paywall rules.
 *
 * The class defines the following operations:
 * - Fetch active paywall rules
 *
 * Usage:
 * - Create an instance of RetailRepository and call the provided methods to perform retail operations.
 * - Handle the results through the Either type, which encapsulates success and failure cases.
 *
 * Example:
 *
 * val retailRepository = RetailRepository()
 * val result = retailRepository.getActivePaywallRules()
 *
 * Note: Ensure that the RetrofitController and RetailService are properly configured before using RetailRepository.
 *
 * @method getActivePaywallRules Fetch the active paywall rules.
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
                type = ArcXPSDKErrorType.EXCEPTION,
                message = e.message,
                value = e
            ))
        }

}