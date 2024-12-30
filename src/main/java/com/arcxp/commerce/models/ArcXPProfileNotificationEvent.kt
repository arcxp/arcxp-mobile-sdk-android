package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPProfileNotificationEvent(
    val createdOn: String,
    val createdBy: String,
    val modifiedOn: String,
    val modifiedBy: String,
    val deleteOn: String,
    val id: Int,
    val rule: ArcXPProfileDeletionRule,
    val uuid: String,
    val status: String,
    val notificationDate: String,
    val actionDate: String,
    val notificationSentCount: Int
) {

    companion object {
        enum class Status {
            SCHEDULED, COMPLETED, CANCELLED
        }
    }
}