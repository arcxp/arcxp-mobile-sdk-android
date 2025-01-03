package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPProfileDeletionRuleCreateRequest(
    val typeId: Int,
    val name: String,
    val notificationTriggerDays: Int,
    val notificationRecurrenceDays: Int,
    val notificationLimit: Int
) {
}