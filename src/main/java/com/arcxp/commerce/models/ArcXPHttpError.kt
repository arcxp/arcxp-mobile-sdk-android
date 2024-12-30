package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPHttpError(
    val error: String,
    val message: String
)