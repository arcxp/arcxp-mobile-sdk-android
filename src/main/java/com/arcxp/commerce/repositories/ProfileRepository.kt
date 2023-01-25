package com.arcxp.commerce.repositories

import com.arcxp.commerce.ArcXPCommerceSDKErrorType
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.ArcXPProfilePatchRequest
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commerce.util.Either
import com.arcxp.commerce.util.Failure
import com.arcxp.commerce.util.Success

/**
 * Repository Class includes all profile related api call
 * @suppress
 */
class ProfileRepository() :
    BaseIdentityRepository() {

    /**
     * function to make get profile request
     *
     * @return Either success response or failure
     */
    suspend fun getProfile(): Either<Any?, ArcXPProfileManage?> =
        try {
            val response = getIdentityService().getProfile()
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
     * function to make patch profile request
     *
     * @param profileRequest request for patch profile
     * @return Either success response or failure
     */
    suspend fun patchProfile(profileRequest: ArcXPProfilePatchRequest): Either<Any?, ArcXPProfileManage?> =
        try {
            val response = getIdentityService().patchProfile(profileRequest)
            with(response) {
                when {
                    isSuccessful -> Success(body())
                    else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message() , response))
                }
            }
        } catch (e: Exception) {
            Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
        }
}
