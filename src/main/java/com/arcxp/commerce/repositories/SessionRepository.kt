package com.arcxp.commerce.repositories

import com.arcxp.commerce.ArcXPCommerceSDKErrorType
import com.arcxp.commerce.models.ArcXPAuthRequest
import com.arcxp.commerce.models.ArcXPAuth
import com.arcxp.commerce.util.*

/**
 * Repository Class includes all registration related api call
 * @suppress
 */
class SessionRepository() :
    BaseIdentityRepository() {

    suspend fun logout(): Either<Any?, Void?> =
        try {
            val response = getIdentityService().logout()
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    /**
     * function to validate if the cached access token is still validated
     *
     * @return Either success response or failure
     */
    suspend fun validateJwt(): Either<Any?, ArcXPAuth?> =
        try {
            val response = getIdentityService().recapToken(ArcXPAuthRequest())
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    /**
     * function to extend the session using refresh token
     *
     * @return Either success response or failure
     */
    suspend fun refreshToken(token: String?, grantType: String): Either<Any?, ArcXPAuth?> =
        try {
            val response = getIdentityService().recapToken(
                ArcXPAuthRequest(token, grantType)
            )
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

}
