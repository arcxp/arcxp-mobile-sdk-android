package com.arcxp.commerce.models

import androidx.annotation.Keep


/**
 * @suppress
 */
@Keep
data class ArcXPBlacklistDomainRequest(
    val emailDomain: String,
    val sigininProviderID: Int
)