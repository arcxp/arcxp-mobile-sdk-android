package com.arcxp.commerce.models

import androidx.annotation.Keep

/**
 * @suppress
 */
@Keep
data class ArcXPPasswordResetRequest(
    val oldPassword: String,
    val newPassword: String
)