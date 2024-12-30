package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPOneTimeAccessLinkRequest(
    val email: String? = null,
    val recaptchaToken: String? = null
)