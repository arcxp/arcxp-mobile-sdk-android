package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPResetPasswordNonceRequest(
    val newPassword: String
)