package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPBlacklistUserName(
    val id: Int,
    val singinProviderID: Int,
    val userName: String
)