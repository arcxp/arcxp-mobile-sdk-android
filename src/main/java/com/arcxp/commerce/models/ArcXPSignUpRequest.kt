package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPSignUpRequest(
    val identity: ArcXPIdentityRequest,
    val profile: ArcXPProfileRequest,
    val recaptchaToken: String? = null
)