package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPUpdateUsersStatusRequest(
    val status: String
)