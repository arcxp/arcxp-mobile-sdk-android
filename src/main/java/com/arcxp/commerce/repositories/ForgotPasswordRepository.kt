package com.arcxp.commerce.repositories

import com.arcxp.commerce.ArcXPCommerceSDKErrorType
import com.arcxp.commerce.models.*
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commerce.util.Either
import com.arcxp.commerce.util.Failure
import com.arcxp.commerce.util.Success

/**
 * Repository Class includes all forgot password related api call
 * @suppress
 */
class ForgotPasswordRepository() :
    BaseIdentityRepository() {

    /**
     * function to make reset password request
     *
     * @param resetPasswordRequest request for reset password
     * @return Either success response or failure
     */
    suspend fun resetPassword(resetPasswordRequest: ArcXPResetPasswordRequestRequest): Either<Any?, ArcXPRequestPasswordReset?> =
        try {
            val response = getIdentityService().resetPassword(resetPasswordRequest)
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
