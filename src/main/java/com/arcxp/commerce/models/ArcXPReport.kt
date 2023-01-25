package com.arcxp.commerce.models

import androidx.annotation.Keep

/**
 * @suppress
 */
@Keep
data class ArcXPReport(
    val name: String,
    val startDateUTC: String,
    val endDateUTC: String,
    val jobID: String,
    val status: String
)