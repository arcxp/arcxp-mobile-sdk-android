package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPBlacklistIPRequest(
    val ip: String,
    val notes: String
)