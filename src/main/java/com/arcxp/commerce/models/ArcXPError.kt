package com.arcxp.commerce.models

import androidx.annotation.Keep

/**
 * @suppress
 */
@Keep
data class ArcXPError(
    val httpStatus: Int,
    val code: String,
    val message: String
)
