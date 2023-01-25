package com.arcxp.commerce.repositories

import com.arcxp.commerce.ArcXPCommerceSDKErrorType
import com.arcxp.commerce.models.ArcXPIdentity
import com.arcxp.commerce.models.ArcXPPasswordResetRequest
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commerce.util.Either
import com.arcxp.commerce.util.Failure
import com.arcxp.commerce.util.Success
/**
 * @suppress
 */

class ChangePasswordRepository() :
    BaseIdentityRepository() {

    /**
     * function to make reset password request
     *
     * @param passwordChangeRequest request for reset password
     * @return Either success response or failure
     */
    suspend fun changePassword(passwordChangeRequest: ArcXPPasswordResetRequest): Either<Any?, ArcXPIdentity?> =
        try {
            val response = getIdentityService().changePassword(passwordChangeRequest)
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
