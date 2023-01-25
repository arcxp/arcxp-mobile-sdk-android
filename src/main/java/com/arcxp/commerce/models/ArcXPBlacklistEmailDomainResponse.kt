package com.arcxp.commerce.models

import androidx.annotation.Keep


/**
 * @suppress
 */
@Keep
data class ArcXPBlacklistEmailDomainResponse(
    val id: Int,
    val signinProviderID: Int,
    val emailDomain: String
)