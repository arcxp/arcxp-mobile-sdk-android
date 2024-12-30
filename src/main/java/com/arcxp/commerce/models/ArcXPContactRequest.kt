package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPContactRequest(
    val phone: String,
    val type: String
)