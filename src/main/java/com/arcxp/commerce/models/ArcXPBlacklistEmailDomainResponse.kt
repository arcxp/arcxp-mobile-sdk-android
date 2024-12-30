package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPBlacklistEmailDomainResponse(
    val id: Int,
    val signinProviderID: Int,
    val emailDomain: String
)