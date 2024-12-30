package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPAddress(
    val line1: String,
    val line2: String? = null,
    val locality: String,
    val region: String? = null,
    val postal: String? = null,
    val country: String,
    val type: String
)