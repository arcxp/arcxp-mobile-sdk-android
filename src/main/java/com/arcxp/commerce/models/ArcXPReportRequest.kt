package com.arcxp.commerce.models

import androidx.annotation.Keep

/**
 * @suppress
 */
@Keep
data class ArcXPReportRequest(
    val name: String,
    val startDate: String,
    val endDate: String,
    val reportType: String,
    val reportFormat: String,
    val paymentMethodExpiration: String,
    val emailVerified: Boolean,
    val profileReportsStatus: String
) {
    companion object {
        enum class ReportFormat {
            json, csv
        }
        enum class ProfileReportStatus {
            active, inactive
        }
    }
}