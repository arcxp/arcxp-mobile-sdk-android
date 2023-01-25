package com.arcxp.commerce.models

import androidx.annotation.Keep

/**
 * @suppress
 */
@Keep
data class ArcXPIdentityRequest(
    val userName: String,
    val credentials: String,
    val grantType: String?
) {
    companion object {
        enum class GrantType {
            password, facebook, google, apple, twitter
        }
    }
}
