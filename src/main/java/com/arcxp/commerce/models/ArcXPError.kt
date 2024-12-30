package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPError(
    val httpStatus: Int,
    val code: String,
    val message: String
)
