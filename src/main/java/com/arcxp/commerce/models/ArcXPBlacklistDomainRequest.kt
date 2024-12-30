package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPBlacklistDomainRequest(
    val emailDomain: String,
    val sigininProviderID: Int
)