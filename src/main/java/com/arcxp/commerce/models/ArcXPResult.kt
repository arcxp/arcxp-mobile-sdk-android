package com.arcxp.commerce.models

import androidx.annotation.Keep

/**
 * @suppress
 */
@Keep
data class ArcXPResult(
    val body: ArcXPHttpEntity,
//TODO:    val flash: String,
//    val session: String,
    val cookies: ArcXPCookies
)