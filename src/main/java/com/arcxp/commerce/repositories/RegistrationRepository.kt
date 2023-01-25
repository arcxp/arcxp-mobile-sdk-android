package com.arcxp.commerce.repositories

import com.arcxp.commerce.ArcXPCommerceSDKErrorType
import com.arcxp.commerce.models.ArcXPEmailVerification
import com.arcxp.commerce.models.ArcXPSignUpRequest
import com.arcxp.commerce.models.ArcXPUser
import com.arcxp.commerce.models.ArcXPVerifyEmailRequest
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commerce.util.Either
import com.arcxp.commerce.util.Failure
import com.arcxp.commerce.util.Success

/**
 * Repository Class includes all registration related api call
 * @suppress
 */
class RegistrationRepository() :
    BaseIdentityRepository() {

    private val TAG = "RegistrationRepository"
    /**
     * function to make registration request
     *
     * @param signUpRequest request for registration
     * @return Either success response or failure
     */
    suspend fun signUp(signUpRequest: ArcXPSignUpRequest) : Either<Any?, ArcXPUser?> =
        try {
            val response = getIdentityService().signUp(signUpRequest)
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
     * function to make verify email request
     *
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

}
