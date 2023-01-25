package com.arcxp.commerce.models

import androidx.annotation.Keep

/**
 * @suppress
 */
@Keep
data class ArcXPTaskRequestRequest(
    val startTime: String,
    val data: String,
    val taskId: Int
)