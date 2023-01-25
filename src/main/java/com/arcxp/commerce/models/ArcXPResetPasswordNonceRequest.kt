package com.arcxp.commerce.models

import androidx.annotation.Keep

/**
 * @suppress
 */
@Keep
data class ArcXPResetPasswordNonceRequest(
    val newPassword: String
)