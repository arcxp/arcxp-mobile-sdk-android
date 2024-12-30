package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPPasswordResetRequest(
    val oldPassword: String,
    val newPassword: String
)