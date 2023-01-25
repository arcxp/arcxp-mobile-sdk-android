package com.arcxp.commerce.repositories

import com.arcxp.commerce.ArcXPCommerceSDKErrorType
import com.arcxp.commerce.models.ArcXPAuthRequest
import com.arcxp.commerce.models.ArcXPAuth
import com.arcxp.commerce.models.ArcXPEmailVerification
import com.arcxp.commerce.models.ArcXPVerifyEmailRequest
import com.arcxp.commerce.retrofit.IdentityService
import com.arcxp.commerce.retrofit.RetrofitController
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commerce.util.Either
import com.arcxp.commerce.util.Failure
import com.arcxp.commerce.util.Success
import okhttp3.ResponseBody

/**
 * Repository Class includes all login related api call
 * @suppress
 */
class LoginRepository(identityService: IdentityService = RetrofitController.getIdentityService()) :
    BaseIdentityRepository(identityService) {

    /**
     * function to make login request
     *
     * @param authRequest request for authentication
     * @return Either success response or failure
     */
    suspend fun login(authRequest: ArcXPAuthRequest): Either<Any?, ArcXPAuth?> =
        try {
            val response = getIdentityService().login(authRequest)
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
     * function to make verification email request
     *
     * @param verifyEmailRequest request for verify email
     * @return Either success response or failure
     */
    suspend fun verifyEmail(verifyEmailRequest: ArcXPVerifyEmailRequest): Either<Any?, ArcXPEmailVerification?> =
        try {
            val response = getIdentityService().verifyEmail(verifyEmailRequest)
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
     * function to get apple login auth url
     *
     * @return Either success response or failure
     */
    suspend fun appleAuthUrl(): Either<Any?, ResponseBody?> =
        try {
            val response = getIdentityService().appleAuthUrl()
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
