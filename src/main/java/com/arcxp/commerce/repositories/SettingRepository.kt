package com.arcxp.commerce.repositories

import com.arcxp.commerce.ArcXPCommerceSDKErrorType
import com.arcxp.commerce.models.ArcXPAnonymizeUser
import com.arcxp.commerce.models.ArcXPDeleteUser
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commerce.util.Either
import com.arcxp.commerce.util.Failure
import com.arcxp.commerce.util.Success

/**
 * Repository Class includes all settings related api call
 * @suppress
 */
class SettingRepository() :
    BaseIdentityRepository() {

    /**
     * function to log out current user
     *
     * @return Either success response or failure
     */
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
     * function to request a current user deletion
     *
     * @return Either success response or failure
     */
    suspend fun deleteUser(): Either<Any?, ArcXPAnonymizeUser?> =
        try {
            val response = getIdentityService().deleteUser()
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message(), response))
                }
            }
        } catch (e: Exception) {
            Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
        }

    suspend fun approveDeletion(nonce: String): Either<Any?, ArcXPDeleteUser?> =
            try {
                val response = getIdentityService().approveDeletion(nonce)
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
