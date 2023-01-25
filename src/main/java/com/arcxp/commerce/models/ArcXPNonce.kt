package com.arcxp.commerce.models

import androidx.annotation.Keep

/**
 * @suppress
 */
@Keep
data class ArcXPNonce(
        val success: Boolean,
        var nonce: String? = null
)