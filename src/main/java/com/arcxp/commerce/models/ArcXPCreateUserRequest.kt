package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPCreateUserRequest(
    val identity: ArcXPIdentityRequest,
    val profile: ArcXPProfileRequest
)