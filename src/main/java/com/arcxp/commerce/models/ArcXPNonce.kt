package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPNonce(
        val success: Boolean,
        var nonce: String? = null
)