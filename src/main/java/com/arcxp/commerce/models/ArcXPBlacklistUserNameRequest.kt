package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPBlacklistUserNameRequest(
    val userName: String,
    val singinProviderID: Int
)