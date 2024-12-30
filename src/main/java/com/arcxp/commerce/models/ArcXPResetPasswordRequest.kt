package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPResetPasswordRequest(
    val newPassword: String
)