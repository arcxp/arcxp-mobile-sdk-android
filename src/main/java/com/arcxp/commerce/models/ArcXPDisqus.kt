package com.arcxp.commerce.models

import androidx.annotation.Keep

/**
 * @suppress
 */
@Keep
data class ArcXPDisqus(
    val enabled: Boolean?,
    val publicKey: String?,
    val ssoKey: String?
) {
}