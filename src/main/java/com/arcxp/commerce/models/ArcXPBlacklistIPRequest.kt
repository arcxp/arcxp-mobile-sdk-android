package com.arcxp.commerce.models

import androidx.annotation.Keep


/**
 * @suppress
 */
@Keep
data class ArcXPBlacklistIPRequest(
    val ip: String,
    val notes: String
)