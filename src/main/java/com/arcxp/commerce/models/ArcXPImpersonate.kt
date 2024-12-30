package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPImpersonate(
    val uuid: String,
    val accessToken: String
)