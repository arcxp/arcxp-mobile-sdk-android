package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPAuthRequest(
    val userName: String? = null,
    val credentials: String? = null,
    val token: String? = null,
    val grantType: String? = null,
    val recaptchaToken: String? = null
) {
    companion object {

        enum class GrantType(val value: String) {
            PASSWORD("password"),
            GOOGLE("google"),
            FACEBOOK("facebook"),
            APPLE("apple"),
            TWITTER("twitter"),
            REFRESH_TOKEN("refresh-token")
        }
    }
}
