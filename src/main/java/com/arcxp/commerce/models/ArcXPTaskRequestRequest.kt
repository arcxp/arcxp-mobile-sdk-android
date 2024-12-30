package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPTaskRequestRequest(
    val startTime: String,
    val data: String,
    val taskId: Int
)