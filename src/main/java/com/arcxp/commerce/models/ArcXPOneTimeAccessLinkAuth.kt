package com.arcxp.commerce.models

import androidx.annotation.Keep

/**
 * @suppress
 */
@Keep
data class ArcXPOneTimeAccessLinkAuth(
    val uuid: String,
    val accessToken: String
)