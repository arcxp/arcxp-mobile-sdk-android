package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPAddressRequest(
    val line1: String,
    val line2: String?,
    val locality: String,
    val region: String?,
    val postal: String?,
    val country: String,
    val type: String
)