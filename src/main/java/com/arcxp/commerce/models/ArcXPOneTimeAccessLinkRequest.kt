package com.arcxp.commerce.models

import androidx.annotation.Keep

/**
 * @suppress
 */
@Keep
data class ArcXPOneTimeAccessLinkRequest(
    val email: String? = null,
    val recaptchaToken: String? = null
)