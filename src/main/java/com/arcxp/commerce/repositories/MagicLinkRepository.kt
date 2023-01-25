package com.arcxp.commerce.repositories

import com.arcxp.commerce.ArcXPCommerceSDKErrorType
import com.arcxp.commerce.models.ArcXPOneTimeAccessLinkAuth
import com.arcxp.commerce.models.ArcXPOneTimeAccessLinkRequest
import com.arcxp.commerce.models.ArcXPOneTimeAccessLink
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commerce.util.Either
import com.arcxp.commerce.util.Failure
import com.arcxp.commerce.util.Success

/**
 * @suppress
 */
class MagicLinkRepository() :
        BaseIdentityRepository() {

        suspend fun getMagicLink(request: ArcXPOneTimeAccessLinkRequest) : Either<Any?, ArcXPOneTimeAccessLink?> =
            try {
                val response = getIdentityService().getMagicLink(request)
                with(response) {
                    when {
                        isSuccessful -> Success(body())
                        else -> Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, response.message(), response))
                    }
                }
            } catch(e: Exception) {
                Failure(ArcXPError(ArcXPCommerceSDKErrorType.SERVER_ERROR, e.message!!, e))
            }

        suspend fun loginMagicLink(nonce: String): Either<Any?, ArcXPOneTimeAccessLinkAuth?> =
            try {
                val response = getIdentityService().loginMagicLink(nonce)
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